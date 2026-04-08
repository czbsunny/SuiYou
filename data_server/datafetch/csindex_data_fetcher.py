#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
中证指数数据获取模块
用于从公开API获取中证指数数据
"""

import requests
import pandas as pd
import time
import random
import io

class CSIndexDataFetcher:

    BASE_URL = "https://www.csindex.com.cn"

    def __init__(self):
        self.session = requests.Session()
        self.session.trust_env = False  # ❗禁用系统代理（关键）

        self.headers = {
            "User-Agent": "Mozilla/5.0",
            "Referer": "https://www.csindex.com.cn/",
            "Origin": "https://www.csindex.com.cn",
            "Accept": "application/json, text/plain, */*",
        }

    # ========================
    # 1️⃣ 获取指数列表
    # ========================
    def get_index_list(self, ifTracked = None):
        url = f"{self.BASE_URL}/csindex-home/exportExcel/indexAll/CH"

        payload = {
            "sorter": {
                "sortField": "null",
                "sortOrder": None
            },
            "pager": {
                "pageNum":1,
                "pageSize":10
            },
            "searchInput": "",
            "indexFilter":{
                "ifCustomized": None,
                "ifTracked": ifTracked,
                "ifWeightCapped": None,
                "indexCompliance": None,
                "hotSpot": None,
                "indexClassify":["16","17","18","19","20","21"],
                "currency":["HKD","CNY"],
                "region": None,
                "indexSeries":["5","7","3","2","1"],
                "undefined": None
            }
        }

        res = self.session.post(url, json=payload, headers=self.headers, timeout=15)
        
        return pd.read_excel(io.BytesIO(res.content))

    # ========================
    # 2️⃣ 获取“有跟踪指数”
    # ========================
    def get_tracked_index_list(self):
        return self.get_index_list(ifTracked="1")

    # ========================
    # 3️⃣ 获取单指数行情
    # ========================
    def get_index_quote(self, index_code: str):
        url = f"{self.BASE_URL}/csindex-home/perf/index-perf-oneday"

        params = {"indexCode": index_code}

        res = self.session.get(url, params=params, headers=self.headers, timeout=10)
        resJson = res.json()
        if resJson.get("code") == "200":
            data = resJson.get("data", {})
            return data.get("intraDayHeader", {})
        else:
            return {}

    # ========================
    # 4️⃣ 批量行情（带限速）
    # ========================
    def batch_get_quotes(self, index_codes, sleep_range=(0.1, 0.2)):
        result = []

        for code in index_codes:
            try:
                data = self.get_index_quote(code)
                data["indexCode"] = code
                result.append(data)

                # ⭐关键：限速
                time.sleep(random.uniform(*sleep_range))

            except Exception as e:
                print(f"失败: {code}, {e}")
                continue

        return pd.DataFrame(result)

    # ===
    # 5️⃣ 获取基金跟踪指数信息
    # ========================
    def get_fund_tracking_index_info(self, index_code = None):
        url = f"{self.BASE_URL}/csindex-home/exportExcel/funds-tracking-index-excel/CH"

        payload = {
            "lang":"cn",
            "pager":{
                "pageNum":1,
                "pageSize":10
            },
            "fundsFilter":{
                "fundSize":None,
                "assetClass":None,
                "fundType":None,
                "coverage":None,
                "market":None,
                "fundAge":None,
                "manager":None
            }
        }
        if index_code:
            payload["searchInput"] = index_code

        res = self.session.post(url, json=payload, headers=self.headers, timeout=15)
        
        return pd.read_excel(io.BytesIO(res.content))

# 全局中证指数数据获取器实例
csindex_fetcher = CSIndexDataFetcher()
# df = csindex_fetcher.get_tracked_index_list()
# print(df.head())

# quote = csindex_fetcher.get_index_quote("H30463")
# print(quote)

# df = csindex_fetcher.get_fund_tracking_index_info()
# print(df.head())