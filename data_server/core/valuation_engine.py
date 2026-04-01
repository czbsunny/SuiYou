#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
估值引擎模块
实现矩阵计算引擎，处理 Matrix.dot(Vector) 运算
"""

import logging
from typing import Dict, Optional
import pandas as pd
import numpy as np

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class ValuationEngine:
    """
    估值引擎类，负责管理权重矩阵和市场数据，执行矩阵计算
    """
    
    def __init__(self):
        # 存储所有基金的权重矩阵
        self.weights_matrices = {}
        # 存储市场数据向量
        self.market_data = pd.Series(dtype=float)
        # 缓存计算结果
        self.calculation_cache = {}
    
    def update_weights(self, fund_code: str, weights: pd.DataFrame):
        """
        更新基金的权重矩阵
        
        Args:
            fund_code: 基金代码
            weights: 权重矩阵，index为股票/指数代码，columns为基金代码
        """
        if not weights.empty:
            self.weights_matrices[fund_code] = weights
            # 清除相关缓存
            if fund_code in self.calculation_cache:
                del self.calculation_cache[fund_code]
            logger.debug(f"更新基金 {fund_code} 的权重矩阵，包含 {len(weights)} 个标的")
    
    def update_market_data(self, market_data: Dict[str, float]):
        """
        更新市场数据
        
        Args:
            market_data: 市场数据，格式为 {"sh.600519": 0.5, "sz.000001": -0.2, ...}
        """
        # 转换为Series
        new_market_data = pd.Series(market_data)
        
        # 更新市场数据
        self.market_data = new_market_data
        
        # 清除所有缓存
        self.calculation_cache.clear()
        
        logger.debug(f"更新市场数据，包含 {len(new_market_data)} 个标的")
    
    def calculate(self, fund_code: str) -> Optional[float]:
        """
        计算单个基金的估值
        
        Args:
            fund_code: 基金代码
            
        Returns:
            基金估值涨跌百分比，若失败返回 None
        """
        # 检查缓存
        if fund_code in self.calculation_cache:
            return self.calculation_cache[fund_code]
        
        # 检查权重矩阵是否存在
        if fund_code not in self.weights_matrices:
            logger.warning(f"基金 {fund_code} 的权重矩阵不存在")
            return None
        
        weights = self.weights_matrices[fund_code]
        
        if weights.empty:
            logger.warning(f"基金 {fund_code} 的权重矩阵为空")
            return None
        
        # 确保市场数据不为空
        if self.market_data.empty:
            logger.warning("市场数据为空")
            return None
        
        try:
            # 执行矩阵乘法：权重矩阵 × 市场数据向量
            # 1. 获取共同的索引
            common_index = weights.index.intersection(self.market_data.index)
            
            if common_index.empty:
                logger.warning(f"基金 {fund_code} 与市场数据无共同标的")
                return None
            
            # 2. 提取共同部分
            weights_subset = weights.loc[common_index]
            market_subset = self.market_data.loc[common_index]
            
            # 3. 执行矩阵乘法
            # 使用numpy进行高效计算
            weights_array = weights_subset.values.flatten()
            market_array = market_subset.values
            
            # 计算加权平均
            weighted_change = np.dot(weights_array, market_array)
            
            # 缓存结果
            self.calculation_cache[fund_code] = weighted_change
            
            return weighted_change
            
        except Exception as e:
            logger.error(f"计算基金 {fund_code} 估值失败: {str(e)}")
            return None
    
    def calculate_all(self) -> Dict[str, float]:
        """
        计算所有基金的估值
        
        Returns:
            基金估值结果，格式为 {"000001": 0.3, "000002": -0.1, ...}
        """
        results = {}
        
        for fund_code in self.weights_matrices:
            try:
                change = self.calculate(fund_code)
                if change is not None:
                    results[fund_code] = change
            except Exception as e:
                logger.error(f"计算基金 {fund_code} 估值失败: {str(e)}")
                continue
        
        return results
    
    def get_weights(self, fund_code: str) -> Optional[pd.DataFrame]:
        """
        获取基金的权重矩阵
        
        Args:
            fund_code: 基金代码
            
        Returns:
            权重矩阵，若不存在返回 None
        """
        return self.weights_matrices.get(fund_code)
    
    def get_market_data(self) -> pd.Series:
        """
        获取当前市场数据
        
        Returns:
            市场数据Series
        """
        return self.market_data
    
    def remove_fund(self, fund_code: str):
        """
        移除基金的权重矩阵
        
        Args:
            fund_code: 基金代码
        """
        if fund_code in self.weights_matrices:
            del self.weights_matrices[fund_code]
        if fund_code in self.calculation_cache:
            del self.calculation_cache[fund_code]
        logger.debug(f"移除基金 {fund_code} 的数据")
    
    def clear(self):
        """
        清空所有数据
        """
        self.weights_matrices.clear()
        self.market_data = pd.Series(dtype=float)
        self.calculation_cache.clear()
        logger.debug("清空估值引擎数据")
    
    def get_status(self) -> Dict:
        """
        获取估值引擎状态
        
        Returns:
            状态信息
        """
        return {
            "fund_count": len(self.weights_matrices),
            "market_data_count": len(self.market_data),
            "cache_count": len(self.calculation_cache)
        }
