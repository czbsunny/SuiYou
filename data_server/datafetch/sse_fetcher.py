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
    
    async def get_fund_list(self, fund_type: str = "ETF") -> Optional[pd.DataFrame]:
        """
        获取上交所基金列表
        
        Args:
            fund_type: 基金类型，可选值: "ETF", "LOF", "Reits"，默认为"ETF"
        
        Returns:
            Optional[pd.DataFrame]: 基金列表DataFrame
        """
        logger.info(f"获取上交所基金列表: {fund_type}")
        
        # 基金类型参数映射
        fund_type_params = {
            "ETF": {
                "fundType": "00",
                "subClass": "01,02,03,04,06,08,09,31,32,33,34,35,36,37,38"
            },
            "LOF": {
                "fundType": "10",
                "subClass": "11,14,15"
            },
            "Reits": {
                "fundType": "50",
                "subClass": ""
            }
        }
        
        if fund_type not in fund_type_params:
            logger.error(f"不支持的基金类型: {fund_type}")
            return None
        
        # 生成随机的 jsonCallBack 名称
        import random
        callback_name = f"jsonpCallback{random.randint(10000000, 99999999)}"
        
        url = "https://query.sse.com.cn/commonSoaQuery.do"
        params = {
            "jsonCallBack": callback_name,
            "isPagination": "true",
            "pageHelp.pageSize": "10000",
            "pageHelp.pageNo": "1",
            "pageHelp.beginPage": "1",
            "pageHelp.cacheSize": "1",
            "pageHelp.endPage": "1",
            "pagecache": "false",
            "sqlId": "FUND_LIST",
            "fundType": fund_type_params[fund_type]["fundType"],
            "subClass": fund_type_params[fund_type]["subClass"],
            "order": "",
            "_": str(int(asyncio.get_event_loop().time() * 1000)),
        }
        
        try:
            headers = self._session.headers.copy()
            headers.update({
                "Referer": "https://www.sse.com.cn/",
                "Accept": "*/*",
                "Cache-Control": "no-cache",
                "Pragma": "no-cache",
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
            
            # 处理 JSONP 响应
            data_text = resp.text
            if data_text.startswith(callback_name + "(") and data_text.endswith(")"):
                data_text = data_text[len(callback_name) + 1:-1]
            elif data_text.startswith("(") and data_text.endswith(")"):
                data_text = data_text[1:-1]
            
            data = json.loads(data_text)
            
            # 数据在 pageHelp.data 中
            if "pageHelp" not in data or "data" not in data["pageHelp"]:
                logger.error("响应数据格式不正确")
                return None
            
            df = pd.DataFrame(data["pageHelp"]["data"])
            
            # 数据清洗和格式化
            if not df.empty:
                # 转换日期字段
                if "listingDate" in df.columns:
                    df["listingDate"] = pd.to_datetime(df["listingDate"], format="%Y%m%d", errors="coerce").dt.date
            
            logger.info(f"成功获取{fund_type}基金列表，共 {len(df)} 条记录")
            return df
            
        except Exception as e:
            logger.error(f"获取{fund_type}基金列表失败: {str(e)}")
            return None
    
    async def get_convertible_bond_list(self) -> Optional[pd.DataFrame]:
        """
        获取上交所可转换债券列表
        
        Returns:
            Optional[pd.DataFrame]: 可转换债券列表DataFrame
        """
        logger.info("获取上交所可转换债券列表")
        
        # 生成随机的 jsonCallBack 名称
        import random
        callback_name = f"jsonpCallback{random.randint(10000000, 99999999)}"
        
        url = "https://query.sse.com.cn/sseQuery/commonSoaQuery.do"
        params = {
            "jsonCallBack": callback_name,
            "isPagination": "true",
            "pageHelp.pageSize": "5000",
            "pageHelp.pageNo": "1",
            "pageHelp.beginPage": "1",
            "pageHelp.cacheSize": "1",
            "pageHelp.endPage": "1",
            "sqlId": "CP_ZQ_ZQLB",
            "BOND_CODE": "",
            "BOND_TYPE": "可转换公司债券",
            "_": str(int(asyncio.get_event_loop().time() * 1000)),
        }
        
        try:
            headers = self._session.headers.copy()
            headers.update({
                "Referer": "https://www.sse.com.cn/",
                "Accept": "*/*",
                "Cache-Control": "no-cache",
                "Pragma": "no-cache",
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
            
            # 处理 JSONP 响应
            data_text = resp.text
            # 移除 jsonpCallback(...) 包装
            if data_text.startswith(callback_name + "(") and data_text.endswith(")"):
                data_text = data_text[len(callback_name) + 1:-1]
            elif data_text.startswith("(") and data_text.endswith(")"):
                data_text = data_text[1:-1]
            
            data = json.loads(data_text)
            
            # 数据在 pageHelp.data 中
            if "pageHelp" not in data or "data" not in data["pageHelp"]:
                logger.error("响应数据格式不正确")
                return None
            
            df = pd.DataFrame(data["pageHelp"]["data"])
            
            # 数据清洗和格式化
            if not df.empty:
                # 转换日期字段
                date_columns = ["LISTING_DATE_CVS", "END_DATE_CVS", "ONLINE_START_DATE_CVS", 
                              "ONLINE_END_DATE_CVS", "START_DATE", "END_DATE", "LISTING_DATE", "TRADE_DATE"]
                for col in date_columns:
                    if col in df.columns:
                        # 处理日期格式，尝试多种格式
                        if col.endswith("_CVS"):
                            df[col] = pd.to_datetime(df[col], errors="coerce").dt.date
                        else:
                            # 处理纯数字格式的日期如 20260511
                            df[col] = pd.to_datetime(df[col], format="%Y%m%d", errors="coerce").dt.date
                
                # 转换数值字段
                numeric_columns = ["FACE_RATE", "FACE_VALUE", "ISSUE_PRICE", "TERM_YEAR", 
                                 "TERM_DAY", "ISSUE_VALUE_HM", "ISSUE_VALUE"]
                for col in numeric_columns:
                    if col in df.columns:
                        df[col] = pd.to_numeric(df[col], errors="coerce")
            
            logger.info(f"成功获取可转换债券列表，共 {len(df)} 条记录")
            return df
            
        except Exception as e:
            logger.error(f"获取可转换债券列表失败: {str(e)}")
            return None


# 创建一个全局的上交所获取器实例
sse_fetcher = SSEFetcher()