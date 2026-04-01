#!/usr/bin/env python3
# -*- coding:utf-8 -*-

from typing import List
from sqlalchemy import func
import pandas as pd

from models.fund_portfolio_hold import FundPortfolioHold
from models.fund_index_mapping import FundIndexMapping
from models.fund_asset_allocation import FundAssetAllocation
from models.stock_daily_quote import StockDailyQuote
from models.index_daily_quote import IndexDailyQuote
from models.fund_nav_history import FundNavHistory


def batch_query_holds(db, fund_codes: List[str] = None):
    results = []
    batch_size = 1000
    for i in range(0, len(fund_codes), batch_size):
        batch = fund_codes[i : i + batch_size]
        latest_quarters_subq = db.query(
            FundPortfolioHold.fund_code,
            func.max(FundPortfolioHold.quarter).label('max_q')
        ).filter(FundPortfolioHold.fund_code.in_(batch)).group_by(FundPortfolioHold.fund_code).subquery()

        batch_data = db.query(
            FundPortfolioHold.fund_code, FundPortfolioHold.stock_code, FundPortfolioHold.weight
        ).join(
            latest_quarters_subq,
            (FundPortfolioHold.fund_code == latest_quarters_subq.c.fund_code) & 
            (FundPortfolioHold.quarter == latest_quarters_subq.c.max_q)
        ).all()
        results.extend(batch_data)

    return results

def batch_query_allocs(db, fund_codes: List[str] = None):
    results = []
    batch_size = 1000
    for i in range(0, len(fund_codes), batch_size):
        batch = fund_codes[i : i + batch_size]
        batch_data = db.query(FundAssetAllocation).filter(
            FundAssetAllocation.fund_code.in_(batch)
        ).all()
        results.extend(batch_data)

    return results

def batch_query_mappings(db, fund_codes: List[str] = None):
    results = []
    batch_size = 1000
    for i in range(0, len(fund_codes), batch_size):
        batch = fund_codes[i : i + batch_size]
        batch_data = db.query(FundIndexMapping).filter(
            FundIndexMapping.fund_code.in_(batch)
        ).all()
        results.extend(batch_data)

    return results


def batch_query_stock_quotes(db, stock_codes: List[str], start_date, end_date):
    """批量查询股票行情数据并返回收益率DataFrame"""
    all_stock_data = []
    batch_size = 500
    for i in range(0, len(stock_codes), batch_size):
        batch_stocks = stock_codes[i:i+batch_size]
        query = db.query(StockDailyQuote.date, StockDailyQuote.stock_code, StockDailyQuote.adj_close).filter(
            StockDailyQuote.stock_code.in_(batch_stocks),
            StockDailyQuote.date >= start_date,
            StockDailyQuote.date <= end_date
        ).all()
        all_stock_data.extend(query)
    
    stock_returns = pd.DataFrame()
    if all_stock_data:
        df = pd.DataFrame(all_stock_data, columns=['date', 'stock_code', 'close'])
        pivot_df = df.pivot(index='date', columns='stock_code', values='close')
        stock_returns = pivot_df.pct_change().dropna(how='all').fillna(0)
    
    return stock_returns


def batch_query_index_returns(db, index_codes: List[str], start_date, end_date):
    """批量查询指数行情数据并返回收益率DataFrame"""
    query = db.query(IndexDailyQuote.date, IndexDailyQuote.index_code, IndexDailyQuote.close).filter(
        IndexDailyQuote.index_code.in_(index_codes),
        IndexDailyQuote.date >= start_date,
        IndexDailyQuote.date <= end_date
    ).all()
    
    if not query:
        return pd.DataFrame()
        
    df = pd.DataFrame(query, columns=['date', 'symbol', 'close'])
    pivot_df = df.pivot(index='date', columns='symbol', values='close')
    # 计算收益率并丢弃全空行
    returns = pivot_df.pct_change().dropna(how='all')
    # 填充少量缺失值（停牌等）
    returns = returns.fillna(0)
    return returns


def batch_query_fund_returns(db, fund_codes: List[str], start_date, end_date):
    """批量查询基金净值数据并返回收益率DataFrame"""
    # 分批查询以防 SQL IN 过长
    batch_size = 500
    all_data = []
    for i in range(0, len(fund_codes), batch_size):
        batch_codes = fund_codes[i:i+batch_size]
        query = db.query(FundNavHistory.date, FundNavHistory.fund_code, FundNavHistory.nav).filter(
            FundNavHistory.fund_code.in_(batch_codes),
            FundNavHistory.date >= start_date,
            FundNavHistory.date <= end_date
        ).all()
        all_data.extend(query)
        
    if not all_data:
        return pd.DataFrame()

    df = pd.DataFrame(all_data, columns=['date', 'fund_code', 'nav'])
    pivot_df = df.pivot(index='date', columns='fund_code', values='nav')
    return pivot_df.pct_change().dropna(how='all')
