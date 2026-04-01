#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
估值服务模块
"""

import logging
from datetime import datetime
from typing import Dict, List, Optional
import pandas as pd
from sqlalchemy.orm import Session

from database.init_db import SessionLocal
from models.fund import Fund
from models.fund_portfolio_hold import FundPortfolioHold
from models.fund_index_mapping import FundIndexMapping
from models.fund_asset_allocation import FundAssetAllocation
from core.strategies import StrategyFactory
from core.valuation_engine import ValuationEngine

class ValuationContext:
    """
    估值上下文类，封装基金的持仓、资产配置和指数映射数据
    """
    def __init__(self, holds, allocs, mappings):
        """
        初始化估值上下文
        
        Args:
            holds: 基金持仓数据，格式为 {"fund_code": [{"stock_code": "600519", "weight": 10.0}, ...]}
            allocs: 基金资产配置数据，格式为 {"fund_code": {"股票": 0.65, "债券": 0.25, ...}}
            mappings: 基金指数映射数据，格式为 {"fund_code": [{"index_code": "000300", "effective_weight": 1.0}, ...]}
        """
        self.holds = holds
        self.allocs = allocs
        self.mappings = mappings
    
    def get_top10(self, code):
        """
        获取基金的前十大持仓
        
        Args:
            code: 基金代码
            
        Returns:
            持仓数据列表
        """
        return self.holds.get(code, [])
    
    def get_total_ratio(self, code):
        """
        获取基金的股票资产占比
        
        Args:
            code: 基金代码
            
        Returns:
            股票资产占比
        """
        alloc_dict = self.allocs.get(code, {})
        return alloc_dict.get('股票', 0.0)
    
    def get_mappings(self, code):
        """
        获取基金的指数映射数据
        
        Args:
            code: 基金代码
            
        Returns:
            指数映射数据列表
        """
        return self.mappings.get(code, [])

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class ValuationService:
    """
    估值服务类，负责从数据库加载数据，生成权重矩阵，并更新估值引擎
    """
    
    def __init__(self):
        self.engine = ValuationEngine()
        self.strategy_factory = StrategyFactory()
    
    def rebuild_matrix(self, fund_codes: Optional[List[str]] = None) -> Dict[str, pd.DataFrame]:
        """
        重建权重矩阵
        
        Args:
            fund_codes: 可选的基金代码列表，若为 None 则处理所有基金
            
        Returns:
            包含权重矩阵的字典，键为基金代码，值为权重矩阵
        """
        start_time = datetime.now()
        logger.info(f"[{start_time.strftime('%Y-%m-%d %H:%M:%S')}] 开始重建权重矩阵...")
        
        db = SessionLocal()
        matrices = {}
        
        try:
            # 获取基金列表
            if fund_codes:
                funds = db.query(Fund).filter(Fund.fund_code.in_(fund_codes)).all()
            else:
                funds = db.query(Fund).all()
            
            logger.info(f"获取到 {len(funds)} 只基金")
            
            # 加载所有基金的数据
            holds = {}
            allocs = {}
            mappings = {}
            
            for fund in funds:
                # 加载持仓数据
                holds[fund.fund_code] = self._load_portfolio_data(db, fund.fund_code)
                # 加载资产配置数据（从数据库获取股票资产占比）
                allocs[fund.fund_code] = self._load_asset_allocation_data(db, fund.fund_code)
                # 加载指数映射数据
                mappings[fund.fund_code] = self._load_index_mapping_data(db, fund.fund_code)
            
            # 创建估值上下文
            context = ValuationContext(holds, allocs, mappings)
            
            # 处理每个基金
            for fund in funds:
                try:
                    # 确定基金类型
                    fund_type = self._determine_fund_type(db, fund.fund_code)
                    
                    # 获取对应的策略
                    strategy = self.strategy_factory.get_strategy(fund_type)
                    
                    # 生成权重矩阵
                    matrix = strategy.get_weights_matrix(fund.fund_code, context)
                    if not matrix.empty:
                        matrices[fund.fund_code] = matrix
                        # 更新到估值引擎
                        self.engine.update_weights(fund.fund_code, matrix)
                except Exception as e:
                    logger.error(f"处理基金 {fund.fund_code} 时出错: {str(e)}")
                    continue
            
            end_time = datetime.now()
            duration = (end_time - start_time).total_seconds()
            logger.info(f"[{end_time.strftime('%Y-%m-%d %H:%M:%S')}] 权重矩阵重建完成，耗时: {duration:.2f}秒，成功处理 {len(matrices)} 只基金")
            
            return matrices
            
        except Exception as e:
            logger.error(f"重建权重矩阵失败: {str(e)}")
            return {}
        finally:
            db.close()
    
    def _determine_fund_type(self, db: Session, fund_code: str) -> str:
        """
        确定基金类型
        
        Args:
            db: 数据库会话
            fund_code: 基金代码
            
        Returns:
            基金类型，如 'active', 'index', 'mixed'
        """
        # 检查是否有指数映射
        index_mappings = db.query(FundIndexMapping).filter(
            FundIndexMapping.fund_code == fund_code
        ).all()
        
        # 检查是否有持仓数据
        portfolio_holds = db.query(FundPortfolioHold).filter(
            FundPortfolioHold.fund_code == fund_code
        ).all()
        
        if index_mappings and not portfolio_holds:
            return 'index'
        elif portfolio_holds and not index_mappings:
            return 'active'
        elif index_mappings and portfolio_holds:
            return 'mixed'
        else:
            return 'active'
    
    def _load_portfolio_data(self, db: Session, fund_code: str) -> List[Dict]:
        """
        加载基金持仓数据
        
        Args:
            db: 数据库会话
            fund_code: 基金代码
            
        Returns:
            持仓数据列表，格式为 [{"stock_code": "600519", "weight": 10.0}, ...]
        """
        # 获取最新季度的持仓数据
        holdings = db.query(FundPortfolioHold).filter(
            FundPortfolioHold.fund_code == fund_code
        ).order_by(
            FundPortfolioHold.quarter.desc()
        ).limit(10).all()
        
        if not holdings:
            logger.warning(f"基金 {fund_code} 无持仓数据")
            return []
        
        # 转换为所需格式
        data = []
        for hold in holdings:
            data.append({
                "stock_code": hold.stock_code,
                "weight": hold.weight
            })
        
        return data
    
    def _load_index_mapping_data(self, db: Session, fund_code: str) -> List[Dict]:
        """
        加载基金指数映射数据
        
        Args:
            db: 数据库会话
            fund_code: 基金代码
            
        Returns:
            指数映射数据列表，格式为 [{"index_code": "000300", "effective_weight": 1.0}, ...]
        """
        mappings = db.query(FundIndexMapping).filter(
            FundIndexMapping.fund_code == fund_code
        ).all()
        
        if not mappings:
            logger.warning(f"基金 {fund_code} 无指数映射数据")
            return []
        
        # 转换为所需格式
        data = []
        for mapping in mappings:
            data.append({
                "index_code": mapping.index_code,
                "effective_weight": float(mapping.effective_weight)
            })
        
        return data
    
    def _load_asset_allocation_data(self, db: Session, fund_code: str) -> Dict[str, float]:
        """
        加载基金资产配置数据
        
        Args:
            db: 数据库会话
            fund_code: 基金代码
            
        Returns:
            资产配置字典，格式为 {"股票": 0.65, "债券": 0.25, ...}，若未找到则返回空字典
        """
        try:
            # 查询该基金的所有资产配置
            allocations = db.query(FundAssetAllocation).filter(
                FundAssetAllocation.fund_code == fund_code
            ).all()
            
            if allocations:
                # 转换为字典格式
                result = {}
                for allocation in allocations:
                    result[allocation.asset_type] = float(allocation.allocation_ratio)
                return result
            else:
                logger.warning(f"基金 {fund_code} 无资产配置数据，返回空字典")
                return {}
                
        except Exception as e:
            logger.error(f"加载基金 {fund_code} 资产配置数据失败: {str(e)}")
            return {}
    
    def update_market_data(self, market_data: Dict[str, float]) -> Dict[str, float]:
        """
        更新市场数据并计算基金估值
        
        Args:
            market_data: 市场数据，格式为 {"sh.600519": 0.5, "sz.000001": -0.2, ...}
            
        Returns:
            基金估值结果，格式为 {"000001": 0.3, "000002": -0.1, ...}
        """
        start_time = datetime.now()
        logger.info(f"[{start_time.strftime('%Y-%m-%d %H:%M:%S')}] 开始更新市场数据并计算估值...")
        
        try:
            # 更新市场数据到估值引擎
            self.engine.update_market_data(market_data)
            
            # 计算所有基金的估值
            results = self.engine.calculate_all()
            
            end_time = datetime.now()
            duration = (end_time - start_time).total_seconds()
            logger.info(f"[{end_time.strftime('%Y-%m-%d %H:%M:%S')}] 估值计算完成，耗时: {duration:.2f}秒，成功计算 {len(results)} 只基金")
            
            return results
            
        except Exception as e:
            logger.error(f"更新市场数据并计算估值失败: {str(e)}")
            return {}
    
    def get_fund_valuation(self, fund_code: str) -> Optional[float]:
        """
        获取单只基金的估值
        
        Args:
            fund_code: 基金代码
            
        Returns:
            基金估值涨跌百分比，若失败返回 None
        """
        try:
            return self.engine.calculate(fund_code)
        except Exception as e:
            logger.error(f"获取基金 {fund_code} 估值失败: {str(e)}")
            return None
    
    def warmup(self):
        """
        预热估值服务，加载所有基金的权重矩阵
        """
        logger.info("开始预热估值服务...")
        self.rebuild_matrix()
        logger.info("估值服务预热完成")

# 全局估值服务实例
valuation_service = ValuationService()
