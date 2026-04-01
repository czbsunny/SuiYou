#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
基金数据获取模块
用于从公开API获取基金基本信息和净值数据
"""

import pandas as pd
import requests
import logging
import time
from typing import Optional, Dict, Any, List

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class FundDataFetcher:
    """基金数据获取类"""
    
    def __init__(self, timeout: float = 15.0, retry_count: int = 3, retry_delay: float = 2.0):
        """初始化基金数据获取器"""
        self.timeout = timeout
        self.retry_count = retry_count
        self.retry_delay = retry_delay
        self.headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/129.0.0.0 Safari/537.36"
        }
    
    def _request_with_retry(self, url: str, params: Optional[Dict[str, Any]] = None) -> Optional[Dict[str, Any]]:
        """带重试机制的HTTP请求"""
        for attempt in range(self.retry_count):
            try:
                response = requests.get(
                    url, 
                    headers=self.headers, 
                    params=params, 
                    timeout=self.timeout
                )
                response.raise_for_status()
                return response.json()
            except requests.exceptions.RequestException as e:
                logger.warning(f"请求失败 (尝试 {attempt + 1}/{self.retry_count}): {str(e)}")
                if attempt < self.retry_count - 1:
                    time.sleep(self.retry_delay)
        logger.error(f"请求失败，已达到最大重试次数: {url}")
        return None
    
    def get_fund_basic_info(self, symbol: str) -> Optional[Dict[str, Any]]:
        """获取基金基本信息"""
        url = f"https://danjuanfunds.com/djapi/fund/{symbol}"
        json_data = self._request_with_retry(url)
        
        if not json_data or "data" not in json_data:
            logger.error(f"获取基金 {symbol} 基本信息失败")
            return None
        
        try:
            fund_data = json_data["data"]
            return {
                "fund_code": fund_data.get("fd_code", ""),
                "fund_name": fund_data.get("fd_name", ""),
                "fund_full_name": fund_data.get("fd_full_name", ""),
                "found_date": fund_data.get("found_date", ""),
                "total_scale": fund_data.get("totshare", 0),
                "company": fund_data.get("keeper_name", ""),
                "manager": fund_data.get("manager_name", ""),
                "fund_type": fund_data.get("type_desc", ""),
                "update_time": time.strftime("%Y-%m-%d %H:%M:%S")
            }
        except Exception as e:
            logger.error(f"解析基金 {symbol} 基本信息失败: {str(e)}")
            return None
    
    def get_fund_nav_history(self, symbol: str, start_date: Optional[str] = None, end_date: Optional[str] = None) -> Optional[List[Dict[str, Any]]]:
        """获取基金净值历史数据"""
        # 使用天天基金网API获取净值数据
        url = f"https://fund.eastmoney.com/pingzhongdata/{symbol}.js"
        try:
            response = requests.get(url, headers=self.headers, timeout=self.timeout)
            response.raise_for_status()
            
            # 解析JavaScript数据
            content = response.text
            
            # 提取净值数据
            nav_data_pattern = r'var\s+sjjzList\s*=\s*"([^"]+)"'
            import re
            match = re.search(nav_data_pattern, content)
            
            if not match:
                logger.error(f"无法提取基金 {symbol} 净值数据")
                return None
            
            nav_data_str = match.group(1)
            nav_data_list = []
            
            # 解析CSV格式的数据
            for line in nav_data_str.split(';'):
                if line:
                    parts = line.split(',')
                    if len(parts) >= 3:
                        nav_data_list.append({
                            "fund_code": symbol,
                            "nav_date": parts[0],
                            "unit_nav": float(parts[1]),
                            "accumulated_nav": float(parts[2]),
                            "update_time": time.strftime("%Y-%m-%d %H:%M:%S")
                        })
            
            # 根据日期过滤
            if start_date:
                nav_data_list = [d for d in nav_data_list if d["nav_date"] >= start_date]
            if end_date:
                nav_data_list = [d for d in nav_data_list if d["nav_date"] <= end_date]
            
            return nav_data_list
            
        except Exception as e:
            logger.error(f"获取基金 {symbol} 净值历史数据失败: {str(e)}")
            return None
    
    def get_fund_list(self, page: int = 1, page_size: int = 100) -> Optional[List[str]]:
        """获取基金代码列表"""
        # 这里使用一个简化的方法，实际项目中可能需要更完整的基金列表获取方式
        # 可以考虑从CSV文件或其他数据源获取基金列表
        logger.info(f"获取基金列表，第{page}页，每页{page_size}条")
        # 返回一些示例基金代码
        sample_funds = [
            "000001", "000002", "000003", "000004", "000005",
            "000006", "000007", "000008", "000009", "000010"
        ]
        return sample_funds[:page_size]