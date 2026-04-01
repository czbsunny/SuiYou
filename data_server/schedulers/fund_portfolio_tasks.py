#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
基金持仓数据定时更新模块
"""

import logging
import time
import akshare as ak
from datetime import datetime
from sqlalchemy.orm import Session
import pandas as pd

from models.fund import Fund
from models.fund_portfolio_hold import FundPortfolioHold
from database.init_db import SessionLocal

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

def get_valid_funds_map(db: Session) -> list:
    """
    获取所有有效的非货币型基金代码
    """
    # 定义需要过滤的基金类型列表
    filtered_fund_types = [
        '货币型-普通货币', '货币型-浮动净值', '货币型',
        '混合型-绝对收益', 'Reits', '其他', '商品',
        '债券型-中短债', '债券型-长债',
        '债券型-混合一级', '债券型-混合二级',
        '指数型-海外股票', '指数型-固收',
        'FOF-稳健型', 'FOF-进取型', 'FOF-均衡型',
        'QDII-REITs','QDII-FOF','QDII-商品','QDII-纯债','QDII-混合债'
    ]    
    
    # 一次性查询所有非货币型基金
    valid_funds = db.query(Fund).filter(
        Fund.fund_type.notin_(filtered_fund_types),
        Fund.is_valid_fund == True,
        Fund.latest_scale.notin_(['---', '0亿份'])
    ).all()
    
    # 返回基金代码列表
    return [fund.fund_code for fund in valid_funds]

def get_quarter_to_update() -> str:
    """
    根据当前时间确定应该更新的季度
    """
    now = datetime.now()
    month = now.month
    
    if month <= 3:
        # 1-3月：更新上一年的第4季度
        year = now.year - 1
        quarter = 4
    elif month <= 6:
        # 4-6月：更新当年的第1季度
        year = now.year
        quarter = 1
    elif month <= 9:
        # 7-9月：更新当年的第2季度
        year = now.year
        quarter = 2
    else:
        # 10-12月：更新当年的第3季度
        year = now.year
        quarter = 3
    
    return f"{year}年{quarter}季度"

def get_updated_fund_codes(db: Session) -> set:
    """
    获取已经更新过指定季度的基金代码
    """
    # 确定应该更新的季度
    target_quarter = get_quarter_to_update()
    
    # 查询已更新过该季度的基金代码
    updated_funds = db.query(FundPortfolioHold.fund_code).filter(
        FundPortfolioHold.quarter.like(f"{target_quarter}%")
    ).all()
    
    # 返回基金代码集合
    return {fund[0] for fund in updated_funds}

async def update_all_fund_portfolio_hold():
    """
    更新所有基金的持仓数据
    每个季度刚开始的月份每天执行
    """
    logger.info("开始更新所有基金持仓数据")
    
    db = SessionLocal()
    try:
        # 根据当前月份判断应该获取哪一年的数据
        # 1-3月：获取上一年的数据（因为当年第一季度数据还未发布）
        # 4-12月：获取当前年的数据
        now = datetime.now()
        if now.month <= 3:
            current_year = now.year - 1
        else:
            current_year = now.year
        logger.info(f"根据当前时间 {now.strftime('%Y-%m-%d')}，确定获取 {current_year} 年的基金持仓数据")
        
        # 获取所有有效的非货币型基金代码
        valid_fund_codes = get_valid_funds_map(db)
        
        # 确定应该更新的季度
        target_quarter = get_quarter_to_update()
        
        # 获取已经更新过该季度的基金代码
        updated_fund_codes = get_updated_fund_codes(db)
        
        # 过滤出需要更新的基金代码
        fund_codes_to_update = [code for code in valid_fund_codes if code not in updated_fund_codes]
        
        logger.info(f"共获取到 {len(valid_fund_codes)} 只有效的非货币型基金")
        logger.info(f"当前需要更新的季度是: {target_quarter}")
        logger.info(f"其中 {len(updated_fund_codes)} 只已经更新过该季度，{len(fund_codes_to_update)} 只需要更新")
        
        # 计数器
        success_count = 0
        fail_count = 0
        total_count = len(fund_codes_to_update)
        batch_size = 100
        
        # 遍历基金代码，更新持仓数据
        for index, fund_code in enumerate(fund_codes_to_update, 1):
            try:
                update_fund_portfolio_hold(db, fund_code, current_year, commit=False)
                success_count += 1
                
                # 每更新10只基金，暂停1秒，避免请求过于频繁
                if success_count % 10 == 0:
                    time.sleep(1)
                
                # 每100只基金commit一次
                if success_count % batch_size == 0:
                    db.commit()
                    logger.info(f"已更新 {success_count}/{total_count} 只基金，进度: {success_count/total_count*100:.1f}%")
                
            except Exception as e:
                fail_count += 1
                logger.error(f"更新基金 {fund_code} 持仓数据失败: {str(e)}")
        
        # 提交剩余的数据
        if success_count % batch_size != 0:
            db.commit()
            logger.info(f"已更新 {success_count}/{total_count} 只基金，进度: 100%")
        
        logger.info(f"基金持仓数据更新完成，成功: {success_count}, 失败: {fail_count}")
        
    except Exception as e:
        db.rollback()
        logger.error(f"更新所有基金持仓数据失败: {str(e)}")
    finally:
        db.close()

def update_fund_portfolio_hold(db: Session, fund_code: str, year: int, commit: bool = True):
    """
    更新单个基金的持仓数据
    
    Args:
        db: 数据库会话
        fund_code: 基金代码
        year: 年份
        commit: 是否立即提交事务，默认True
    """
    try:
        # 使用akshare获取基金持仓数据
        fund_portfolio_hold_em_df = ak.fund_portfolio_hold_em(symbol=fund_code, date=str(year))
        
        if fund_portfolio_hold_em_df.empty:
            logger.warning(f"基金 {fund_code} 无持仓数据")
            return
        
        # 遍历数据，插入或更新数据库
        for _, row in fund_portfolio_hold_em_df.iterrows():
            weight = row['占净值比例']
            market_value = row['持仓市值']
            stock_name = row['股票名称']
            if pd.isna(weight):
                weight = 0.0
            if pd.isna(market_value):
                market_value = 0.0
            if pd.isna(stock_name):
                stock_name = ''
            # 检查是否已存在相同记录
            existing = db.query(FundPortfolioHold).filter(
                FundPortfolioHold.fund_code == fund_code,
                FundPortfolioHold.stock_code == row['股票代码'],
                FundPortfolioHold.quarter == row['季度']
            ).first()
            
            if existing:
                # 更新现有记录
                existing.stock_name = stock_name
                existing.weight = weight
                existing.share = row['持股数']
                existing.market_value = market_value
            else:
                # 插入新记录
                portfolio_hold = FundPortfolioHold(
                    fund_code=fund_code,
                    stock_code=row['股票代码'],
                    stock_name=stock_name,
                    weight=weight,
                    share=row['持股数'],
                    market_value=market_value,
                    quarter=row['季度']
                )
                db.add(portfolio_hold)
        
        # 根据参数决定是否立即提交事务
        if commit:
            db.commit()
        
    except Exception as e:
        if commit:
            db.rollback()
        logger.error(f"更新基金 {fund_code} 持仓数据失败: {str(e)}")
        raise
