#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
市场数据全量初始化脚本
用途：初始化申万行业分类信息、抓取指数行情、抓取基金重仓股行情。
为后续的【归因分析】和【实时估值】准备底层数据。
"""

import sys
import os
import logging
import time
from datetime import datetime, timedelta

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

import akshare as ak
import pandas as pd
from sqlalchemy.orm import sessionmaker

# 请确保这些导入路径与你的项目结构一致
from database.init_db import init_db, get_db
from models.fund import Fund
from models.sw_index_info import SwIndexInfo
from models.fund_nav_history import FundNavHistory
from models.fund_portfolio_hold import FundPortfolioHold
from models.index_daily_quote import IndexDailyQuote
from models.stock_daily_quote import StockDailyQuote
from services.audit_service import AuditService
from utils.helpers import standardize_symbol, is_cdr
from sqlalchemy import func
from datafetch.fund_fetcher import FundFetcher
import asyncio

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class QuoteService:
    """内部市场行情同步服务"""
    
    def __init__(self, db_session):
        self.db = db_session
        self.fetcher = FundFetcher()

    def init_sw_index_metadata(self):
        """1. 初始化申万行业指数基本信息 (一、二、三级)"""
        logger.info("========== 步骤 1: 初始化申万行业指数元数据 ==========")
        try:
            # 1.1 一级行业
            logger.info("获取申万 1 级行业指数信息...")
            df_first = ak.sw_index_first_info()
            for _, row in df_first.iterrows():
                self._upsert_sw_info(row, 1, None)
            
            # 1.2 二级行业
            logger.info("获取申万 2 级行业指数信息...")
            df_second = ak.sw_index_second_info()
            for _, row in df_second.iterrows():
                self._upsert_sw_info(row, 2, row.get('上级行业'))
                
            # 1.3 三级行业
            logger.info("获取申万 3 级行业指数信息...")
            df_third = ak.sw_index_third_info()
            for _, row in df_third.iterrows():
                self._upsert_sw_info(row, 3, row.get('上级行业'))

            self.db.commit()
            logger.info("✅ 申万行业指数元数据初始化完成！")
        except Exception as e:
            self.db.rollback()
            logger.error(f"❌ 申万元数据初始化失败: {e}")

    def _upsert_sw_info(self, row, level, parent):
        """辅助方法：插入或更新申万指数元数据"""
        industry_code = row['行业代码']
        existing = self.db.query(SwIndexInfo).filter_by(industry_code=industry_code).first()
        
        if not existing:
            sw_info = SwIndexInfo(
                industry_code=industry_code,
                industry_name=row['行业名称'],
                parent_industry=parent,
                industry_level=level,
                component_count=row.get('成份个数'),
                static_pe=row.get('静态市盈率'),
                ttm_pe=row.get('TTM(滚动)市盈率'),
                pb=row.get('市净率'),
                static_dividend_yield=row.get('静态股息率')
            )
            self.db.add(sw_info)

    def sync_sw_indices_quotes(self, days=90):
        """
        2. 同步申万 二级 行业指数历史行情
        注意：归因分析仅需要二级行业行情数据
        """
        logger.info(f"========== 步骤 2: 同步申万行业指数历史行情 (近{days}天) ==========")
        end_date = datetime.now().strftime("%Y%m%d")
        start_date = (datetime.now() - timedelta(days=days)).strftime("%Y%m%d")
        
        # 我们需要同步二级行业  
        levels_to_sync =[("二级行业", 2)]
        total_success = 0

        for symbol_name, level_num in levels_to_sync:
            logger.info(f"开始拉取 [{symbol_name}] 行情数据...")
            
            # 获取数据库中允许的指数代码集合，防止塞入垃圾数据
            valid_indices = {
                standardize_symbol(info.industry_code, "index") 
                for info in self.db.query(SwIndexInfo).filter(SwIndexInfo.industry_level == level_num).all()
            }
            
            try:
                df = ak.index_analysis_daily_sw(symbol=symbol_name, start_date=start_date, end_date=end_date)
                if df.empty:
                    continue

                count = 0
                for _, row in df.iterrows():
                    raw_code = str(row['指数代码'])
                    std_code = standardize_symbol(raw_code, 'index')
                    
                    if std_code not in valid_indices:
                        continue

                    dt = pd.to_datetime(row['发布日期']).date()
                    close_val = float(row['收盘指数'])
                    change_pct = float(row['涨跌幅']) / 100  # 转为小数

                    # Upsert
                    existing = self.db.query(IndexDailyQuote).filter_by(index_code=std_code, date=dt).first()
                    if existing:
                        existing.close = close_val
                        existing.change_pct = change_pct
                    else:
                        self.db.add(IndexDailyQuote(
                            index_code=std_code, date=dt,
                            close=close_val, change_pct=change_pct
                        ))
                    
                    count += 1
                    if count % 500 == 0:
                        self.db.commit()

                self.db.commit()
                total_success += len(df['指数代码'].unique())
                logger.info(f"✅ [{symbol_name}] 行情入库成功，更新了 {count} 条记录")

            except Exception as e:
                self.db.rollback()
                logger.error(f"❌ 拉取 [{symbol_name}] 行情失败: {e}")

        return total_success

    def sync_fund_heavy_stocks_quotes(self, days=90):
        """
        3. 同步基金前十大重仓股的历史行情 (用于归因分析计算残差)
        """
        logger.info(f"========== 步骤 3: 同步基金重仓股历史行情 (近{days}天) ==========")
        
        # 1. 构建子查询：按 fund_code 分组，求出每只基金自己最新的 quarter
        latest_quarters_subq = self.db.query(
            FundPortfolioHold.fund_code,
            func.max(FundPortfolioHold.quarter).label('max_q')
        ).group_by(FundPortfolioHold.fund_code).subquery()

        # 2. 主查询：将原表与子查询进行 INNER JOIN，只取出对应最新季度的股票代码
        latest_stocks_query = self.db.query(FundPortfolioHold.stock_code).join(
            latest_quarters_subq,
            (FundPortfolioHold.fund_code == latest_quarters_subq.c.fund_code) & 
            (FundPortfolioHold.quarter == latest_quarters_subq.c.max_q)
        ).distinct()

        # 3. 执行查询并存入 Set 中
        all_stock_codes = {r.stock_code for r in latest_stocks_query.all()}
        
        end_date = datetime.now()
        start_date = end_date - timedelta(days=days)
        
        stocks_to_sync =[]
        # 筛选缺失数据的股票，避免重复全量抓取
        for stock_code in all_stock_codes:
            std_code = standardize_symbol(stock_code, 'stock')
            record_count = self.db.query(StockDailyQuote).filter(
                StockDailyQuote.stock_code == std_code,
                StockDailyQuote.date >= start_date.date(),
                StockDailyQuote.date <= end_date.date()
            ).count()
            
            # 交易日通常为 days 的 70% 左右(除去周末节假日)
            if record_count < (days * 0.6):
                stocks_to_sync.append(std_code)
        
        logger.info(f"共有 {len(all_stock_codes)} 只重仓股，其中 {len(stocks_to_sync)} 只需补充行情数据")
        
        end_str = end_date.strftime("%Y%m%d")
        start_str = start_date.strftime("%Y%m%d")

        for i, std_code in enumerate(stocks_to_sync):
            try:
                if i % 10 == 0:
                    logger.info(f"进度: {i}/{len(stocks_to_sync)}...")
                    
                if std_code.startswith(('sh.', 'sz.', 'bj.')):
                    df = self._fetch_a_share(std_code, start_str, end_str)
                elif std_code.startswith('hk.'):
                    df = self._fetch_hk_share(std_code, start_str, end_str)
                elif std_code.startswith('us.'):
                    df = self._fetch_us_share(std_code, start_str, end_str)
                else:
                    continue

                if df is not None and not df.empty:
                    self._save_stock_to_db(std_code, df)
                    
                time.sleep(0.3) # 跨市场抓取保护延迟

            except Exception as e:
                logger.error(f"同步股票 {std_code} 异常: {e}")
                
        logger.info("✅ 股票行情同步完成！")

    def sync_fund_nav_history(self, days=90):
        """
        4. 同步基金历史净值 (使用 get_fund_latest_nav 接口)
        """
        logger.info(f"========== 步骤 4: 同步基金历史净值 (近{days}天) ==========")
        
        # 1. 获取所有有效的非货币型基金
        valid_funds = self.db.query(Fund).filter(
            Fund.fund_type.notin_(['货币型-普通货币', '货币型-浮动净值', '货币型']),
            Fund.is_valid_fund == True
        ).all()
        
        total_funds = len(valid_funds)
        logger.info(f"共有 {total_funds} 只有效基金需要同步")

        # 定义每批并发的数量 (建议 10-20，防止被东方财富封 IP)
        batch_size = 20 
        # 使用异步包装器处理批量逻辑
        async def process_all():
            for i in range(0, total_funds, batch_size):
                batch_funds = valid_funds[i : i + batch_size]
                tasks = [self._sync_single_fund_nav(f, days) for f in batch_funds]
                await asyncio.gather(*tasks)
        
                # 每一批次完成后提交一次数据库，并打印进度
                self.db.commit()
                logger.info(f"进度: {min(i + batch_size, total_funds)}/{total_funds} 完成")
                # 批次间微小延迟
                await asyncio.sleep(0.5)

        # 运行异步任务
        asyncio.run(process_all())
        logger.info("✅ 所有基金净值同步完成！")

    async def _sync_single_fund_nav(self, fund: Fund, days: int):
        """
        内部异步方法：处理单只基金的抓取与入库
        """
        fund_code = fund.fund_code
        try:
            nav_data = await self.fetcher.get_fund_latest_nav(fund_code, period=days)
            
            if not nav_data:
                return

            existing_records = self.db.query(FundNavHistory).filter(
                FundNavHistory.fund_code == fund_code,
                FundNavHistory.date >= (datetime.now() - timedelta(days=days+7)).date()
            ).all()
            date_map = {r.date: r for r in existing_records}

            latest_dt = None
            latest_nav_val = None
            latest_growth_val = None

            for date_str, values in nav_data.items():
                # values 格式: [nav, daily_growth, accumulated_nav, None]
                dt = datetime.strptime(date_str, '%Y-%m-%d').date()
                
                nav = values[0]
                growth = values[1] / 100 if values[1] is not None else None
                acc_nav = values[2]

                if dt in date_map:
                    # 更新
                    rec = date_map[dt]
                    rec.nav = nav
                    rec.daily_growth_rate = growth
                    rec.accumulated_nav = acc_nav
                else:
                    # 插入
                    new_rec = FundNavHistory(
                        fund_code=fund_code,
                        date=dt,
                        nav=nav,
                        daily_growth_rate=growth,
                        accumulated_nav=acc_nav
                    )
                    self.db.add(new_rec)
                
                # 追踪最新的一条数据更新到 Fund 表
                if not latest_dt or dt > latest_dt:
                    latest_dt = dt
                    latest_nav_val = nav
                    latest_growth_val = growth

            # 更新 Fund 主表快照
            if latest_dt:
                fund.latest_nav = latest_nav_val
                fund.latest_nav_date = latest_dt
                fund.daily_growth = latest_growth_val
                fund.nav_updated_at = datetime.now()

        except Exception as e:
            logger.error(f"处理基金 {fund_code} 异常: {str(e)}")

    # ==========================
    # 抓取工具方法
    # ==========================
    def _fetch_a_share(self, std_code, start_date, end_date):
        pure_code = std_code.split('.')[1]
        start_date_obj = datetime.strptime(start_date, "%Y%m%d")
        # 往前推60天防停牌
        ext_start = (start_date_obj - timedelta(days=60)).strftime("%Y%m%d")
        
        try:
            if is_cdr(pure_code) or pure_code == "689009":
                df = ak.stock_zh_a_cdr_daily(symbol=pure_code, start_date=ext_start, end_date=end_date)
            else:
                df = ak.stock_zh_a_daily(symbol=pure_code, start_date=ext_start, end_date=end_date, adjust="qfq")
                
            return self._clean_stock_df(df, start_date, end_date)
        except:
            return None

    def _fetch_hk_share(self, std_code, start_date, end_date):
        pure_code = std_code.split('.')[1]
        try:
            df = ak.stock_hk_daily(symbol=pure_code, adjust="qfq")
            return self._clean_stock_df(df, start_date, end_date)
        except:
            return None

    def _fetch_us_share(self, std_code, start_date, end_date):
        pure_code = std_code.split('.')[1].upper()
        try:
            df = ak.stock_us_daily(symbol=pure_code, adjust="qfq")
            return self._clean_stock_df(df, start_date, end_date)
        except:
            return None

    def _clean_stock_df(self, df, start_date, end_date):
        if df.empty: return df
        df.rename(columns={'date': '日期', 'close': '收盘'}, inplace=True)
        df['涨跌幅'] = df['收盘'].pct_change() * 100
        df['日期'] = pd.to_datetime(df['日期'])
        df = df[(df['日期'] >= pd.to_datetime(start_date)) & (df['日期'] <= pd.to_datetime(end_date))]
        return df[['日期', '收盘', '涨跌幅']]

    def _save_stock_to_db(self, std_code, df):
        for _, row in df.iterrows():
            dt = row['日期'].date()
            change_pct = row.get('涨跌幅', 0)
            rtn = 0.0 if pd.isna(change_pct) else float(change_pct) / 100
            
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


def init_loss_data(db):
    """(可选) 初始化基金损失/审计数据"""
    logger.info("========== 步骤 4 (可选): 初始化基金审计数据 ==========")
    try:
        service = AuditService(db)
        all_fund_codes = {r.fund_code for r in db.query(FundPortfolioHold.fund_code).filter(FundPortfolioHold.quarter.like('2025年4季度%')).distinct()}
        service.generate_historical_audit(all_fund_codes, days=60)
        logger.info("✅ 基金审计数据初始化完成")
    except Exception as e:
        logger.error(f"❌ 初始化审计数据失败: {e}")


def main():
    """主执行入口"""
    print("\n" + "="*50)
    print("🚀 开始执行系统底层数据初始化 (冷启动)")
    print("="*50 + "\n")
    
    # 1. 初始化数据库表结构
    init_db()
    
    # 2. 获取数据库 Session
    db = next(get_db())
    
    try:
        sync_service = QuoteService(db)
        
        # 执行流 1：初始化行业定义
        sync_service.init_sw_index_metadata()
        
        # 执行流 2：拉取行业指数行情 (设为90天，满足LassoCV样本量)
        sync_service.sync_sw_indices_quotes(days=90)
        
        # 执行流 3：拉取重仓股行情 (设为90天，对齐指数)
        sync_service.sync_fund_heavy_stocks_quotes(days=90)
        
        # 执行流 4：拉取基金净值行情 (90天)
        sync_service.sync_fund_nav_history(days=90)
        
        print("\n" + "="*50)
        print("🎉 所有底层数据初始化完毕！")
        print("➡️ 接下来，你可以安全地运行归因分析脚本 (AttributionAnalyzer.cold_start) 了！")
        print("="*50 + "\n")
        
    except Exception as e:
        db.rollback()
        logger.error(f"初始化过程中发生严重异常: {e}")
    finally:
        # 确保释放连接
        db.close()

if __name__ == "__main__":
    main()