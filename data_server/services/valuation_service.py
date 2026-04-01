#!/usr/bin/env python3
# -*- coding:utf-8 -*-

import logging
import os
from datetime import datetime
from typing import Dict, List, Optional
from collections import defaultdict
from concurrent.futures import ThreadPoolExecutor, as_completed

import pandas as pd
from sqlalchemy.orm import func

from database.init_db import SessionLocal
from models.fund import Fund
from models.fund_portfolio_hold import FundPortfolioHold
from models.fund_index_mapping import FundIndexMapping
from models.fund_asset_allocation import FundAssetAllocation
from core.strategies import StrategyFactory
from core.valuation_engine import ValuationEngine

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class ValuationContext:
    def __init__(self, holds, allocs, mappings):
        self.holds = holds
        self.allocs = allocs
        self.mappings = mappings

    def get_top10(self, code):
        return self.holds.get(code, [])

    def get_total_ratio(self, code):
        return self.allocs.get(code, {}).get('股票', 0.0)

    def get_mappings(self, code):
        return self.mappings.get(code, [])


class ValuationService:

    def __init__(self):
        self.engine = ValuationEngine()
        self.strategy_factory = StrategyFactory()

        # ✅ 缓存目录
        self.cache_dir = "./matrix_cache"
        os.makedirs(self.cache_dir, exist_ok=True)

    # ========================
    # 🚀 核心：重建矩阵（优化版）
    # ========================
    def rebuild_matrix(self, fund_codes: Optional[List[str]] = None) -> Dict[str, pd.DataFrame]:
        logger.info("开始重建权重矩阵（优化版）...")

        db = SessionLocal()
        matrices = {}

        try:
            # 1️⃣ 基金列表
            if not fund_codes:
                fund_codes = [f.fund_code for f in db.query(Fund.fund_code).filter(Fund.is_valid_fund == True).all()]

            logger.info(f"基金数量: {len(fund_codes)}")

            # =========================
            # 2️⃣ 批量查询（关键优化）
            # =========================
            all_holds = self.batch_query_holds(db, fund_codes)
            logger.info(f"持仓数据查询完成: {len(all_holds)} 条")

            all_allocs = self.batch_query_allocs(db, fund_codes)
            logger.info(f"资产配置查询完成: {len(all_allocs)} 条")

            all_mappings = self.batch_query_mappings(db, fund_codes)
            logger.info(f"指数映射查询完成: {len(all_mappings)} 条")

            # =========================
            # 3️⃣ 内存分组
            # =========================

            holds_map = defaultdict(list)
            for h in all_holds:
                holds_map[h.fund_code].append({
                    "stock_code": h.stock_code,
                    "weight": h.weight
                })

            allocs_map = defaultdict(dict)
            for a in all_allocs:
                allocs_map[a.fund_code][a.asset_type] = float(a.allocation_ratio)

            mappings_map = defaultdict(list)
            for m in all_mappings:
                mappings_map[m.fund_code].append({
                    "index_code": m.index_code,
                    "effective_weight": float(m.effective_weight)
                })

            context = ValuationContext(holds_map, allocs_map, mappings_map)

            # =========================
            # 4️⃣ 并行计算
            # =========================

            def process_fund(fund_code):
                try:
                    holds = holds_map.get(fund_code, [])
                    mappings = mappings_map.get(fund_code, [])

                    # ✅ 不查数据库，直接判断类型
                    if mappings and not holds:
                        fund_type = 'index'
                    elif holds and not mappings:
                        fund_type = 'active'
                    elif mappings and holds:
                        fund_type = 'mixed'
                    else:
                        fund_type = 'active'

                    strategy = self.strategy_factory.get_strategy(fund_type)
                    matrix = strategy.get_weights_matrix(fund_code, context)

                    if not matrix.empty:
                        return fund_code, matrix

                except Exception as e:
                    logger.error(f"{fund_code} 处理失败: {e}")

                return fund_code, None

            # 并行执行
            total_funds = len(fund_codes)
            completed = 0
            success_count = 0
            logger.info(f"开始并行处理 {total_funds} 个基金，使用 4 个工作线程...")

            with ThreadPoolExecutor(max_workers=2) as executor:
                futures = [executor.submit(process_fund, code) for code in fund_codes]
                
                for future in as_completed(futures):
                    fund_code, matrix = future.result()
                    completed += 1
                    if matrix is not None:
                        matrices[fund_code] = matrix
                        self.engine.update_weights(fund_code, matrix)
                        success_count += 1
                        self._save_matrix(fund_code, matrix)

                    if completed % max(1, total_funds // 10) == 0 or completed == total_funds:
                        progress = completed / total_funds * 100
                        logger.info(f"进度: {completed}/{total_funds} ({progress:.1f}%) - 成功: {success_count}")

            logger.info(f"并行处理完成，成功: {success_count}/{total_funds}")
            return matrices

        finally:
            db.close()

    # ========================
    # 💾 缓存存储
    # ========================
    def _save_matrix(self, fund_code: str, matrix: pd.DataFrame):
        try:
            path = os.path.join(self.cache_dir, f"{fund_code}.parquet")
            matrix.to_parquet(path)
        except Exception as e:
            logger.error(f"保存缓存失败 {fund_code}: {e}")

    def load_from_cache(self):
        logger.info("加载本地缓存 matrix...")

        count = 0

        for file in os.listdir(self.cache_dir):
            if not file.endswith(".parquet"):
                continue

            fund_code = file.replace(".parquet", "")
            path = os.path.join(self.cache_dir, file)

            try:
                matrix = pd.read_parquet(path)
                self.engine.update_weights(fund_code, matrix)
                count += 1
            except Exception as e:
                logger.error(f"加载失败 {fund_code}: {e}")

        logger.info(f"加载完成，共 {count} 只基金")

    # ========================
    # 🚀 warmup（秒级）
    # ========================
    def warmup(self):
        logger.info("开始预热估值服务...")

        # 1️⃣ 优先加载缓存
        self.load_from_cache()

        # 2️⃣ 如果没有缓存才重建
        if not self.engine.weights_matrices:
            logger.warning("缓存为空，开始重建...")
            self.rebuild_matrix()

        logger.info("预热完成")

    # ========================
    # 实时估值
    # ========================
    def update_market_data(self, market_data: Dict[str, float]) -> Dict[str, float]:
        self.engine.update_market_data(market_data)
        return self.engine.calculate_all()

    # ========================
    # 🚀 加载数据（秒级）
    # ========================
    def batch_query_holds(self, db: SessionLocal, fund_codes: List[str] = None):
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

    def batch_query_allocs(self, db: SessionLocal, fund_codes: List[str] = None):
        results = []
        batch_size = 1000
        for i in range(0, len(fund_codes), batch_size):
            batch = fund_codes[i : i + batch_size]
            batch_data = db.query(FundAssetAllocation).filter(
                FundAssetAllocation.fund_code.in_(batch)
            ).all()
            results.extend(batch_data)

        return results

    def batch_query_mappings(self, db: SessionLocal, fund_codes: List[str] = None):
        results = []
        batch_size = 1000
        for i in range(0, len(fund_codes), batch_size):
            batch = fund_codes[i : i + batch_size]
            batch_data = db.query(FundIndexMapping).filter(
                FundIndexMapping.fund_code.in_(batch)
            ).all()
            results.extend(batch_data)

        return results

valuation_service = ValuationService()