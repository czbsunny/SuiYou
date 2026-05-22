#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
深交所数据获取模块
提供深交所证券产品信息的获取功能
"""

import requests
import random
import pandas as pd
import logging
import asyncio
import warnings
from io import BytesIO
from typing import List, Dict, Optional

# 配置日志记录
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class SZSEFetcher:
    """深交所数据获取器类，提供深交所证券产品信息的获取方法"""
    
    def __init__(self):
        """初始化深交所获取器，创建共享的HTTP会话"""
        self._session = requests.Session()
        self._session.headers.update({
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Accept": "application/json, text/plain, */*",
            "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
            "Accept-Encoding": "gzip, deflate, br",
            "Connection": "keep-alive",
            "Host": "www.szse.cn",
        })
    
    async def close(self):
        """异步关闭HTTP会话，释放资源"""
        if hasattr(self, '_session') and self._session:
            await asyncio.to_thread(self._session.close)
            self._session = None
    
    def _generate_random(self) -> float:
        """生成随机数，模拟请求参数"""
        return random.random()
    
    async def _fetch_xlsx_data(self, catalog_id: str, tab_key: str = "tab1", date_str: Optional[str] = None) -> Optional[pd.DataFrame]:
        """
        通用方法：从深交所获取XLSX格式数据
        
        Args:
            catalog_id: 数据目录ID
            tab_key: 表格键，默认为tab1
            date_str: 日期参数（可选）
        
        Returns:
            Optional[pd.DataFrame]: 返回解析后的DataFrame，失败返回None
        """
        try:
            url = "https://www.szse.cn/api/report/ShowReport"
            params = {
                "SHOWTYPE": "xlsx",
                "CATALOGID": catalog_id,
                "TABKEY": tab_key,
                "random": self._generate_random(),
            }
            
            if date_str:
                params["txtDate"] = date_str
            
            resp = await asyncio.to_thread(
                self._session.get,
                url,
                params=params,
                timeout=30
            )
            
            if resp.status_code != 200:
                logger.error(f"请求失败，状态码: {resp.status_code}")
                return None
            
            # 使用BytesIO读取Excel数据
            excel_data = BytesIO(resp.content)
            
            # 尝试读取Excel文件
            with warnings.catch_warnings(record=True):
                warnings.simplefilter("always")
                df = pd.read_excel(excel_data, header=0)
            
            logger.info(f"成功获取数据，共 {len(df)} 条记录")
            return df
                
        except Exception as e:
            logger.error(f"获取数据失败: {str(e)}")
            return None
    
    async def get_stock_list(self, symbol: str = "A股列表") -> Optional[pd.DataFrame]:
        """
        获取深交所股票列表
        
        Args:
            symbol: choice of {"A股列表", "B股列表", "CDR列表", "AB股列表"}
        
        Returns:
            Optional[pd.DataFrame]: 股票列表DataFrame
        """
        logger.info(f"获取深交所股票列表: {symbol}")
        
        indicator_map = {
            "A股列表": "tab1",
            "B股列表": "tab2",
            "CDR列表": "tab3",
            "AB股列表": "tab4",
        }
        
        df = await self._fetch_xlsx_data("1110", tab_key=indicator_map.get(symbol, "tab1"))
        
        if df is None or df.empty or len(df) <= 10:
            return df
        
        # 数据清洗和格式化
        if symbol == "A股列表":
            df["A股代码"] = (
                df["A股代码"]
                .astype(str)
                .str.split(".", expand=True)
                .iloc[:, 0]
                .str.zfill(6)
                .str.replace("000nan", "")
            )
            df = df[
                [
                    "板块",
                    "A股代码",
                    "A股简称",
                    "A股上市日期",
                    "A股总股本",
                    "A股流通股本",
                    "所属行业",
                ]
            ]
        elif symbol == "B股列表":
            df["B股代码"] = (
                df["B股代码"]
                .astype(str)
                .str.split(".", expand=True)
                .iloc[:, 0]
                .str.zfill(6)
                .str.replace("000nan", "")
            )
            df = df[
                [
                    "板块",
                    "B股代码",
                    "B股简称",
                    "B股上市日期",
                    "B股总股本",
                    "B股流通股本",
                    "所属行业",
                ]
            ]
        elif symbol == "AB股列表":
            df["A股代码"] = (
                df["A股代码"]
                .astype(str)
                .str.split(".", expand=True)
                .iloc[:, 0]
                .str.zfill(6)
                .str.replace("000nan", "")
            )
            df["B股代码"] = (
                df["B股代码"]
                .astype(str)
                .str.split(".", expand=True)
                .iloc[:, 0]
                .str.zfill(6)
                .str.replace("000nan", "")
            )
            df = df[
                [
                    "板块",
                    "A股代码",
                    "A股简称",
                    "A股上市日期",
                    "B股代码",
                    "B股简称",
                    "B股上市日期",
                    "所属行业",
                ]
            ]
        
        return df
    
    async def get_etf_list(self, date_str: Optional[str] = None) -> Optional[pd.DataFrame]:
        """
        获取深交所ETF列表
        CATALOGID=1277
        
        Args:
            date_str: 日期参数，格式如"2026-05-22"，默认为None
        
        Returns:
            Optional[pd.DataFrame]: ETF列表DataFrame
        """
        logger.info(f"获取深交所ETF列表，日期: {date_str}")
        return await self._fetch_xlsx_data("1277", date_str=date_str)
    
    async def get_bond_list(self) -> Optional[pd.DataFrame]:
        """
        获取深交所债券列表
        CATALOGID=1105
        
        Returns:
            Optional[pd.DataFrame]: 债券列表DataFrame
        """
        logger.info("获取深交所债券列表")
        return await self._fetch_xlsx_data("1105")
    
    async def get_all_stocks(self) -> pd.DataFrame:
        """
        获取深交所所有股票（A股+B股+CDR+AB股）
        
        Returns:
            pd.DataFrame: 合并后的股票列表
        """
        logger.info("获取深交所所有股票")
        
        tasks = [
            self.get_stock_list("A股列表"),
            self.get_stock_list("B股列表"),
            self.get_stock_list("CDR列表"),
            self.get_stock_list("AB股列表"),
        ]
        
        results = await asyncio.gather(*tasks)
        
        # 合并数据
        big_df = pd.DataFrame()
        for i, symbol in enumerate(["A股列表", "B股列表", "CDR列表", "AB股列表"]):
            df = results[i]
            if df is not None and not df.empty:
                df["股票类型"] = symbol
                big_df = pd.concat([big_df, df], ignore_index=True)
        
        return big_df
    
    async def get_all_securities(self) -> Dict[str, pd.DataFrame]:
        """
        获取所有深交所证券产品数据
        
        Returns:
            Dict[str, pd.DataFrame]: 包含股票、ETF、债券列表的字典
        """
        logger.info("获取所有深交所证券产品数据")
        
        tasks = [
            self.get_stock_list("A股列表"),
            self.get_etf_list(),
            self.get_bond_list()
        ]
        
        results = await asyncio.gather(*tasks)
        
        return {
            "stocks": results[0],
            "etfs": results[1],
            "bonds": results[2]
        }
    
    def parse_to_security_model(self, df: pd.DataFrame, security_type: str) -> List[Dict]:
        """
        将DataFrame转换为Security模型的字典列表
        
        Args:
            df: 原始数据DataFrame
            security_type: 证券类型
        
        Returns:
            List[Dict]: Security模型字典列表
        """
        if df is None or df.empty:
            return []
        
        result = []
        for _, row in df.iterrows():
            security = {
                "security_type": security_type,
                "market": "SZ",
                "status": "LISTED"
            }
            
            # 尝试从不同列名获取代码和名称
            code = None
            name = None
            code_columns = ["A股代码", "B股代码", "证券代码", "代码", "证券代码 代码", "股票代码"]
            name_columns = ["A股简称", "B股简称", "证券简称", "简称", "证券简称 简称", "股票名称"]
            
            for col in code_columns:
                if col in df.columns:
                    code = str(row[col]).strip()
                    break
            
            for col in name_columns:
                if col in df.columns:
                    name = str(row[col]).strip()
                    break
            
            if code and name:
                security["unified_code"] = f"SZ.{code}"
                security["symbol"] = code
                security["name"] = name
                
                # 尝试获取上市日期
                date_columns = ["A股上市日期", "B股上市日期", "上市日期", "挂牌日期", "发行日期"]
                for col in date_columns:
                    if col in df.columns:
                        try:
                            security["listed_date"] = pd.to_datetime(row[col]).date()
                        except:
                            pass
                        break
                
                # 尝试获取行业信息
                if "所属行业" in df.columns:
                    security["industry"] = str(row["所属行业"]).strip()
                
                # 尝试获取板块信息
                if "板块" in df.columns:
                    security["sector"] = str(row["板块"]).strip()
                
                result.append(security)
        
        logger.info(f"解析完成，共 {len(result)} 条 {security_type} 数据")
        return result


# 创建一个全局的深交所获取器实例
szse_fetcher = SZSEFetcher()
