#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
归因服务模块
调度回归算法，并将结果存回数据库
"""

import logging
from datetime import datetime, timedelta
from typing import Dict, List, Optional
import pandas as pd
import numpy as np
from sqlalchemy.orm import Session

from database.init_db import SessionLocal
from models.fund_nav_history import FundNavHistory
from models.fund_index_mapping import FundIndexMapping
from models.index_daily_quote import IndexDailyQuote
from core.attribution_analyzer import AttributionAnalyzer

from collections import defaultdict
from concurrent.futures import ThreadPoolExecutor, as_completed

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class AttributionService:
    """
    归因服务类，负责调度回归算法，并将结果存回数据库
    """
    
    def __init__(self):
        self.analyzer = AttributionAnalyzer(SessionLocal())
    
    def init_analyzer(self):
        """初始化归因分析器"""
        self.analyzer.cold_start()

    def run_attribution_for_all_funds(self):
        start_time = datetime.now()
        logger.info("开始归因分析（优化版）...")

        db = SessionLocal()

        try:
            # =========================
            # 1️⃣ 一次性加载 mappings
            # =========================
            mappings = db.query(FundIndexMapping).all()

            fund_mappings = defaultdict(list)
            index_set = set()

            for m in mappings:
                fund_mappings[m.fund_code].append(m)
                index_set.add(m.index_code)

            fund_codes = list(fund_mappings.keys())

            logger.info(f"基金数: {len(fund_codes)}")

            # =========================
            # 2️⃣ 一次性加载 NAV
            # =========================
            end_date = datetime.now()
            start_date = end_date - timedelta(days=90)

            nav_data = db.query(FundNavHistory).filter(
                FundNavHistory.fund_code.in_(fund_codes),
                FundNavHistory.date >= start_date,
                FundNavHistory.date <= end_date
            ).all()

            # 分组 NAV
            nav_map = defaultdict(list)
            for n in nav_data:
                nav_map[n.fund_code].append((n.date, n.nav))

            # =========================
            # 3️⃣ 一次性加载指数行情（关键优化）
            # =========================
            index_data = db.query(IndexDailyQuote).filter(
                IndexDailyQuote.index_code.in_(list(index_set)),
                IndexDailyQuote.date >= start_date,
                IndexDailyQuote.date <= end_date
            ).all()

            index_map = defaultdict(list)
            for i in index_data:
                index_map[i.index_code].append((i.date, i.close))

            # =========================
            # 4️⃣ 并行归因
            # =========================

            def process_fund(fund_code):

                try:
                    nav_list = nav_map.get(fund_code, [])
                    if not nav_list:
                        return False

                    df = pd.DataFrame(nav_list, columns=["date", "nav"])
                    df.set_index("date", inplace=True)

                    fund_returns = df["nav"].pct_change().dropna() * 100
                    if fund_returns.empty:
                        return False

                    factor_df = pd.DataFrame(index=fund_returns.index)

                    for m in fund_mappings[fund_code]:
                        idx = m.index_code
                        idx_list = index_map.get(idx, [])

                        if not idx_list:
                            continue

                        idx_df = pd.DataFrame(idx_list, columns=["date", "close"])
                        idx_df.set_index("date", inplace=True)

                        returns = idx_df["close"].pct_change().dropna() * 100
                        factor_df[idx] = returns

                    factor_df = factor_df.fillna(0)

                    if factor_df.empty:
                        return False

                    betas = self.analyzer.calculate_betas(
                        fund_code, fund_returns, factor_df
                    )

                    if not betas:
                        return False

                    # 更新 DB
                    for m in fund_mappings[fund_code]:
                        if m.index_code in betas:
                            w = max(0, min(1, betas[m.index_code]))
                            m.effective_weight = w
                            m.last_update = datetime.now()

                    return True

                except Exception as e:
                    logger.error(f"{fund_code} 失败: {e}")
                    return False

            success = 0

            with ThreadPoolExecutor(max_workers=8) as executor:
                futures = [executor.submit(process_fund, code) for code in fund_codes]

                for f in as_completed(futures):
                    if f.result():
                        success += 1

            db.commit()

            logger.info(f"完成，成功 {success}/{len(fund_codes)}")
            logger.info(f"耗时: {(datetime.now()-start_time).total_seconds():.2f}s")

        finally:
            db.close()

# 全局归因服务实例
attribution_service = AttributionService()