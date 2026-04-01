import requests
import random
"""
基金数据处理模块

负责基金数据的获取、处理和转换功能，为API层提供数据支持。
"""
from typing import List, Dict, Optional, Union
import logging
import akshare as ak
import time
import pandas as pd
from bs4 import BeautifulSoup
import asyncio

# 配置日志记录
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class FundFetcher:
    """基金数据获取器类，提供基金数据相关的获取方法"""
    
    def __init__(self):
        """初始化基金获取器，创建共享的HTTP会话"""
        # 创建共享的HTTP会话，用于复用连接
        self._session = requests.Session()
        # 设置默认headers
        self._session.headers.update({
            "User-Agent": "Mozilla/5.0",
        })
    
    async def close(self):
        """
        异步关闭HTTP会话，释放资源
        
        应该在不再使用FundProcessor实例时调用此方法，以确保所有资源被正确释放。
        """
        if hasattr(self, '_session') and self._session:
            # 使用to_thread包装同步操作
            await asyncio.to_thread(self._session.close)
            self._session = None
    
    async def get_fund_codes(self) -> List[str]:
        """
        获取基金代码列表
        
        Returns:
            List[str]: 基金代码列表
        """
        try:
            logger.info("获取基金代码列表")
            ak_fund_list = ak.fund_name_em()
            return ak_fund_list["基金代码"].tolist()
        except Exception as e:
            logger.error(f"获取基金代码列表失败: {str(e)}")
            return []
    
    async def get_fund_info(self, fund_code: str) -> Dict[str, str]:
        """
        获取基金信息
        
        Returns:
            Dict[str, str]: 以item列为key、value列为value的字典
        """
        try:
            logger.info("获取基金信息")
            ak_fund_info = ak.fund_individual_basic_info_xq(fund_code)
            
            # 转换为以item列为key、value列为value的字典
            result = {}
            for _, row in ak_fund_info.iterrows():
                if 'item' in row and 'value' in row:
                    result[str(row['item'])] = str(row['value']) if pd.notna(row['value']) else ''
            
            return result
        except Exception as e:
            logger.error(f"获取基金信息失败: {str(e)}")
            return {}
    
    async def _fetch_fund_nav_data(self, fund_code: str) -> Dict[str, List[Union[float, None]]]:
        """
        内部方法：获取基金净值数据的核心实现
        
        Args:
            fund_code: 基金代码
            fetch_all: 是否获取全部数据
            period: 查询周期（天），当fetch_all为False时使用
            
        Returns:
            Dict[str, List[Union[float, None]]]: 基金净值数据，键为日期字符串，值为包含[单位净值, 日增长率, 累计净值, 累计收益率]的列表
        """
        try:
            # 创建结果字典
            result = {}
            try:
                time.sleep(0.1)
                nav_df = ak.fund_open_fund_info_em(symbol=fund_code, indicator="单位净值走势")
                if nav_df is not None and not nav_df.empty:
                    for _, row in nav_df.iterrows():
                        date_str = str(row['净值日期']).strip()
                        # 安全地转换为浮点数，处理可能的异常
                        try:
                            nav = float(row['单位净值']) if pd.notna(row['单位净值']) else None
                        except (ValueError, TypeError):
                            nav = None
                        
                        try:
                            daily_growth = float(row['日增长率']) if pd.notna(row['日增长率']) else None
                        except (ValueError, TypeError):
                            daily_growth = None
                        # 初始化结果列表 [单位净值, 日增长率, 累计净值, 累计收益率]
                        result[date_str] = [nav, daily_growth, None, None]
            except Exception as e:
                logger.warning(f"获取基金 {fund_code} 单位净值数据失败: {str(e)}")
                
            # 获取累计净值数据
            try:
                time.sleep(0.1)
                accumulated_nav_df = ak.fund_open_fund_info_em(symbol=fund_code, indicator="累计净值走势")
                if accumulated_nav_df is not None and not accumulated_nav_df.empty:
                    for _, row in accumulated_nav_df.iterrows():
                        date_str = str(row['净值日期']).strip()
                        # 安全地转换为浮点数，处理可能的异常
                        try:
                            accumulated_nav = float(row['累计净值']) if pd.notna(row['累计净值']) else None
                        except (ValueError, TypeError):
                            accumulated_nav = None
                        # 更新累计净值
                        if date_str in result:
                            result[date_str][2] = accumulated_nav
                        else:
                            # 如果日期不存在，初始化并设置累计净值
                            result[date_str] = [None, None, accumulated_nav, None]
            except Exception as e:
                logger.warning(f"获取基金 {fund_code} 累计净值数据失败: {str(e)}")
            
            # 获取累计收益率数据
            try:
                time.sleep(0.1)
                return_rate_df = ak.fund_open_fund_info_em(symbol=fund_code, indicator="累计收益率走势", period="成立来")
                if return_rate_df is not None and not return_rate_df.empty:
                    for _, row in return_rate_df.iterrows():
                        date_str = str(row['日期']).strip()
                        # 安全地转换为浮点数，处理可能的异常
                        try:
                            return_rate = float(row['累计收益率']) if pd.notna(row['累计收益率']) else None
                        except (ValueError, TypeError):
                            return_rate = None
                        # 更新累计收益率
                        if date_str in result:
                            result[date_str][3] = return_rate
                        else:
                            # 如果日期不存在，初始化并设置累计收益率
                            result[date_str] = [None, None, None, return_rate]
            except Exception as e:
                logger.warning(f"获取基金 {fund_code} 累计收益率数据失败: {str(e)}")
                
            # 确保数据按日期排序
            sorted_result = {date: result[date] for date in sorted(result.keys())}
            
            return sorted_result
            
        except Exception as e:
            logger.error(f"获取基金净值数据失败: 基金代码={fund_code}, 错误={str(e)}")
            return {}
    
    async def get_fund_nav(self, fund_code: str, period: int = None) -> Dict[str, List[Union[float, None]]]:
        """
        获取基金净值数据
        
        Args:
            fund_code: 基金代码
            period: 查询周期（天），当为0或None时获取所有历史数据
            
        Returns:
            Dict[str, List[Union[float, None]]]: 基金净值数据，键为日期字符串，值为包含[单位净值, 日增长率, 累计净值, 累计收益率]的列表
        """
        try:
            if period == 0 or period is None:
                return await self._fetch_fund_nav_data(fund_code)
            
            return await self.get_fund_latest_nav(fund_code, period=period)
            
        except Exception as e:
            logger.error(f"获取基金净值数据失败: 基金代码={fund_code}, 错误={str(e)}")
            return {}
    
    async def validate_fund_code(self, fund_code: str) -> bool:
        """
        验证基金代码格式是否正确
        
        Args:
            fund_code: 基金代码
            
        Returns:
            bool: 基金代码格式是否正确
        """
        # 简单的格式验证，实际应用中可能需要更复杂的验证逻辑
        if not fund_code or not isinstance(fund_code, str):
            return False
        
        # 基金代码通常是6位数字
        return fund_code.isdigit() and len(fund_code) == 6
    
    async def get_fund_latest_nav(self, fund_code: str, page_index: int = 1, period: int = 1) -> Dict[str, List[Union[float, None]]]:
        """
        获取基金最新净值
        
        Args:
            fund_code: 基金代码
            
        Returns:
            Optional[float]: 最新净值，如果获取失败返回None
        """
        try:
            url = f"http://api.fund.eastmoney.com/f10/lsjz?fundCode={fund_code}&pageIndex={page_index}&pageSize={period}"
            # 使用会话对象并更新特定请求的headers
            headers = {
                "Referer": f"http://fundf10.eastmoney.com/jjjz_{fund_code}.html"
            }
            # 使用to_thread包装同步的会话请求
            resp = await asyncio.to_thread(
                self._session.get, 
                url, 
                headers=headers
            )
            data = resp.json()
            records = data["Data"]["LSJZList"]

            df = pd.DataFrame(records)
            # 选常用字段并转类型
            df = df[["FSRQ", "DWJZ", "LJJZ", "JZZZL"]].rename(columns={
                "FSRQ": "净值日期",
                "DWJZ": "单位净值",
                "LJJZ": "累计净值",
                "JZZZL": "日增长率(%)"
            })

            # 转换为与get_fund_nav方法一致的返回格式
            result = {}
            for _, row in df.iterrows():
                date_str = str(row["净值日期"])
                # 安全地转换为浮点数，处理可能的异常
                try:
                    nav = float(row["单位净值"]) if pd.notna(row["单位净值"]) else None
                except (ValueError, TypeError):
                    nav = None
                
                try:
                    daily_growth = float(row["日增长率(%)"]) if pd.notna(row["日增长率(%)"]) else None
                except (ValueError, TypeError):
                    daily_growth = None
                
                try:
                    accumulated_nav = float(row["累计净值"]) if pd.notna(row["累计净值"]) else None
                except (ValueError, TypeError):
                    accumulated_nav = None
                # 累计收益率字段保留为空
                result[date_str] = [nav, daily_growth, accumulated_nav, None]
            
            # 确保数据按日期排序
            sorted_result = {date: result[date] for date in sorted(result.keys())}
            return sorted_result
        except Exception as e:
            logger.error(f"获取基金最新净值失败: 基金代码={fund_code}, 错误={str(e)}")
            return None
    
    async def fetch_fund_industry_allocation(self, fund_code: str, year: str) -> Optional[pd.DataFrame]:
        """
        获取基金行业配置数据
        
        Args:
            fund_code: 基金代码
            year: 年份，格式如"2023"
            
        Returns:
            Optional[pd.DataFrame]: 基金行业配置数据，若获取失败则返回None
        """
        try:
            # 直接实现天天基金网行业配置数据获取逻辑
            url = "https://api.fund.eastmoney.com/f10/HYPZ/"
            headers = {
                "Accept": "*/*",
                "Accept-Encoding": "gzip, deflate",
                "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
                "Host": "api.fund.eastmoney.com",
                "Pragma": "no-cache",
                "Referer": "https://fundf10.eastmoney.com/",
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
                "Chrome/99.0.4844.82 Safari/537.36",
            }
            params = {
                "fundCode": fund_code,
                "year": year,
                "callback": "jQuery183006997159478989867_1648016188499",
            }
            
            # 使用异步请求获取数据
            resp = await asyncio.to_thread(
                self._session.get, 
                url, 
                params=params, 
                headers=headers,
                timeout=10
            )
            
            data_text = resp.text
            
            # 解析JSON数据，移除jQuery回调包装
            json_start = data_text.find("{")
            json_end = data_text.rfind("}") + 1
            if json_start == -1 or json_end == 0:
                raise ValueError("无法解析JSON数据")
                
            json_str = data_text[json_start:json_end]
            
            # 使用demjson或json解析（需要导入demjson）
            try:
                import demjson
                data_json = demjson.decode(json_str)
            except ImportError:
                # 如果没有demjson，尝试使用简单的JSON解析
                import json
                # 移除可能的jQuery回调前缀
                if json_str.startswith('jQuery'):
                    start = json_str.find('(') + 1
                    end = json_str.rfind(')')
                    json_str = json_str[start:end]
                data_json = json.loads(json_str)
            
            # 提取行业配置数据
            temp_list = []
            if "Data" in data_json and "QuarterInfos" in data_json["Data"]:
                for item in data_json["Data"]["QuarterInfos"]:
                    if "HYPZInfo" in item:
                        temp_list.extend(item["HYPZInfo"])
            
            if not temp_list:
                logger.warning(f"基金 {fund_code} {year}年没有找到行业配置数据")
                return None
            
            # 创建DataFrame
            temp_df = pd.DataFrame(temp_list)
            temp_df.reset_index(inplace=True)
            temp_df["index"] = temp_df.index + 1
            
            # 设置列名
            expected_columns = [
                "序号", "-", "截止时间", "-", "行业类别", "市值", "-", 
                "占净值比例", "-", "-", "-", "-", "-", "-", "-", "-", "-"
            ]
            
            # 如果列数匹配，重命名列
            if len(temp_df.columns) >= len(expected_columns):
                temp_df.columns = expected_columns[:len(temp_df.columns)]
                
                # 选择需要的列
                result_df = temp_df[["序号", "行业类别", "占净值比例", "市值", "截止时间"]].copy()
            else:
                # 如果列数不匹配，使用原始列名但选择前几列
                result_df = temp_df.copy()
            
            # 数据类型转换和清理
            try:
                # 转换数值列
                if "市值" in result_df.columns:
                    result_df["市值"] = pd.to_numeric(result_df["市值"], errors="coerce").round(2)
                
                if "占净值比例" in result_df.columns:
                    # 处理可能包含百分比符号的情况
                    if result_df["占净值比例"].dtype == 'object':
                        result_df["占净值比例"] = result_df["占净值比例"].str.replace('%', '', regex=False)
                    result_df["占净值比例"] = pd.to_numeric(result_df["占净值比例"], errors="coerce").round(2)
                
                # 转换日期列
                if "截止时间" in result_df.columns:
                    result_df["截止时间"] = pd.to_datetime(result_df["截止时间"], errors="coerce")
                
                logger.info(f"成功获取基金 {fund_code} {year}年行业配置数据，共 {len(result_df)} 个行业")
                return result_df
                
            except Exception as inner_e:
                logger.warning(f"处理基金 {fund_code} {year}年行业配置数据格式时出错: {str(inner_e)}")
                # 如果处理失败，返回原始数据但确保类型安全
                return result_df.astype(str)
                
        except Exception as e:
            logger.error(f"获取基金 {fund_code} {year}年行业配置数据时出错: {str(e)}")

            return None
    
    async def fetch_fund_profile(self, fund_code: str) -> Optional[Dict[str, str]]:
        """
        获取基金概况信息
        
        Args:
            fund_code: 基金代码
            
        Returns:
            Optional[Dict[str, str]]: 基金概况信息字典，若获取失败则返回None
        """
        try:
            logger.info(f"获取基金概况信息: 基金代码={fund_code}")
            url = f"https://fundf10.eastmoney.com/jbgk_{fund_code}.html"

            # 更新会话的User-Agent，使用随机选择的UA
            session_headers = {
                "User-Agent": random.choice([
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36 Edge/119.0.0.0",
                    "Mozilla/5.0 (Linux; Android 13; SM-G998U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36",
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:119.0) Gecko/20100101 Firefox/119.0",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
                    "Mozilla/5.0 (Linux; Android 10; SM-A205U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36",
                ]),
                "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
                "Accept-Language": "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3",
                "Accept-Encoding": "gzip, deflate, br",
                "Referer": "https://fund.eastmoney.com/",
                "Connection": "keep-alive",
                "Upgrade-Insecure-Requests": "1",
                "Cache-Control": "max-age=0",
            }

            jbgk_table = None
            for attempt in range(3):
                try:
                    # 添加随机延迟，避免请求过于频繁
                    await asyncio.sleep(random.uniform(1, 3))
                    
                    # 使用to_thread包装同步的会话请求
                    resp = await asyncio.to_thread(
                        self._session.get, 
                        url, 
                        headers=session_headers,
                        timeout=5
                    )
                    resp.encoding = resp.apparent_encoding  # 防止中文乱码
                    soup = BeautifulSoup(resp.text, "lxml")

                    # 定位"基本概况"那块 table
                    tables = soup.find_all("table")
                    if len(tables) < 1:
                        raise ValueError("未找到概况表格")

                    for table in tables:
                        th_texts = [th.get_text(strip=True) for th in table.find_all("th")]
                        joined_text = "".join(th_texts)
                        
                        # 判断是否包含基金信息的关键字段
                        if any(key in joined_text for key in ["基金全称", "基金简称", "基金代码", "基金类型"]):
                            jbgk_table = table
                            break

                    # 找到基金数据表格后，跳出循环
                    if jbgk_table:
                        break

                except Exception as e:
                    logger.warning(f"[第{attempt+1}次] 获取基金概况失败: {fund_code}, 错误={e}")
                    time.sleep(1 + attempt)

            if jbgk_table is None:
                logger.error(f"获取基金概况表格失败: {fund_code}")
                return None

            cells = jbgk_table.find_all("td")
            data = [cell.get_text(strip=True) for cell in cells]

            strategy_info = {}
            boxes = soup.find_all("div", class_="box")
            for box in boxes:
                title_div = box.find("h4", class_="t")
                content_div = box.find("p")
                if not title_div or not content_div:
                    continue
                title = title_div.get_text(strip=True)
                content = content_div.get_text(strip=True)
                strategy_info[title] = content

            # 根据页面顺序提取关键信息
            is_backend_fund = False
            is_on_market = False
            code_field = data[2].replace(" ", "")
            if '、' in code_field:
                parts = code_field.split('、')
                back_code = parts[1].split('（')[0]
                if fund_code == back_code:
                    is_backend_fund = True
            elif '（主代码）' in code_field:
                is_on_market = True
            result = {
                "基金代码": fund_code,
                "基金名称": data[1],
                "基金全称": data[0],
                "基金类型": data[3],
                "基金公司": data[8],
                "托管银行": data[9],
                "基金经理": data[10],
                "成立时间": data[5].split("/")[0],
                "资产规模": data[5].split("/")[1].strip(),
                "最新规模": data[7].split('（')[0].strip(),
                "业绩比较基准": data[18],
                "跟踪标的": data[19],
                "投资目标": strategy_info.get("投资目标", ""),
                "投资范围": strategy_info.get("投资范围", ""),
                "投资策略": strategy_info.get("投资策略", ""),
                "后端基金": is_backend_fund,
                "场内基金": is_on_market
            }

            return result
            
        except Exception as e:
            logger.error(f"获取基金概况信息失败: 基金代码={fund_code}, 错误={str(e)}")
            return None

# 创建一个全局的基金获取器实例，方便API层调用
fund_fetcher = FundFetcher()