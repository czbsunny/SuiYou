#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
实时数据采集模块
5分钟一次拉取 A/港/美/指数 快照
"""

import logging
from datetime import datetime, timedelta
from typing import Dict, List, Optional
import os
import json
import akshare as ak
import pandas as pd

from utils.helpers import standardize_symbol

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class RealtimeFetcher:
    """
    实时数据采集类，负责从 AkShare 获取实时股票和指数数据
    """
    
    def __init__(self, debug_mode: bool = True):
        # 缓存数据，减少重复请求
        self.data_cache = {}
        self.cache_timestamp = {}
        # 缓存有效期（秒）
        self.CACHE_EXPIRY = 30
        # 调试模式，启用本地文件缓存
        self.debug_mode = debug_mode
        # 本地缓存目录
        self.cache_dir = os.path.join(os.path.dirname(__file__), 'cache')
        # 确保缓存目录存在
        os.makedirs(self.cache_dir, exist_ok=True)
    
    def fetch_all_market_data(self) -> Dict[str, float]:
        """
        获取所有市场的数据
        
        Returns:
            市场数据，格式为 {"sh.600519": 0.5, "sz.000001": -0.2, ...}
        """
        start_time = datetime.now()
        logger.info(f"[{start_time.strftime('%Y-%m-%d %H:%M:%S')}] 开始获取所有市场数据...")
        
        all_data = {}
        
        try:
            # 获取 A 股数据
            a_stock_data = self.fetch_a_stock_data()
            all_data.update(a_stock_data)
            logger.info(f"获取到 {len(a_stock_data)} 条 A 股数据")
            
            # 获取科创板数据
            kcb_stock_data = self.fetch_kcb_stock_data()
            all_data.update(kcb_stock_data)
            logger.info(f"获取到 {len(kcb_stock_data)} 条科创板数据")
            
            # 获取港股数据
            hk_stock_data = self.fetch_hk_stock_data()
            all_data.update(hk_stock_data)
            logger.info(f"获取到 {len(hk_stock_data)} 条港股数据")
            
            # # 获取指数数据
            # index_data = self.fetch_index_data()
            # all_data.update(index_data)
            # logger.info(f"获取到 {len(index_data)} 条指数数据")
            
            # 获取申万指数数据
            swindex_data = self.fetch_swindex_data()
            all_data.update(swindex_data)
            logger.info(f"获取到 {len(swindex_data)} 条申万指数数据")
            
            # 获取美股数据（每天只初始化一次）
            # us_stock_data = self.fetch_us_stock_data()
            # all_data.update(us_stock_data)
            # logger.info(f"获取到 {len(us_stock_data)} 条美股数据")
            
            end_time = datetime.now()
            duration = (end_time - start_time).total_seconds()
            logger.info(f"[{end_time.strftime('%Y-%m-%d %H:%M:%S')}] 市场数据获取完成，总耗时: {duration:.2f}秒，共获取 {len(all_data)} 条数据")
            
            return all_data
            
        except Exception as e:
            logger.error(f"获取市场数据失败: {str(e)}")
            return {}
    
    def fetch_a_stock_data(self) -> Dict[str, float]:
        """
        获取 A 股实时数据
        
        Returns:
            A 股数据，格式为 {"sh.600519": 0.5, "sz.000001": -0.2, ...}
        """
        try:
            # 检查内存缓存
            cache_key = "a_stock"
            if self._is_cache_valid(cache_key):
                return self.data_cache[cache_key]
            
            # 检查本地缓存
            local_data = self._load_from_local_cache("a_stock")
            if local_data:
                # 更新内存缓存
                self._update_cache(cache_key, local_data)
                return local_data
            
            # 使用 AkShare 获取 A 股实时数据
            df = ak.stock_zh_a_spot()
            
            if df.empty:
                logger.warning("A 股数据为空")
                return {}
            
            # 处理数据
            result = {}
            for _, row in df.iterrows():
                stock_code = row['代码']
                change = row['涨跌幅']
                
                # 只保留后面6位（处理北交所股票如 bj920000 -> 920000）
                if len(stock_code) > 6:
                    stock_code = stock_code[-6:]
                
                # 标准化股票代码
                standardized_code = standardize_symbol(stock_code)
                if standardized_code:
                    result[standardized_code] = change
            
            # 更新内存缓存
            self._update_cache(cache_key, result)
            
            # 保存到本地缓存
            self._save_to_local_cache("a_stock", result)
            
            return result
            
        except Exception as e:
            logger.error(f"获取 A 股数据失败: {str(e)}")
            return {}
    
    def fetch_kcb_stock_data(self) -> Dict[str, float]:
        """
        获取科创板实时数据
        
        Returns:
            科创板数据，格式为 {"sh.688001": 0.5, "sh.688002": -0.2, ...}
        """
        try:
            # 检查内存缓存
            cache_key = "kcb_stock"
            if self._is_cache_valid(cache_key):
                return self.data_cache[cache_key]
            
            # 检查本地缓存
            local_data = self._load_from_local_cache("kcb_stock")
            if local_data:
                # 更新内存缓存
                self._update_cache(cache_key, local_data)
                return local_data
            
            # 使用 AkShare 获取科创板实时数据
            df = ak.stock_zh_kcb_spot()
            
            if df.empty:
                logger.warning("科创板数据为空")
                return {}
            
            # 处理数据
            result = {}
            for _, row in df.iterrows():
                stock_code = row['代码']
                change = row['涨跌幅']
                
                # 只保留后面6位（处理科创板股票如 sh688001 -> 688001）
                if len(stock_code) > 6:
                    stock_code = stock_code[-6:]

                # 标准化股票代码
                standardized_code = standardize_symbol(stock_code)
                if standardized_code:
                    result[standardized_code] = change
            
            # 更新内存缓存
            self._update_cache(cache_key, result)
            
            # 保存到本地缓存
            self._save_to_local_cache("kcb_stock", result)
            
            return result
            
        except Exception as e:
            logger.error(f"获取科创板数据失败: {str(e)}")
            return {}
    
    def fetch_hk_stock_data(self) -> Dict[str, float]:
        """
        获取港股实时数据
        
        Returns:
            港股数据，格式为 {"hk.00700": 1.2, "hk.09988": -0.5, ...}
        """
        try:
            # 检查内存缓存
            cache_key = "hk_stock"
            if self._is_cache_valid(cache_key):
                return self.data_cache[cache_key]
            
            # 检查本地缓存
            local_data = self._load_from_local_cache("hk_stock")
            if local_data:
                # 更新内存缓存
                self._update_cache(cache_key, local_data)
                return local_data
            
            # 使用 AkShare 获取港股实时数据
            df = ak.stock_hk_spot()
            
            if df.empty:
                logger.warning("港股数据为空")
                return {}
            
            # 处理数据
            result = {}
            for _, row in df.iterrows():
                stock_code = row['代码']
                change = row['涨跌幅']
                
                # 标准化股票代码
                standardized_code = standardize_symbol(stock_code)
                if standardized_code:
                    result[standardized_code] = change
            
            # 更新内存缓存
            self._update_cache(cache_key, result)
            
            # 保存到本地缓存
            self._save_to_local_cache("hk_stock", result)
            
            return result
            
        except Exception as e:
            logger.error(f"获取港股数据失败: {str(e)}")
            return {}
    
    def fetch_index_data(self) -> Dict[str, float]:
        """
        获取指数实时数据
        
        Returns:
            指数数据，格式为 {"index.sh000001": 0.3, "index.sz399001": -0.1, ...}
        """
        try:
            # 检查内存缓存
            cache_key = "index"
            if self._is_cache_valid(cache_key):
                return self.data_cache[cache_key]
            
            # 检查本地缓存
            local_data = self._load_from_local_cache("index")
            if local_data:
                # 更新内存缓存
                self._update_cache(cache_key, local_data)
                return local_data
            
            # 使用 AkShare 获取指数实时数据
            df = ak.stock_zh_index_spot_sina()
            
            if df.empty:
                logger.warning("指数数据为空")
                return {}
            
            # 处理数据
            result = {}
            for _, row in df.iterrows():
                index_code = row['代码']
                change = row['涨跌幅']
                
                # 标准化指数代码
                standardized_code = f"index.{index_code.lower()}"
                result[standardized_code] = change
            
            # 更新内存缓存
            self._update_cache(cache_key, result)
            
            # 保存到本地缓存
            self._save_to_local_cache("index", result)
            
            return result
            
        except Exception as e:
            logger.error(f"获取指数数据失败: {str(e)}")
            return {}
    
    def fetch_swindex_data(self) -> Dict[str, float]:
        """
        获取申万指数实时数据
        
        Returns:
            申万指数数据，格式为 {"801010": 0.3, "801020": -0.1, ...}
        """
        try:
            # 检查内存缓存
            cache_key = "swindex"
            if self._is_cache_valid(cache_key):
                return self.data_cache[cache_key]
            
            # 检查本地缓存
            local_data = self._load_from_local_cache("swindex")
            if local_data:
                # 更新内存缓存
                self._update_cache(cache_key, local_data)
                return local_data
            
            # 使用 AkShare 获取申万指数实时数据
            df = ak.index_realtime_sw(symbol="二级行业")
            
            if df.empty:
                logger.warning("申万指数数据为空")
                return {}
            
            # 处理数据
            result = {}
            for _, row in df.iterrows():
                index_code = row['指数代码']
                close = row['昨收盘']
                latest_price = row['最新价']
                change = (latest_price - close) / close * 100
                
                # 标准化申万指数代码
                standardized_code = standardize_symbol(index_code, "index")
                result[standardized_code] = change
            
            # 更新内存缓存
            self._update_cache(cache_key, result)
            
            # 保存到本地缓存
            self._save_to_local_cache("swindex", result)
            
            logger.info(f"获取到 {len(result)} 条申万指数数据")
            
            return result
            
        except Exception as e:
            logger.error(f"获取申万指数数据失败: {str(e)}")
            return {}
    
    def fetch_us_stock_data(self) -> Dict[str, float]:
        """
        获取美股实时数据（每天只初始化一次）
        
        Returns:
            美股数据，格式为 {"us.AAPL": 0.8, "us.MSFT": -0.2, ...}
        """
        try:
            # 美股数据缓存键，包含日期
            today = datetime.now().strftime('%Y-%m-%d')
            cache_key = f"us_stock_{today}"
            
            # 检查内存缓存
            if cache_key in self.data_cache:
                return self.data_cache[cache_key]
            
            # 检查本地缓存
            local_data = self._load_from_local_cache("us_stock")
            if local_data:
                # 更新内存缓存
                self.data_cache[cache_key] = local_data
                self.cache_timestamp[cache_key] = datetime.now()
                return local_data
            
            # 使用 AkShare 获取美股实时数据
            df = ak.stock_us_spot()
            
            if df.empty:
                logger.warning("美股数据为空")
                return {}
            
            # 处理数据
            result = {}
            for _, row in df.iterrows():
                stock_code = row['代码']
                change = row['涨跌幅']
                
                # 标准化股票代码
                standardized_code = standardize_symbol(stock_code)
                if standardized_code:
                    result[standardized_code] = change
            
            # 更新内存缓存（使用包含日期的缓存键，确保每天只更新一次）
            self.data_cache[cache_key] = result
            # 记录缓存时间
            self.cache_timestamp[cache_key] = datetime.now()
            
            # 保存到本地缓存
            self._save_to_local_cache("us_stock", result)
            
            logger.info(f"美股数据已更新，缓存键: {cache_key}")
            
            return result
            
        except Exception as e:
            logger.error(f"获取美股数据失败: {str(e)}")
            return {}
    
    def fetch_specific_symbols(self, symbols: List[str]) -> Dict[str, float]:
        """
        获取指定标的的数据
        
        Args:
            symbols: 标的代码列表，如 ["sh.600519", "sz.000001", "index.sh000001"]
            
        Returns:
            标的数据，格式为 {"sh.600519": 0.5, "sz.000001": -0.2, ...}
        """
        result = {}
        
        # 分类标的
        a_stocks = []
        hk_stocks = []
        us_stocks = []
        indices = []
        
        for symbol in symbols:
            if symbol.startswith("sh.") or symbol.startswith("sz."):
                # A 股
                code = symbol.split(".")[1]
                a_stocks.append(code)
            elif symbol.startswith("hk."):
                # 港股
                code = symbol.split(".")[1]
                hk_stocks.append(code)
            elif symbol.startswith("us."):
                # 美股
                code = symbol.split(".")[1]
                us_stocks.append(code)
            elif symbol.startswith("index."):
                # 指数
                code = symbol.split(".")[1]
                indices.append(code)
        
        # 获取所有市场数据
        all_data = self.fetch_all_market_data()
        
        # 提取指定标的的数据
        for symbol in symbols:
            if symbol in all_data:
                result[symbol] = all_data[symbol]
        
        return result
    
    def _is_cache_valid(self, cache_key: str) -> bool:
        """
        检查缓存是否有效
        
        Args:
            cache_key: 缓存键
            
        Returns:
            缓存是否有效
        """
        if cache_key not in self.data_cache:
            return False
        
        if cache_key not in self.cache_timestamp:
            return False
        
        # 检查缓存时间
        cache_time = self.cache_timestamp[cache_key]
        now = datetime.now()
        
        return (now - cache_time).total_seconds() < self.CACHE_EXPIRY
    
    def _update_cache(self, cache_key: str, data: Dict[str, float]):
        """
        更新缓存
        
        Args:
            cache_key: 缓存键
            data: 缓存数据
        """
        self.data_cache[cache_key] = data
        self.cache_timestamp[cache_key] = datetime.now()
    
    def clear_cache(self):
        """
        清除缓存
        """
        self.data_cache.clear()
        self.cache_timestamp.clear()
        logger.debug("清除所有缓存")
    
    def _get_cache_file_path(self, data_type: str) -> str:
        """
        获取缓存文件路径
        
        Args:
            data_type: 数据类型，如 "a_stock", "hk_stock", "us_stock" 等
            
        Returns:
            缓存文件路径
        """
        today = datetime.now().strftime('%Y-%m-%d')
        return os.path.join(self.cache_dir, f"{data_type}_{today}.json")
    
    def _save_to_local_cache(self, data_type: str, data: Dict[str, float]):
        """
        保存数据到本地缓存文件
        
        Args:
            data_type: 数据类型
            data: 要保存的数据
        """
        if not self.debug_mode:
            return
        
        try:
            cache_file = self._get_cache_file_path(data_type)
            cache_data = {
                'data': data,
                'timestamp': datetime.now().isoformat()
            }
            
            with open(cache_file, 'w', encoding='utf-8') as f:
                json.dump(cache_data, f, ensure_ascii=False, indent=2)
            
            logger.debug(f"数据已保存到本地缓存: {cache_file}")
            
        except Exception as e:
            logger.error(f"保存到本地缓存失败: {str(e)}")
    
    def _load_from_local_cache(self, data_type: str) -> Optional[Dict[str, float]]:
        """
        从本地缓存文件加载数据
        
        Args:
            data_type: 数据类型
            
        Returns:
            加载的数据，若失败返回 None
        """
        if not self.debug_mode or data_type != "us_stock":
            return None
        
        try:
            cache_file = self._get_cache_file_path(data_type)
            
            if not os.path.exists(cache_file):
                return None
            
            # 检查文件修改时间，超过1天的缓存视为无效
            file_mtime = os.path.getmtime(cache_file)
            file_age = datetime.now().timestamp() - file_mtime
            if file_age > 86400:  # 24小时
                logger.debug(f"本地缓存文件已过期: {cache_file}")
                return None
            
            with open(cache_file, 'r', encoding='utf-8') as f:
                cache_data = json.load(f)
            
            logger.debug(f"从本地缓存加载数据: {cache_file}")
            
            return cache_data.get('data', None)
            
        except Exception as e:
            logger.error(f"从本地缓存加载数据失败: {str(e)}")
            return None

# 全局实时数据采集器实例
realtime_fetcher = RealtimeFetcher()
