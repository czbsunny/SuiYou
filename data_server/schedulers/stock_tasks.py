#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
股票数据定时更新模块
"""

import logging
import akshare as ak
from datetime import datetime
from sqlalchemy.orm import Session
from elasticsearch import Elasticsearch
from typing import Dict, List, Optional

from models.fund_portfolio_hold import FundPortfolioHold
from models.fund_nav_realtime import FundNavRealtime
from database.init_db import SessionLocal
from datetime import datetime
from utils.helpers import is_trading_day

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

def init_es():
    try:
        es = Elasticsearch(
            "http://localhost:9200",
            request_timeout=5
        )

        if not es.ping():
            raise RuntimeError("ES ping failed")

        logger.info("ES客户端初始化成功")
        return es

    except Exception:
        logger.exception("ES客户端初始化失败")
        return None

es = init_es()

# 基金持仓数据缓存
fund_portfolio_cache: Dict[str, List[Dict]] = {}
last_cache_load_time: Optional[datetime] = None
cache_valid = False

# 缓存有效期时间点
CACHE_LOAD_TIME = "00:00"  # 9点20分加载缓存
CACHE_INVALIDATE_TIME = "18:00"  # 16点后缓存失效

def check_and_update_cache():
    """
    检查并更新缓存状态
    每天早上9:20左右初始化一次缓存
    """
    global cache_valid, last_cache_load_time
    now = datetime.now()
    current_time = now.strftime('%H:%M')
    current_date = now.date()
    
    if not cache_valid and current_time >= CACHE_LOAD_TIME and current_time < CACHE_INVALIDATE_TIME:
        if not last_cache_load_time or last_cache_load_time.date() != current_date:
            load_fund_portfolio_cache()


def load_fund_portfolio_cache():
    """
    加载基金持仓数据到缓存
    """
    global fund_portfolio_cache, last_cache_load_time, cache_valid
    now = datetime.now()
    logger.info(f"[{now.strftime('%Y-%m-%d %H:%M:%S')}] 开始加载基金持仓数据到缓存...")
    
    db = SessionLocal()
    try:
        # 获取所有有持仓数据的基金代码
        fund_codes = get_funds_with_portfolio(db)
        logger.info(f"[{now.strftime('%Y-%m-%d %H:%M:%S')}] 共有 {len(fund_codes)} 只有持仓数据的基金")
        
        # 加载每个基金的持仓数据
        fund_portfolio_cache = {}
        for fund_code in fund_codes:
            portfolio = db.query(FundPortfolioHold).filter(
                FundPortfolioHold.fund_code == fund_code
            ).order_by(FundPortfolioHold.quarter.desc()).limit(10).all()
            
            # 将查询结果转换为字典列表
            portfolio_data = []
            for item in portfolio:
                portfolio_data.append({
                    'stock_code': item.stock_code,
                    'weight': item.weight
                })
            
            if portfolio_data:
                fund_portfolio_cache[fund_code] = portfolio_data
        
        last_cache_load_time = now
        cache_valid = True
        logger.info(f"[{now.strftime('%Y-%m-%d %H:%M:%S')}] 基金持仓数据缓存加载完成，共加载 {len(fund_portfolio_cache)} 只基金的数据")
    except Exception as e:
        logger.error(f"[{now.strftime('%Y-%m-%d %H:%M:%S')}] 加载基金持仓数据缓存失败: {str(e)}")
        cache_valid = False
    finally:
        db.close()


def get_fund_portfolio_from_cache(fund_code: str) -> List[Dict]:
    """
    从缓存中获取基金持仓数据
    """
    return fund_portfolio_cache.get(fund_code, [])

def process_lof_funds() -> List[str]:
    """
    处理LOF基金数据，直接获取实时涨跌
    返回已处理的LOF基金代码列表
    """
    lof_start_time = datetime.now()
    logger.info(f"[{lof_start_time.strftime('%Y-%m-%d %H:%M:%S')}] 开始获取LOF基金数据...")
    fund_lof_spot_em_df = ak.fund_lof_spot_em()
    lof_end_time = datetime.now()
    lof_duration = (lof_end_time - lof_start_time).total_seconds()
    
    lof_fund_codes = []
    
    if fund_lof_spot_em_df.empty:
        logger.warning(f"[{lof_end_time.strftime('%Y-%m-%d %H:%M:%S')}] LOF基金数据为空，跳过处理")
    else:
        logger.info(f"[{lof_end_time.strftime('%Y-%m-%d %H:%M:%S')}] 获取到 {len(fund_lof_spot_em_df)} 只LOF基金的数据，耗时: {lof_duration:.2f}秒")
        
        # 处理LOF基金数据
        lof_fund_changes = []
        for _, row in fund_lof_spot_em_df.iterrows():
            fund_code = row['代码']
            change = row['涨跌幅']  # 注意单位是%
            lof_fund_codes.append(fund_code)
            
            lof_fund_changes.append({
                'fund_id': fund_code,
                'change': change,
                'timestamp': datetime.now().isoformat()
            })
        
        # 写入ES
        if lof_fund_changes:
            logger.info(f"开始写入 {len(lof_fund_changes)} 只LOF基金数据到ES...")
            write_fund_changes_to_es(lof_fund_changes)
            logger.info(f"成功写入 {len(lof_fund_changes)} 只LOF基金数据到ES")
    
    return lof_fund_codes


async def update_stock_data_and_calculate_fund_change():
    """
    更新股票数据并计算基金涨跌百分比
    从9点25分开始，每5分钟执行一次
    """
    start_time = datetime.now()
    logger.info(f"[{start_time.strftime('%Y-%m-%d %H:%M:%S')}] 开始更新股票数据并计算基金涨跌")
    
    # 检查是否为交易日
    today = start_time.date()
    if not is_trading_day(today):
        logger.info(f"[{start_time.strftime('%Y-%m-%d %H:%M:%S')}] 今天是{today}，非交易日，跳过股票数据更新任务")
        return
    
    # 检查并更新缓存状态
    check_and_update_cache()
    
    # 检查当前时间是否在交易时间内
    current_time = start_time.strftime('%H:%M')
    
    # 只在交易时间内执行（9:25-11:30, 13:00-18:00）
    if not ((current_time >= '09:25' and current_time <= '11:30') or 
            (current_time >= '13:00' and current_time <= '18:00')):
        logger.info(f"[{start_time.strftime('%Y-%m-%d %H:%M:%S')}] 当前时间 {current_time} 不在交易时间内，跳过更新")
        return
    
    db = SessionLocal()
    try:
        # 获取股票数据
        stock_start_time = datetime.now()
        logger.info(f"[{stock_start_time.strftime('%Y-%m-%d %H:%M:%S')}] 开始获取A股股票数据...")
        stock_zh_a_spot_em_df = ak.stock_zh_a_spot()
        stock_end_time = datetime.now()
        stock_duration = (stock_end_time - stock_start_time).total_seconds()
        
        if stock_zh_a_spot_em_df.empty:
            logger.warning(f"[{stock_end_time.strftime('%Y-%m-%d %H:%M:%S')}] 股票数据为空，跳过计算")
            return
        
        logger.info(f"[{stock_end_time.strftime('%Y-%m-%d %H:%M:%S')}] 获取到 {len(stock_zh_a_spot_em_df)} 只股票的数据，耗时: {stock_duration:.2f}秒")
        
        # 转换股票数据为字典，方便查询
        convert_start_time = datetime.now()
        stock_data = {}
        for _, row in stock_zh_a_spot_em_df.iterrows():
            full_code = row['代码']
            if len(full_code) == 8:
                stock_code = full_code[2:]
            else:
                # 如果格式不符合预期，使用原始代码
                stock_code = full_code
            
            stock_data[stock_code] = {
                'name': row['名称'],
                'price': row['最新价'],
                'change': row['涨跌幅']
            }
        convert_end_time = datetime.now()
        convert_duration = (convert_end_time - convert_start_time).total_seconds()
        logger.info(f"[{convert_end_time.strftime('%Y-%m-%d %H:%M:%S')}] 转换股票数据完成，耗时: {convert_duration:.2f}秒")
        
        # 处理LOF基金数据
        lof_fund_codes = process_lof_funds()
        
        # 获取需要计算涨跌的基金
        fund_start_time = datetime.now()
        if cache_valid:
            # 从缓存中获取基金代码
            fund_codes = list(fund_portfolio_cache.keys())
            logger.info(f"[{fund_start_time.strftime('%Y-%m-%d %H:%M:%S')}] 从缓存中获取到 {len(fund_codes)} 只有持仓数据的基金")
        else:
            # 从数据库获取基金代码
            fund_codes = get_funds_with_portfolio(db)
        fund_end_time = datetime.now()
        fund_duration = (fund_end_time - fund_start_time).total_seconds()
        logger.info(f"[{fund_end_time.strftime('%Y-%m-%d %H:%M:%S')}] 共获取到 {len(fund_codes)} 只有持仓数据的基金，耗时: {fund_duration:.2f}秒")
        
        # 过滤掉LOF基金，避免重复处理
        if lof_fund_codes:
            fund_codes = [code for code in fund_codes if code not in lof_fund_codes]
            logger.info(f"过滤掉 {len(lof_fund_codes)} 只LOF基金，剩余 {len(fund_codes)} 只基金需要计算涨跌")
        
        # 计算基金涨跌
        calc_start_time = datetime.now()
        logger.info(f"[{calc_start_time.strftime('%Y-%m-%d %H:%M:%S')}] 开始计算基金涨跌...")
        fund_changes = []
        for fund_code in fund_codes:
            try:
                fund_change = calculate_fund_change(db, fund_code, stock_data)
                if fund_change:
                    fund_changes.append(fund_change)
            except Exception as e:
                logger.error(f"计算基金 {fund_code} 涨跌失败: {str(e)}")
        calc_end_time = datetime.now()
        calc_duration = (calc_end_time - calc_start_time).total_seconds()
        logger.info(f"[{calc_end_time.strftime('%Y-%m-%d %H:%M:%S')}] 计算完成 {len(fund_changes)} 只基金的涨跌，耗时: {calc_duration:.2f}秒")
        
        # 写入ES
        if fund_changes:
            es_start_time = datetime.now()
            logger.info(f"[{es_start_time.strftime('%Y-%m-%d %H:%M:%S')}] 开始写入ES...")
            write_fund_changes_to_es(fund_changes)
            es_end_time = datetime.now()
            es_duration = (es_end_time - es_start_time).total_seconds()
            logger.info(f"[{es_end_time.strftime('%Y-%m-%d %H:%M:%S')}] 成功计算并写入 {len(fund_changes)} 只基金的涨跌数据，耗时: {es_duration:.2f}秒")
        else:
            logger.info("没有基金涨跌数据需要写入")
        
    except Exception as e:
        logger.error(f"更新股票数据并计算基金涨跌失败: {str(e)}")
    finally:
        db.close()
        end_time = datetime.now()
        total_duration = (end_time - start_time).total_seconds()
        logger.info(f"[{end_time.strftime('%Y-%m-%d %H:%M:%S')}] 股票数据更新和基金涨跌计算任务完成，总耗时: {total_duration:.2f}秒")

def get_funds_with_portfolio(db: Session) -> list:
    """
    获取有持仓数据的基金代码
    """
    funds = db.query(FundPortfolioHold.fund_code).distinct().all()
    return [fund[0] for fund in funds]

def calculate_fund_change(db: Session, fund_code: str, stock_data: dict) -> dict:
    """
    计算基金涨跌百分比
    """
    # 从缓存中获取基金持仓数据
    portfolio_data = fund_portfolio_cache.get(fund_code, [])
    
    # 如果缓存中无数据，则从数据库查询
    if not portfolio_data:
        logger.warning(f"基金 {fund_code} 持仓数据未缓存，从数据库查询")
        portfolio = db.query(FundPortfolioHold).filter(
            FundPortfolioHold.fund_code == fund_code
        ).order_by(FundPortfolioHold.quarter.desc()).limit(10).all()
        
        # 将查询结果转换为字典列表
        portfolio_data = []
        for item in portfolio:
            portfolio_data.append({
                'stock_code': item.stock_code,
                'weight': item.weight
            })
    
    if not portfolio_data:
        logger.warning(f"基金 {fund_code} 无持仓数据")
        return None
    
    # 计算加权涨跌
    total_weight = 0
    weighted_change = 0
    for item in portfolio_data:
        stock_code = item['stock_code']
        weight = item['weight']
        
        if stock_code in stock_data:
            stock_change = stock_data[stock_code]['change']
            weighted_change += weight * stock_change / 100
            total_weight += weight
        else:
            logger.warning(f"基金 {fund_code} 持仓中股票 {stock_code} 无有效数据")
    

    if total_weight == 0:
        logger.warning(f"基金 {fund_code} 持仓中无有效股票数据")
        return None
    
    # 计算最终涨跌百分比
    fund_change_percent = weighted_change
    
    return {
        'fund_id': fund_code,
        'change': fund_change_percent,
        'timestamp': datetime.now().isoformat()
    }

def write_fund_changes_to_es(fund_changes: list):
    """
    将基金涨跌数据写入ES
    只保留一份最新的基金涨跌数据
    """
    if not es:
        logger.error("ES客户端未初始化，无法写入数据")
        return
    
    try:
        # 使用固定的索引名称，只保留最新数据
        index_name = "fund_change_latest"
        
        # 确保索引存在
        if not es.indices.exists(index=index_name):
            es.indices.create(index=index_name)
            logger.info(f"创建ES索引: {index_name}")
        
        for fund_change in fund_changes:
            # 使用fund_id作为文档ID，每次更新时覆盖旧数据
            es.index(
                index=index_name,
                body=fund_change,
                id=fund_change['fund_id']
            )
        
        logger.info(f"成功将 {len(fund_changes)} 条基金涨跌数据写入ES，只保留最新数据")
        
        # 异步将数据同步到FundNavRealtime表
        import asyncio
        asyncio.create_task(sync_to_fund_nav_realtime(fund_changes))
        
    except Exception as e:
        logger.error(f"写入ES失败: {str(e)}")

async def sync_to_fund_nav_realtime(fund_changes: list):
    """
    将基金涨跌数据同步到FundNavRealtime表
    异步执行，优先保证写入到ES
    """
    logger.info(f"开始将 {len(fund_changes)} 条基金涨跌数据同步到FundNavRealtime表")
    
    db = SessionLocal()
    try:
        for fund_change in fund_changes:
            fund_code = fund_change['fund_id']
            change = fund_change['change']
            
            # 查找是否已存在该基金的最新记录
            latest_record = db.query(FundNavRealtime).filter(
                FundNavRealtime.fund_code == fund_code
            ).order_by(FundNavRealtime.datetime.desc()).first()
            
            if latest_record:
                # 更新现有记录
                latest_record.daily_growth_rate = change
                latest_record.datetime = datetime.now()
                latest_record.updated_at = datetime.now()
            else:
                # 创建新记录（注意：这里需要有nav值，暂时使用1.0作为默认值）
                # 实际应用中，应该从其他来源获取最新的nav值
                new_record = FundNavRealtime(
                    fund_code=fund_code,
                    nav=1.0,  # 默认值，实际应用中需要替换
                    daily_growth_rate=change,
                    weight_ready=0,  # 默认值
                    datetime=datetime.now()
                )
                db.add(new_record)
        
        db.commit()
        logger.info(f"成功将 {len(fund_changes)} 条基金涨跌数据同步到FundNavRealtime表")
    except Exception as e:
        logger.error(f"同步到FundNavRealtime表失败: {e}")
        db.rollback()
    finally:
        db.close()
