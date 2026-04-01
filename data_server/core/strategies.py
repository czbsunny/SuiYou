#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
基金估值策略模块
"""

from abc import ABC, abstractmethod
from typing import List, Dict, Optional
import pandas as pd
from utils.helpers import standardize_symbol

class ValuationStrategy(ABC):
    """
    估值策略基类
    """
    
    @abstractmethod
    def get_weights_matrix(self, fund_code: str, context) -> pd.DataFrame:
        """
        获取权重矩阵
        
        Args:
            fund_code: 基金代码
            context: 估值上下文，包含持仓、资产配置和指数映射数据
            
        Returns:
            权重矩阵，index为股票代码，columns为基金代码，values为权重
        """
        pass
    
    @abstractmethod
    def normalize_weights(self, weights: pd.Series) -> pd.Series:
        """
        归一化权重
        
        Args:
            weights: 原始权重系列
            
        Returns:
            归一化后的权重系列
        """
        pass

class ActiveFundStrategy(ValuationStrategy):
    """
    主动型基金估值策略
    """
    
    def get_weights_matrix(self, fund_code: str, context) -> pd.DataFrame:
        """
        获取主动型基金的权重矩阵
        
        Args:
            fund_code: 基金代码
            context: 估值上下文，包含持仓、资产配置和指数映射数据
            
        Returns:
            权重矩阵
        """
        # 从上下文获取持仓数据
        data = context.get_top10(fund_code)
        
        if not data:
            return pd.DataFrame()
        
        # 转换数据为DataFrame
        df = pd.DataFrame(data)
        
        # 标准化股票代码
        df['stock_code'] = df['stock_code'].apply(standardize_symbol)
        
        # 过滤无效代码
        df = df[df['stock_code'].notna()]
        
        if df.empty:
            return pd.DataFrame()
        
        # 获取股票资产占比
        total_ratio = context.get_total_ratio(fund_code)
        
        # 创建权重矩阵
        weights_matrix = pd.DataFrame(
            index=df['stock_code'],
            columns=[fund_code],
            data=df['weight'].values * total_ratio / 100
        )
        
        return weights_matrix
    
    def normalize_weights(self, weights: pd.Series) -> pd.Series:
        """
        归一化主动型基金的权重
        
        Args:
            weights: 原始权重系列
            
        Returns:
            归一化后的权重系列
        """
        total_weight = weights.sum()
        if total_weight > 0:
            return weights / total_weight
        return weights

class IndexFundStrategy(ValuationStrategy):
    """
    指数型基金估值策略
    """
    
    def get_weights_matrix(self, fund_code: str, context) -> pd.DataFrame:
        """
        获取指数型基金的权重矩阵
        
        Args:
            fund_code: 基金代码
            context: 估值上下文，包含持仓、资产配置和指数映射数据
            
        Returns:
            权重矩阵
        """
        # 从上下文获取指数映射数据
        data = context.get_mappings(fund_code)
        
        if not data:
            return pd.DataFrame()
        
        # 转换数据为DataFrame
        df = pd.DataFrame(data)
        
        # 创建权重矩阵
        weights_matrix = pd.DataFrame(
            index=df['index_code'],
            columns=[fund_code],
            data=df['effective_weight'].values
        )
        
        # 归一化权重
        weights_matrix[fund_code] = self.normalize_weights(weights_matrix[fund_code])
        
        return weights_matrix
    
    def normalize_weights(self, weights: pd.Series) -> pd.Series:
        """
        归一化指数型基金的权重
        
        Args:
            weights: 原始权重系列
            
        Returns:
            归一化后的权重系列
        """
        total_weight = weights.sum()
        if total_weight > 0:
            return weights / total_weight
        return weights

class MixedFundStrategy(ValuationStrategy):
    """
    混合型基金估值策略
    同时考虑股票持仓和指数映射
    """
    
    def __init__(self):
        self.active_strategy = ActiveFundStrategy()
        self.index_strategy = IndexFundStrategy()
    
    def get_weights_matrix(self, fund_code: str, context) -> pd.DataFrame:
        """
        获取混合型基金的权重矩阵
        
        Args:
            fund_code: 基金代码
            context: 估值上下文，包含持仓、资产配置和指数映射数据
            
        Returns:
            权重矩阵
        """
        # 获取股票持仓权重矩阵
        stock_matrix = self.active_strategy.get_weights_matrix(fund_code, context)

        # 获取指数映射权重矩阵
        index_matrix = self.index_strategy.get_weights_matrix(fund_code, context)
        
        # 合并矩阵
        if stock_matrix.empty:
            return index_matrix
        if index_matrix.empty:
            return stock_matrix
        
        total_ratio = context.get_total_ratio(fund_code)

        stock_weight_sum = stock_matrix[fund_code].sum()
        remain_weight = max(0, total_ratio - stock_weight_sum)
        index_matrix[fund_code] = index_matrix[fund_code] * remain_weight

        # 合并并归一化
        combined_matrix = pd.concat([stock_matrix, index_matrix], axis=0)

        return combined_matrix
    
    def normalize_weights(self, weights: pd.Series) -> pd.Series:
        """
        归一化混合型基金的权重
        
        Args:
            weights: 原始权重系列
            
        Returns:
            归一化后的权重系列
        """
        total_weight = weights.sum()
        if total_weight > 0:
            return weights / total_weight
        return weights

# 策略工厂
class StrategyFactory:
    """
    策略工厂类，用于创建不同类型的估值策略
    """
    
    @staticmethod
    def get_strategy(fund_type: str) -> ValuationStrategy:
        """
        根据基金类型获取对应的估值策略
        
        Args:
            fund_type: 基金类型，如 'active', 'index', 'mixed'
            
        Returns:
            估值策略实例
        """
        if fund_type == 'active':
            return ActiveFundStrategy()
        elif fund_type == 'index':
            return IndexFundStrategy()
        elif fund_type == 'mixed':
            return MixedFundStrategy()
        else:
            # 默认使用主动型策略
            return ActiveFundStrategy()
