#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
基金行业配置数据定时更新任务调度模块
每季度（1、4、7、10月）每天更新全量数据
"""

import asyncio
import logging
from datetime import datetime, date, timedelta
from typing import List, Dict, Any, Optional
from sqlalchemy.orm import Session
from sqlalchemy import and_
import pandas as pd

from datafetch.fund_processor import FundProcessor
from database.init_db import get_db
from models.fund import Fund
from models.fund_industry_allocation import FundIndustryAllocation

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 初始化基金处理器实例
fund_processor = FundProcessor()

def get_target_date() -> str:
    """
    根据当前日期计算目标年份
    使用当前日期月初往前减1天的方式确定要查询的年份
    
    Returns:
        str: 目标年份字符串
    """
    now = datetime.now()
    # 获取当前月初
    first_day_of_month = now.replace(day=1)
    # 月初往前减1天
    target_date = first_day_of_month - timedelta(days=1)
    
    return str(target_date.date())

def parse_date(date_str) -> Optional[date]:
    """尝试多种格式解析日期"""
    if not date_str:
        return None
    if isinstance(date_str, datetime):
        return date_str.date()
    if isinstance(date_str, date):
        return date_str
    
    for fmt in ['%Y-%m-%d', '%Y/%m/%d', '%Y.%m.%d']:
        try:
            return datetime.strptime(str(date_str), fmt).date()
        except ValueError:
            continue
    return None

async def should_update_fund_industry(db: Session, fund_code: str, target_date: str) -> bool:
    """
    检查基金是否需要更新行业配置数据
    如果基金最新截止数据已经是上个季度的，就不重复拉取
    
    Args:
        db: 数据库会话
        fund_code: 基金代码
        target_date: 目标日期
    
    Returns:
        bool: 是否需要更新
    """
    try:
        # 查询该基金最新的行业配置数据
        latest_allocation = db.query(FundIndustryAllocation)\
            .filter(FundIndustryAllocation.fund_code == fund_code)\
            .order_by(FundIndustryAllocation.as_of_date.desc())\
            .first()
        
        if not latest_allocation or not latest_allocation.as_of_date:
            return True  # 没有数据，需要更新
        
        # 如果最新数据已经是上个季度或更早的，就不需要更新
        if latest_allocation.as_of_date == parse_date(target_date):
            logger.info(f"基金 {fund_code} 最新行业配置数据截止日期为 {latest_allocation.as_of_date}，已是上个季度数据，跳过更新")
            return False
        
        return True
        
    except Exception as e:
        logger.warning(f"检查基金 {fund_code} 是否需要更新行业配置数据时出错: {str(e)}")
        return True  # 出错时默认更新

async def process_single_fund_industry(fund_code: str, target_date: str, index: int) -> Dict[str, Any]:
    """
    处理单个基金的行业配置数据
    
    Args:
        fund_code: 基金代码
        target_date: 目标日期
        index: 基金在批次中的索引
    
    Returns:
        Dict: 包含处理结果的字典
    """
    # 每个并发任务使用独立的数据库会话
    db = next(get_db())
    result = {
        'fund_code': fund_code,
        'success': False,
        'error': None,
        'index': index,
        'records_count': 0
    }
    
    try:
        # 检查基金是否存在
        fund = db.query(Fund).filter(Fund.fund_code == fund_code).first()
        if not fund:
            result['error'] = "基金不存在"
            return result
        
        # 检查是否需要更新
        should_update = await should_update_fund_industry(db, fund_code, target_date)
        if not should_update:
            result['success'] = True  # 跳过更新也算成功
            result['error'] = "数据已是最新，跳过更新"
            return result
        
        # 获取行业配置数据
        df = await fund_processor.fetch_fund_industry_allocation(fund_code, parse_date(target_date).year)
        if df is None or df.empty:
            result['error'] = "无行业配置数据"
            return result
        
        # 创建新的行业配置记录
        allocations = []
        for _, row in df.iterrows():
            try:
                alloc = FundIndustryAllocation(
                    fund_code=fund_code,
                    rank=int(row.get('序号')) if row.get('序号') and pd.notna(row.get('序号')) else None,
                    industry_name=str(row.get('行业类别', '')) if row.get('行业类别') else '未知',
                    net_value_ratio=float(row.get('占净值比例')) if row.get('占净值比例') and pd.notna(row.get('占净值比例')) else None,
                    market_value=float(row.get('市值')) if row.get('市值') and pd.notna(row.get('市值')) else None,
                    as_of_date=parse_date(row.get('截止时间'))
                )
                allocations.append(alloc)
            except Exception as e:
                logger.warning(f"基金 {fund_code} 行业数据解析失败: {e}")
        
        if not allocations:
            result['error'] = "解析结果为空"
            return result
        
        # 删除旧记录
        db.query(FundIndustryAllocation)\
            .filter(FundIndustryAllocation.fund_code == fund_code)\
            .filter(FundIndustryAllocation.as_of_date.between(
                date(parse_date(target_date).year, 1, 1),
                date(parse_date(target_date).year, 12, 31)
            ))\
            .delete()
        
        # 批量插入新记录
        db.add_all(allocations)
        db.commit()
        
        result['success'] = True
        result['records_count'] = len(allocations)
        
    except Exception as e:
        db.rollback()
        result['error'] = str(e)
        logger.error(f"处理基金 {fund_code} 行业配置数据时出错: {str(e)}")
    finally:
        try:
            db.close()
        except:
            pass
    
    return result

async def process_fund_industry_concurrent(fund_codes: List[str], target_date: str, batch_size: int = 10) -> Dict[str, int]:
    """
    并发处理基金行业配置数据
    
    Args:
        fund_codes: 基金代码列表
        target_year: 目标年份
        batch_size: 每批次并发处理的基金数量
    
    Returns:
        Dict: 包含处理统计的字典
    """
    total_count = len(fund_codes)
    success_count = 0
    skip_count = 0
    error_count = 0
    total_records = 0
    
    logger.info(f"开始处理 {total_count} 只基金的行业配置数据，目标日期: {target_date}")
    
    # 分批处理基金
    for batch_start in range(0, total_count, batch_size):
        batch_end = min(batch_start + batch_size, total_count)
        current_batch = fund_codes[batch_start:batch_end]
        logger.info(f"处理批次 {batch_start//batch_size + 1}/{(total_count + batch_size - 1)//batch_size}, 共{len(current_batch)}只基金")
        
        # 创建并发任务
        tasks = [
            process_single_fund_industry(fund_code, target_date, i)
            for i, fund_code in enumerate(current_batch, start=batch_start)
        ]
        
        # 执行并发任务
        batch_results = await asyncio.gather(*tasks, return_exceptions=True)
        
        # 处理结果
        for result in batch_results:
            if isinstance(result, Exception):
                error_count += 1
                logger.error(f"任务执行异常: {str(result)}")
            elif result.get('success', False):
                success_count += 1
                total_records += result.get('records_count', 0)
                if result.get('error') and "跳过更新" in result.get('error', ''):
                    skip_count += 1
            else:
                error_count += 1
                logger.error(f"基金 {result.get('fund_code', 'unknown')} 处理失败: {result.get('error', 'unknown error')}")
        
        # 批次之间添加延迟，避免请求过于频繁
        if batch_end < total_count:
            await asyncio.sleep(1)
        
        # 记录进度
        if (batch_start // batch_size + 1) % 5 == 0 or batch_end >= total_count:
            logger.info(f"已处理 {batch_end}/{total_count} 个基金，成功: {success_count}, 跳过: {skip_count}, 失败: {error_count}")
    
    return {
        'total': total_count,
        'success': success_count,
        'skip': skip_count,
        'error': error_count,
        'total_records': total_records
    }

async def update_all_fund_industry_allocation():
    """
    更新所有基金行业配置数据
    每季度（1、4、7、10月）每天执行
    """
    task_name = "季度基金行业配置更新任务"
    logger.info(f"开始执行{task_name}，时间：{datetime.now()}")
    start_time = datetime.now()
    
    db = None
    try:
        # 获取目标日期
        target_date = get_target_date()
        logger.info(f"目标日期: {target_date}")
        
        # 获取所有基金代码
        db = next(get_db())
        funds = db.query(Fund.fund_code).filter(Fund.is_valid_fund == True).all()
        fund_codes = [f[0] for f in funds]
        db.close()
        
        if not fund_codes:
            logger.warning("没有找到任何基金代码")
            return
        
        # 并发处理基金行业配置数据
        stats = await process_fund_industry_concurrent(fund_codes, target_date, batch_size=15)
        
        # 计算耗时
        duration = (datetime.now() - start_time).total_seconds()
        logger.info(f"{task_name}完成，统计: 总数={stats['total']}, 成功={stats['success']}, "
                   f"跳过={stats['skip']}, 失败={stats['error']}, 总记录数={stats['total_records']}, "
                   f"耗时：{duration:.2f}秒，时间：{datetime.now()}")
        
    except Exception as e:
        logger.error(f"{task_name}执行失败: {str(e)}")
    finally:
        # 确保数据库会话关闭
        if db:
            try:
                db.close()
            except:
                pass