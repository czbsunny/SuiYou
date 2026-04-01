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
from core.attribution import AttributionAnalyzer

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
        """
        为所有基金运行归因分析
        """
        start_time = datetime.now()
        logger.info(f"[{start_time.strftime('%Y-%m-%d %H:%M:%S')}] 开始为所有基金运行归因分析...")
        
        db = SessionLocal()
        processed_count = 0
        
        try:
            # 获取所有有指数映射的基金
            fund_index_mappings = db.query(FundIndexMapping).all()
            
            logger.info(f"获取到 {len(fund_index_mappings)} 条基金指数映射记录")
            
            # 按基金代码分组
            fund_mappings = {}
            for mapping in fund_index_mappings:
                if mapping.fund_code not in fund_mappings:
                    fund_mappings[mapping.fund_code] = []
                fund_mappings[mapping.fund_code].append(mapping)
            
            # 为每个基金运行归因分析
            for fund_code, mappings in fund_mappings.items():
                try:
                    # 运行归因分析
                    success = self.run_attribution_for_fund(db, fund_code, mappings)
                    if success:
                        processed_count += 1
                except Exception as e:
                    logger.error(f"为基金 {fund_code} 运行归因分析失败: {str(e)}")
                    continue
            
            end_time = datetime.now()
            duration = (end_time - start_time).total_seconds()
            logger.info(f"[{end_time.strftime('%Y-%m-%d %H:%M:%S')}] 归因分析任务完成，总耗时: {duration:.2f}秒，成功处理 {processed_count} 只基金")
            
        except Exception as e:
            logger.error(f"归因分析任务失败: {str(e)}")
        finally:
            db.close()
    
    def run_attribution_for_fund(self, db: Session, fund_code: str, mappings: List[FundIndexMapping]) -> bool:
        """
        为单个基金运行归因分析
        
        Args:
            db: 数据库会话
            fund_code: 基金代码
            mappings: 基金指数映射列表
            
        Returns:
            是否成功
        """
        try:
            # 1. 获取基金历史净值数据
            fund_returns = self._get_fund_returns(db, fund_code)
            
            if fund_returns.empty:
                logger.warning(f"基金 {fund_code} 无历史净值数据")
                return False
            
            # 2. 获取指数历史数据作为因子
            factor_returns = self._get_factor_returns(db, mappings, fund_returns.index)
            
            if factor_returns.empty:
                logger.warning(f"基金 {fund_code} 无有效因子数据")
                return False
            
            # 3. 运行回归分析
            betas = self.analyzer.calculate_betas(fund_code, fund_returns, factor_returns)
            
            if not betas:
                logger.warning(f"基金 {fund_code} 回归分析失败")
                return False
            
            # 4. 更新有效权重
            self._update_effective_weights(db, fund_code, mappings, betas)
            
            logger.info(f"基金 {fund_code} 归因分析完成，Beta 系数: {betas}")
            
            return True
            
        except Exception as e:
            logger.error(f"为基金 {fund_code} 运行归因分析失败: {str(e)}")
            return False
    
    def _get_fund_returns(self, db: Session, fund_code: str, days: int = 90) -> pd.Series:
        """
        获取基金历史净值数据并计算收益率
        
        Args:
            db: 数据库会话
            fund_code: 基金代码
            days: 历史天数
            
        Returns:
            基金收益率序列
        """
        # 计算开始日期
        end_date = datetime.now()
        start_date = end_date - timedelta(days=days)
        
        # 查询历史净值
        nav_history = db.query(FundNavHistory).filter(
            FundNavHistory.fund_code == fund_code,
            FundNavHistory.date >= start_date,
            FundNavHistory.date <= end_date
        ).order_by(FundNavHistory.date).all()
        
        if not nav_history:
            return pd.Series(dtype=float)
        
        # 转换为DataFrame
        df = pd.DataFrame([
            {
                "date": item.date,
                "nav": item.nav
            }
            for item in nav_history
        ])
        
        # 设置日期索引
        df.set_index("date", inplace=True)
        
        # 计算日收益率
        returns = df["nav"].pct_change().dropna() * 100  # 转换为百分比
        
        return returns
    
    def _get_factor_returns(self, db: Session, mappings: List[FundIndexMapping], index_dates: pd.DatetimeIndex) -> pd.DataFrame:
        """
        获取指数历史数据并计算收益率
        
        Args:
            db: 数据库会话
            mappings: 基金指数映射列表
            index_dates: 基金收益率的日期索引
            
        Returns:
            因子收益率矩阵
        """
        factor_returns = pd.DataFrame(index=index_dates)
        
        for mapping in mappings:
            index_code = mapping.index_code
            
            # 查询指数历史数据
            index_history = db.query(IndexDailyQuote).filter(
                IndexDailyQuote.index_code == index_code,
                IndexDailyQuote.date >= index_dates.min(),
                IndexDailyQuote.date <= index_dates.max()
            ).order_by(IndexDailyQuote.date).all()
            
            if not index_history:
                continue
            
            # 转换为DataFrame
            df = pd.DataFrame([
                {
                    "date": item.date,
                    "close": item.close
                }
                for item in index_history
            ])
            
            # 设置日期索引
            df.set_index("date", inplace=True)
            
            # 计算日收益率
            returns = df["close"].pct_change().dropna() * 100  # 转换为百分比
            
            # 合并到因子收益率矩阵
            factor_returns[index_code] = returns
        
        # 填充缺失值
        factor_returns = factor_returns.fillna(0)
        
        return factor_returns
    
    def _update_effective_weights(self, db: Session, fund_code: str, mappings: List[FundIndexMapping], betas: Dict[str, float]):
        """
        更新基金指数映射的有效权重
        
        Args:
            db: 数据库会话
            fund_code: 基金代码
            mappings: 基金指数映射列表
            betas: Beta 系数字典
        """
        for mapping in mappings:
            index_code = mapping.index_code
            
            # 获取对应指数的 Beta 系数
            if index_code in betas:
                effective_weight = betas[index_code]
                # 确保权重在合理范围内
                effective_weight = max(0, min(1, effective_weight))
                
                # 更新有效权重
                mapping.effective_weight = effective_weight
                mapping.last_update = datetime.now()
                
                logger.debug(f"更新基金 {fund_code} 对指数 {index_code} 的有效权重为: {effective_weight}")
        
        # 提交更改
        db.commit()
    
    def get_attribution_result(self, fund_code: str) -> Optional[Dict[str, float]]:
        """
        获取基金的归因分析结果
        
        Args:
            fund_code: 基金代码
            
        Returns:
            Beta 系数字典，若失败返回 None
        """
        try:
            db = SessionLocal()
            
            # 获取基金指数映射
            mappings = db.query(FundIndexMapping).filter(
                FundIndexMapping.fund_code == fund_code
            ).all()
            
            if not mappings:
                logger.warning(f"基金 {fund_code} 无指数映射数据")
                return None
            
            # 获取基金历史净值数据
            fund_returns = self._get_fund_returns(db, fund_code)
            
            if fund_returns.empty:
                logger.warning(f"基金 {fund_code} 无历史净值数据")
                return None
            
            # 获取因子数据
            factor_returns = self._get_factor_returns(db, mappings, fund_returns.index)
            
            if factor_returns.empty:
                logger.warning(f"基金 {fund_code} 无有效因子数据")
                return None
            
            # 运行回归分析
            betas = self.analyzer.calculate_betas(fund_code, fund_returns, factor_returns)
            
            db.close()
            
            return betas
            
        except Exception as e:
            logger.error(f"获取基金 {fund_code} 的归因分析结果失败: {str(e)}")
            return None

# 全局归因服务实例
attribution_service = AttributionService()


