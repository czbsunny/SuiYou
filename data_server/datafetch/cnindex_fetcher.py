import requests
import pandas as pd
import io
import os

class CNIndexFetcher:
    BASE_URL = "https://www.cnindex.com.cn"

    def __init__(self):
        self.session = requests.Session()
        self.session.trust_env = False  # 禁用系统代理
        self.headers = {
            "User-Agent": "Mozilla/5.0",
            "Referer": "https://www.cnindex.com.cn/",
            "Origin": "https://www.cnindex.com.cn",
            "Accept": "application/json, text/plain, */*",
        }

    def get_index_list(self, url: str):
        """
        获取指数列表
        """
        if not url:
            print("url 不能为空")
            return pd.DataFrame()
        
        res = self.session.get(url, headers=self.headers, timeout=15)
        
        # 检查响应状态
        if res.status_code == 200:
            try:
                # 尝试解析 Excel 响应
                df = pd.read_excel(io.BytesIO(res.content))
                # 填充空值
                df = df.fillna('')
                return df
            except Exception as e:
                print(f"解析 Excel 失败: {e}")
                return pd.DataFrame()
        else:
            print(f"请求失败: {res.status_code}")
            return pd.DataFrame()

# 全局中国指数数据获取器实例
cnindex_fetcher = CNIndexFetcher()