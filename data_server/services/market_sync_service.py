#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
市场行情同步服务
"""
import logging
import time
from datetime import datetime, timedelta
import akshare as ak
import pandas as pd

from database.init_db import get_db
from models.index_daily_quote import IndexDailyQuote
from models.stock_daily_quote import StockDailyQuote
from models.fund_portfolio_hold import FundPortfolioHold
from models.sw_index_info import SwIndexInfo

from utils.helpers import standardize_symbol, is_cdr

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class MarketSyncService:
    """
    市场行情同步服务
    """
    
    def __init__(self, db_session=None):
        """
        初始化市场行情同步服务
        
        Args:
            db_session: 数据库会话对象，如果为 None，则创建新会话
        """
        if db_session:
            self.db = db_session
        else:
            # get_db() 是一个生成器函数，需要通过 next() 获取实际的会话对象
            self.db = next(get_db())
    
    def sync_sw_indices(self, days=5):
        """
        一次性同步所有申万一级行业指数行情
        
        Args:
            days: 同步过去多少天的数据
            
        Returns:
            成功同步的指数数量
        """
        # 1. 计算日期范围
        # 接口要求格式 YYYYMMDD
        end_date = datetime.now().strftime("%Y%m%d")
        start_date = (datetime.now() - timedelta(days=days)).strftime("%Y%m%d")
        
        logger.info(f"开始批量同步申万一级行业行情: {start_date} -> {end_date}")
        sw_level1_indices = {standardize_symbol(info.industry_code, "index") for info in self.db.query(SwIndexInfo).filter(SwIndexInfo.industry_level == 1).all()}
        try:
            # 2. 一次性获取全量一级行业数据
            df = ak.index_analysis_daily_sw(symbol="一级行业", start_date=start_date, end_date=end_date)
            
            if df.empty:
                logger.warning("未抓取到申万一级行业行情数据")
                return 0

            # 3. 遍历 DataFrame 处理数据
            count = 0
            success_indices = set()
            
            for _, row in df.iterrows():
                raw_code = str(row['指数代码'])
                
                # 标准化 ID: 801120 -> idx.sw801120
                std_code = standardize_symbol(raw_code, 'index')
                
                # 过滤：只处理我们定义的 31 个一级行业（防止抓到多余的垃圾数据）
                if std_code not in sw_level1_indices:
                    continue

                # 解析日期: 格式通常为 2024-10-25
                dt = pd.to_datetime(row['发布日期']).date()
                
                # 数据清洗
                close_val = float(row['收盘指数'])
                # 注意：AkShare 返回的是百分比数值（如 1.23 表示 1.23%），需转为 0.0123
                change_pct = float(row['涨跌幅']) / 100 

                # 4. 执行数据库 Upsert (有则更新，无则插入)
                existing = self.db.query(IndexDailyQuote).filter(
                    IndexDailyQuote.index_code == std_code,
                    IndexDailyQuote.date == dt
                ).first()

                if existing:
                    existing.close = close_val
                    existing.change_pct = change_pct
                else:
                    new_quote = IndexDailyQuote(
                        index_code=std_code,
                        date=dt,
                        close=close_val,
                        change_pct=change_pct
                    )
                    self.db.add(new_quote)
                
                count += 1
                success_indices.add(raw_code)
                # 每 100 条 commit 一次或最后统一 commit
                if count % 100 == 0:
                    self.db.commit()

            self.db.commit()
            success_count = len(success_indices)
            logger.info(f"批量同步完成，共入库/更新 {count} 条行业行情记录，成功同步 {success_count} 个指数")
            return success_count

        except Exception as e:
            self.db.rollback()
            logger.error(f"批量同步申万行业失败: {e}")
            return 0

    # ==========================
    # 2. 个股同步模块 (针对 权重漂移 & 审计残差)
    # ==========================
    def sync_stocks(self, std_stock_list: list, days=60):
        """
        std_stock_list: 传入带前缀的标准ID列表，如 ['sh.600519', 'hk.00700', 'us.aapl']
        """
        end_date = datetime.now().strftime("%Y%m%d")
        start_date = (datetime.now() - timedelta(days=days)).strftime("%Y%m%d")

        for std_code in std_stock_list:
            try:
                # 1. 自动分流适配
                if std_code.startswith(('sh.', 'sz.', 'bj.')):
                    df = self._fetch_a_share(std_code, start_date, end_date)
                elif std_code.startswith('hk.'):
                    df = self._fetch_hk_share(std_code, start_date, end_date)
                elif std_code.startswith('us.'):
                    df = self._fetch_us_share(std_code, start_date, end_date)
                else:
                    logger.warning(f"未知市场前缀: {std_code}")
                    continue

                if df is None or df.empty: continue

                # 2. 统一写入数据库
                self._save_to_db(std_code, df)
                time.sleep(0.2) # 跨市场抓取建议加延迟

            except Exception as e:
                logger.error(f"同步 {std_code} 异常: {e}")

    # ==========================
    # 市场具体的抓取策略
    # ==========================

    def _fetch_a_share(self, std_code, start_date, end_date):
        """A股/科创板/北交所 统一接口"""
        pure_code = std_code.replace('.', '')
        df = None
        # 将字符串格式的 start_date 转换为 datetime 对象，再进行日期计算
        start_date_obj = datetime.strptime(start_date, "%Y%m%d")
        # 为了处理停牌情况，向前扩展更多天的数据，确保能获取到停牌前的交易日
        extended_start_date = (start_date_obj - timedelta(days=60)).strftime("%Y%m%d")
        if pure_code == "sh689009":
            logger.info(f"cdr 数据特殊处理，使用cdr接口 获取 {pure_code} 数据")
            
        if is_cdr(pure_code):
            df = ak.stock_zh_a_cdr_daily(symbol=pure_code, start_date=extended_start_date, end_date=end_date)
        else:
            df = ak.stock_zh_a_daily(symbol=pure_code, start_date=extended_start_date, end_date=end_date, adjust="qfq")
        if not df.empty:
            # 重命名列
            df.rename(columns={'date': '日期', 'close': '收盘'}, inplace=True)
            # 计算涨跌幅（基于收盘价的变化率）
            df['涨跌幅'] = df['收盘'].pct_change() * 100
            # 筛选日期范围
            df['日期'] = pd.to_datetime(df['日期'])
            start_dt = pd.to_datetime(start_date)
            end_dt = pd.to_datetime(end_date)
            df = df[(df['日期'] >= start_dt) & (df['日期'] <= end_dt)]
            # 只保留需要的列
            df = df[['日期', '收盘', '涨跌幅']]
        return df

    def _fetch_hk_share(self, std_code, start_date, end_date):
        """港股接口"""
        pure_code = std_code.split('.')[1]
        # 港股接口代码通常需要5位，如 00700
        df = ak.stock_hk_daily(symbol=pure_code, adjust="qfq")
        if not df.empty:
            # 重命名列
            df.rename(columns={'date': '日期', 'close': '收盘'}, inplace=True)
            # 计算涨跌幅（基于收盘价的变化率）
            df['涨跌幅'] = df['收盘'].pct_change() * 100
            # 筛选日期范围
            df['日期'] = pd.to_datetime(df['日期'])
            start_dt = pd.to_datetime(start_date)
            end_dt = pd.to_datetime(end_date)
            df = df[(df['日期'] >= start_dt) & (df['日期'] <= end_dt)]
            # 只保留需要的列
            df = df[['日期', '收盘', '涨跌幅']]
        return df

    def _fetch_us_share(self, std_code, start_date, end_date):
        """美股接口"""
        pure_code = std_code.split('.')[1].upper()
        df = ak.stock_us_daily(symbol=pure_code, adjust="qfq")

        if not df.empty:
            # 重命名列
            df.rename(columns={'date': '日期', 'close': '收盘'}, inplace=True)
            # 计算涨跌幅（基于收盘价的变化率）
            df['涨跌幅'] = df['收盘'].pct_change() * 100
            # 筛选日期范围
            df['日期'] = pd.to_datetime(df['日期'])
            start_dt = pd.to_datetime(start_date)
            end_dt = pd.to_datetime(end_date)
            df = df[(df['日期'] >= start_dt) & (df['日期'] <= end_dt)]
            # 只保留需要的列
            df = df[['日期', '收盘', '涨跌幅']]
        return df

    def _save_to_db(self, std_code, df):
        """将不同接口返回的异构 DataFrame 转化为统一模型存入 DB"""
        for _, row in df.iterrows():
            dt = pd.to_datetime(row['日期']).date()
            
            # 处理涨跌幅，确保不会有 NaN 值
            change_pct = row.get('涨跌幅', 0)
            # 检查是否为 NaN，如果是则使用 0
            if pd.isna(change_pct):
                rtn = 0.0
            else:
                rtn = float(change_pct) / 100
            
            existing = self.db.query(StockDailyQuote).filter_by(stock_code=std_code, date=dt).first()
            if existing:
                existing.adj_close = float(row['收盘'])
                existing.change_pct = rtn
            else:
                self.db.add(StockDailyQuote(
                    stock_code=std_code, date=dt,
                    adj_close=float(row['收盘']), change_pct=rtn
                ))
        self.db.commit()

    def cold_start_sync(self):
        """
        冷启动：拉取过去两年的所有一级行业历史数据
        """
        # 1. 指数历史 (60天)
        self.sync_sw_indices(days=60)

        # 2. 从持仓表提取所有重仓股代码并补齐历史 (60天)
        # 首先获取所有不同的股票代码
        all_stock_codes = {r.stock_code for r in self.db.query(FundPortfolioHold.stock_code).filter(FundPortfolioHold.quarter.like('2025年4季度%')).distinct()}
        
        # 计算需要同步的日期范围
        end_date = datetime.now()
        start_date = end_date - timedelta(days=60)
        
        # 过滤出需要同步的股票：数据不完整的股票
        stocks_to_sync = []
        for stock_code in all_stock_codes:
            std_code = standardize_symbol(stock_code, 'stock')
            
            # 查询该股票在日期范围内的记录数
            record_count = self.db.query(StockDailyQuote).filter(
                StockDailyQuote.stock_code == std_code,
                StockDailyQuote.date >= start_date.date(),
                StockDailyQuote.date <= end_date.date()
            ).count()
            
            # 简单判断：如果记录数少于30天（考虑到周末和节假日），就认为数据不完整，需要同步
            if record_count < 30:
                stocks_to_sync.append(std_code)
        
        logger.info(f"冷启动同步股票：共 {len(all_stock_codes)} 只股票，其中 {len(stocks_to_sync)} 只需要同步数据")
        
        # 同步数据不完整的股票
        if stocks_to_sync:
            self.sync_stocks(stocks_to_sync, days=60)
    
    def sync_all_market_data(self, days=5):
        """
        同步所有市场数据
        
        Args:
            days: 同步过去多少天的数据
            
        Returns:
            成功同步的总数量
        """
        logger.info(f"开始同步所有市场数据，过去 {days} 天")
        
        # 同步申万一级行业指数数据
        sw_success = self.sync_sw_indices(days)
        
        # 这里可以添加其他市场数据的同步，例如：
        # - 大盘指数数据
        # - 风格因子数据
        # - 热门赛道数据
        
        logger.info(f"所有市场数据同步完成，成功同步 {sw_success} 个申万一级行业指数")
        return sw_success

# 全局市场行情同步服务实例
market_sync_service = MarketSyncService()
