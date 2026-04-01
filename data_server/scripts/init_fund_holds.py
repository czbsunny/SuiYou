#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
基金持仓数据初始化模块
手动执行一次，初始化2025年的基金持仓数据
"""

import sys
import os

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

import logging
import time
import akshare as ak
from datetime import datetime
from sqlalchemy.orm import Session
import pandas as pd

from models.fund import Fund
from models.fund_portfolio_hold import FundPortfolioHold
from database.init_db import SessionLocal, init_db

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
    # 先获取所有有效基金的代码和类型
    query = db.query(Fund.fund_code, Fund.fund_type).filter(
        Fund.is_valid_fund == True
    )
    
    # 执行查询
    funds = query.all()
    
    # 定义需要排除的基金类型
    exclude_types = {
        '货币型-普通货币', '货币型-浮动净值', '货币型',
        '混合型-绝对收益', 'Reits', '其他', '商品',
        '债券型-中短债', '债券型-长债',
        '债券型-混合一级', '债券型-混合二级',
        '指数型-海外股票', '指数型-固收', '指数型-其他',
        'FOF-稳健型', 'FOF-进取型', 'FOF-均衡型',
        'QDII-REITs','QDII-FOF','QDII-商品','QDII-纯债','QDII-混合债'
    }
    
    # 在内存中过滤
    valid_funds = []
    for fund in funds:
        fund_code, fund_type = fund
        if fund_type not in exclude_types:
            valid_funds.append(fund_code)
    
    # 返回基金代码列表
    return valid_funds

def get_updated_fund_codes(db: Session, year: int) -> set:
    """
    获取已经更新过指定年份的基金代码
    """
    # 查询已更新过该年份的基金代码
    updated_funds = db.query(FundPortfolioHold.fund_code).filter(
        FundPortfolioHold.quarter.like(f"{year}%"
    )).all()
    
    # 返回基金代码集合
    return {fund[0] for fund in updated_funds}

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
        # 使用akshare获取基金持仓数据，添加超时处理
        import timeout_decorator
        
        @timeout_decorator.timeout(30)
        def fetch_fund_holdings(code, y):
            return ak.fund_portfolio_hold_em(symbol=code, date=str(y))
        
        fund_portfolio_hold_em_df = fetch_fund_holdings(fund_code, year)
        
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

def init_fund_holds(year: int = 2025):
    """
    初始化基金持仓数据
    
    Args:
        year: 年份，默认2025
    """
    logger.info(f"开始初始化{year}年基金持仓数据")
    
    # 初始化数据库表
    init_db()
    
    db = SessionLocal()
    try:
        # 获取所有有效的非货币型基金代码
        valid_fund_codes = get_valid_funds_map(db)
        
        # 获取已经更新过该年份的基金代码
        updated_fund_codes = get_updated_fund_codes(db, year)
        
        # 过滤出需要更新的基金代码
        fund_codes_to_update = [code for code in valid_fund_codes if code not in updated_fund_codes]
        
        logger.info(f"共获取到 {len(valid_fund_codes)} 只有效的非货币型基金")
        logger.info(f"其中 {len(updated_fund_codes)} 只已经更新过{year}年，{len(fund_codes_to_update)} 只需要更新")
        
        # 计数器
        success_count = 0
        fail_count = 0
        total_count = len(fund_codes_to_update)
        batch_size = 100
        
        # 遍历基金代码，更新持仓数据
        for index, fund_code in enumerate(fund_codes_to_update, 1):
            try:
                update_fund_portfolio_hold(db, fund_code, year, commit=False)
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
        
        logger.info(f"基金持仓数据初始化完成，成功: {success_count}, 失败: {fail_count}")
        
    except Exception as e:
        db.rollback()
        logger.error(f"初始化基金持仓数据失败: {str(e)}")
    finally:
        db.close()

if __name__ == "__main__":
    # 初始化2025年的基金持仓数据
    init_fund_holds(2025)