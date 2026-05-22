#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
上交所数据获取模块
提供上交所证券产品信息的获取功能
"""

import requests
import json
import pandas as pd
import logging
import asyncio
from typing import List, Dict, Optional

# 配置日志记录
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class SSEFetcher:
    """上交所数据获取器类，提供上交所证券产品信息的获取方法"""
    
    def __init__(self):
        """初始化上交所获取器，创建共享的HTTP会话"""
        self._session = requests.Session()
        self._session.headers.update({
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36",
            "Accept": "*/*",
            "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
            "Accept-Encoding": "gzip, deflate",
            "Connection": "keep-alive",
            "Host": "query.sse.com.cn",
            "Pragma": "no-cache",
        })
    
    async def close(self):
        """异步关闭HTTP会话，释放资源"""
        if hasattr(self, '_session') and self._session:
            await asyncio.to_thread(self._session.close)
            self._session = None
    
    async def get_stock_list(self, symbol: str = "主板A股") -> Optional[pd.DataFrame]:
        """
        获取上交所股票列表
        
        Args:
            symbol: choice of {"主板A股": "1", "主板B股": "2", "科创板": "8"}
        
        Returns:
            Optional[pd.DataFrame]: 股票列表DataFrame
        """
        logger.info(f"获取上交所股票列表: {symbol}")
        
        indicator_map = {"主板A股": "1", "主板B股": "2", "科创板": "8"}
        url = "https://query.sse.com.cn/sseQuery/commonQuery.do"
        
        headers = self._session.headers.copy()
        headers.update({
            "Referer": "https://www.sse.com.cn/assortment/stock/list/share/",
        })
        
        params = {
            "STOCK_TYPE": indicator_map.get(symbol, "1"),
            "REG_PROVINCE": "",
            "CSRC_CODE": "",
            "STOCK_CODE": "",
            "sqlId": "COMMON_SSE_CP_GPJCTPZ_GPLB_GP_L",
            "COMPANY_STATUS": "2,4,5,7,8",
            "type": "inParams",
            "isPagination": "true",
            "pageHelp.cacheSize": "1",
            "pageHelp.beginPage": "1",
            "pageHelp.pageSize": "10000",
            "pageHelp.pageNo": "1",
            "pageHelp.endPage": "1",
        }
        
        try:
            resp = await asyncio.to_thread(
                self._session.get,
                url,
                params=params,
                headers=headers,
                timeout=30
            )
            
            if resp.status_code != 200:
                logger.error(f"请求失败，状态码: {resp.status_code}")
                return None
            
            data_json = resp.json()
            
            if "result" not in data_json:
                logger.error("响应数据格式不正确")
                return None
            
            df = pd.DataFrame(data_json["result"])
            
            # 列名映射和数据清洗
            col_stock_code = "B_STOCK_CODE" if symbol == "主板B股" else "A_STOCK_CODE"
            df.rename(
                columns={
                    col_stock_code: "证券代码",
                    "COMPANY_ABBR": "证券简称",
                    "FULL_NAME": "公司全称",
                    "LIST_DATE": "上市日期",
                },
                inplace=True,
            )
            
            df = df[
                [
                    "证券代码",
                    "证券简称",
                    "公司全称",
                    "上市日期",
                ]
            ]
            
            df["上市日期"] = pd.to_datetime(df["上市日期"], errors="coerce").dt.date
            
            logger.info(f"成功获取股票列表，共 {len(df)} 条记录")
            return df
            
        except Exception as e:
            logger.error(f"获取股票列表失败: {str(e)}")
            return None
    
    async def get_all_stocks(self) -> pd.DataFrame:
        """
        获取上交所所有股票（主板A股+主板B股+科创板）
        
        Returns:
            pd.DataFrame: 合并后的股票列表
        """
        logger.info("获取上交所所有股票")
        
        tasks = [
            self.get_stock_list("主板A股"),
            self.get_stock_list("主板B股"),
            self.get_stock_list("科创板"),
        ]
        
        results = await asyncio.gather(*tasks)
        
        # 合并数据
        big_df = pd.DataFrame()
        for i, symbol in enumerate(["主板A股", "主板B股", "科创板"]):
            df = results[i]
            if df is not None and not df.empty:
                df["股票类型"] = symbol
                big_df = pd.concat([big_df, df], ignore_index=True)
        
        return big_df
    
    async def get_delisted_stocks(self, symbol: str = "全部") -> Optional[pd.DataFrame]:
        """
        获取上交所终止上市公司列表
        
        Args:
            symbol: choice of {"全部", "沪市", "科创板"}
        
        Returns:
            Optional[pd.DataFrame]: 终止上市公司列表
        """
        logger.info(f"获取上交所终止上市公司: {symbol}")
        
        symbol_map = {
            "全部": "1,2,8",
            "沪市": "1,2",
            "科创板": "8",
        }
        
        url = "https://query.sse.com.cn/commonQuery.do"
        
        headers = self._session.headers.copy()
        headers.update({
            "Referer": "https://www.sse.com.cn/assortment/stock/list/delisting/",
        })
        
        params = {
            "sqlId": "COMMON_SSE_CP_GPJCTPZ_GPLB_GP_L",
            "isPagination": "true",
            "STOCK_CODE": "",
            "CSRC_CODE": "",
            "REG_PROVINCE": "",
            "STOCK_TYPE": symbol_map.get(symbol, "1,2,8"),
            "COMPANY_STATUS": "3",
            "type": "inParams",
            "pageHelp.cacheSize": "1",
            "pageHelp.beginPage": "1",
            "pageHelp.pageSize": "500",
            "pageHelp.pageNo": "1",
            "pageHelp.endPage": "1",
        }
        
        try:
            resp = await asyncio.to_thread(
                self._session.get,
                url,
                params=params,
                headers=headers,
                timeout=30
            )
            
            if resp.status_code != 200:
                logger.error(f"请求失败，状态码: {resp.status_code}")
                return None
            
            data_json = resp.json()
            
            if "result" not in data_json:
                logger.error("响应数据格式不正确")
                return None
            
            df = pd.DataFrame(data_json["result"])
            
            df.rename(
                columns={
                    "COMPANY_ABBR": "公司简称",
                    "DELIST_DATE": "暂停上市日期",
                    "LIST_DATE": "上市日期",
                    "COMPANY_CODE": "公司代码",
                },
                inplace=True,
            )
            
            df = df[
                [
                    "公司代码",
                    "公司简称",
                    "上市日期",
                    "暂停上市日期",
                ]
            ]
            
            df["上市日期"] = pd.to_datetime(df["上市日期"], errors="coerce").dt.date
            df["暂停上市日期"] = pd.to_datetime(df["暂停上市日期"], errors="coerce").dt.date
            
            logger.info(f"成功获取终止上市公司列表，共 {len(df)} 条记录")
            return df
            
        except Exception as e:
            logger.error(f"获取终止上市公司列表失败: {str(e)}")
            return None
    
    async def get_etf_list(self, date_str: Optional[str] = None) -> Optional[pd.DataFrame]:
        """
        获取上交所ETF列表
        
        Args:
            date_str: 日期参数，格式如"2026-05-22"，默认为None
        
        Returns:
            Optional[pd.DataFrame]: ETF列表DataFrame
        """
        logger.info(f"获取上交所ETF列表")
        
        url = "http://query.sse.com.cn/security/fund/getFundListData.do"
        params = {
            "jsonCallBack": "",
            "isPagination": "true",
            "pageHelp.cacheSize": "1",
            "pageHelp.beginPage": "1",
            "pageHelp.pageSize": "5000",
            "pageHelp.pageNo": "1",
            "pageHelp.endPage": "5",
        }
        
        if date_str:
            params["txtDate"] = date_str
        
        try:
            headers = self._session.headers.copy()
            headers.update({
                "Referer": "http://www.sse.com.cn/assortment/fund/etf/list/",
            })
            
            resp = await asyncio.to_thread(
                self._session.get,
                url,
                params=params,
                headers=headers,
                timeout=30
            )
            
            if resp.status_code != 200:
                logger.error(f"请求失败，状态码: {resp.status_code}")
                return None
            
            data_text = resp.text
            if data_text.startswith("(") and data_text.endswith(")"):
                data_text = data_text[1:-1]
            
            data = json.loads(data_text)
            
            if "result" not in data:
                logger.error("响应数据格式不正确")
                return None
            
            df = pd.DataFrame(data["result"])
            logger.info(f"成功获取ETF列表，共 {len(df)} 条记录")
            return df
            
        except Exception as e:
            logger.error(f"获取ETF列表失败: {str(e)}")
            return None
    
    async def get_bond_list(self) -> Optional[pd.DataFrame]:
        """
        获取上交所债券列表
        
        Returns:
            Optional[pd.DataFrame]: 债券列表DataFrame
        """
        logger.info("获取上交所债券列表")
        
        url = "http://query.sse.com.cn/security/bond/getBondListData.do"
        params = {
            "jsonCallBack": "",
            "isPagination": "true",
            "pageHelp.cacheSize": "1",
            "pageHelp.beginPage": "1",
            "pageHelp.pageSize": "5000",
            "pageHelp.pageNo": "1",
            "pageHelp.endPage": "5",
        }
        
        try:
            headers = self._session.headers.copy()
            headers.update({
                "Referer": "http://www.sse.com.cn/assortment/bond/list/",
            })
            
            resp = await asyncio.to_thread(
                self._session.get,
                url,
                params=params,
                headers=headers,
                timeout=30
            )
            
            if resp.status_code != 200:
                logger.error(f"请求失败，状态码: {resp.status_code}")
                return None
            
            data_text = resp.text
            if data_text.startswith("(") and data_text.endswith(")"):
                data_text = data_text[1:-1]
            
            data = json.loads(data_text)
            
            if "result" not in data:
                logger.error("响应数据格式不正确")
                return None
            
            df = pd.DataFrame(data["result"])
            logger.info(f"成功获取债券列表，共 {len(df)} 条记录")
            return df
            
        except Exception as e:
            logger.error(f"获取债券列表失败: {str(e)}")
            return None
    
    async def get_all_securities(self) -> Dict[str, pd.DataFrame]:
        """
        获取所有上交所证券产品数据
        
        Returns:
            Dict[str, pd.DataFrame]: 包含股票、ETF、债券列表的字典
        """
        logger.info("获取所有上交所证券产品数据")
        
        tasks = [
            self.get_stock_list("主板A股"),
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
                "market": "SH",
                "status": "LISTED"
            }
            
            # 尝试从不同列名获取代码和名称
            code = None
            name = None
            
            code_columns = ["证券代码", "公司代码", "A_STOCK_CODE", "B_STOCK_CODE", 
                          "SECURITY_CODE_ABBR", "PRODUCT_CODE", "SECUCODE", "CODE"]
            name_columns = ["证券简称", "公司简称", "COMPANY_ABBR", "SECURITY_NAME_ABBR", 
                           "PRODUCT_NAME", "SECURITY_ABBR", "NAME"]
            
            for col in code_columns:
                if col in df.columns:
                    code = str(row[col]).strip()
                    break
            
            for col in name_columns:
                if col in df.columns:
                    name = str(row[col]).strip()
                    break
            
            if code and name:
                security["unified_code"] = f"SH.{code}"
                security["symbol"] = code
                security["name"] = name
                
                # 尝试获取上市日期
                date_columns = ["上市日期", "LIST_DATE", "LISTING_DATE", "挂牌日期"]
                for col in date_columns:
                    if col in df.columns:
                        try:
                            security["listed_date"] = pd.to_datetime(row[col]).date()
                        except:
                            pass
                        break
                
                # 尝试获取公司全称
                if "公司全称" in df.columns:
                    security["full_name"] = str(row["公司全称"]).strip()
                
                result.append(security)
        
        logger.info(f"解析完成，共 {len(result)} 条 {security_type} 数据")
        return result


# 创建一个全局的上交所获取器实例
sse_fetcher = SSEFetcher()