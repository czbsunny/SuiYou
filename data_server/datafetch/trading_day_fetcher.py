import requests
import random
import logging
import asyncio
from datetime import datetime
from typing import List, Dict, Optional

# 配置日志记录
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class TradingDayFetcher:
    """交易日数据获取器类，提供交易日数据相关的获取方法"""
    
    def __init__(self):
        """初始化交易日获取器，创建共享的HTTP会话"""
        # 创建共享的HTTP会话，用于复用连接
        self._session = requests.Session()
        # 设置默认headers
        self._session.headers.update({
            "Accept": "application/json, text/javascript, */*; q=0.01",
            "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Referer": "https://www.szse.cn/aboutus/calendar/index.html",
            "Sec-Fetch-Dest": "empty",
            "Sec-Fetch-Mode": "cors",
            "Sec-Fetch-Site": "same-origin",
            "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36",
            "X-Request-Type": "ajax",
            "X-Requested-With": "XMLHttpRequest",
            "sec-ch-ua": "\"Chromium\";v=\"146\", \"Not-A.Brand\";v=\"24\", \"Google Chrome\";v=\"146\"",
            "sec-ch-ua-mobile": "?0",
            "sec-ch-ua-platform": "\"macOS\""
        })
    
    async def close(self):
        """
        异步关闭HTTP会话，释放资源
        
        应该在不再使用TradingDayFetcher实例时调用此方法，以确保所有资源被正确释放。
        """
        if hasattr(self, '_session') and self._session:
            # 使用to_thread包装同步操作
            await asyncio.to_thread(self._session.close)
            self._session = None
    
    async def fetch_trading_days(self, month: str) -> Optional[List[Dict]]:
        """
        获取指定月份的交易日数据
        
        Args:
            month: 月份，格式为"YYYY-MM"
            
        Returns:
            Optional[List[Dict]]: 交易日数据列表，每个元素包含日期、星期几和交易状态
        """
        try:
            # 生成0-1之间的随机数
            random_num = random.random()
            
            # 构建请求URL
            url = f"https://www.szse.cn/api/report/exchange/onepersistenthour/monthList"
            params = {
                "month": month,
                "random": random_num
            }
            
            logger.info(f"获取{month}月份交易日数据")
            
            # 使用to_thread包装同步的会话请求
            resp = await asyncio.to_thread(
                self._session.get, 
                url, 
                params=params,
                timeout=10
            )
            
            # 检查响应状态
            resp.raise_for_status()
            
            # 解析JSON数据
            data = resp.json()
            
            if "data" in data:
                trading_days = []
                for item in data["data"]:
                    # 解析日期
                    date_str = item.get("jyrq")
                    if not date_str:
                        continue
                    
                    # 解析星期几 (zrxh: 1=周日, 7=周六)
                    zrxh = item.get("zrxh", 0)
                    # 转换为模型期望的格式 (1=周一, 7=周日)
                    if zrxh == 1:
                        weekday = 7  # 周日
                    else:
                        weekday = zrxh - 1  # 其他日期减1
                    
                    # 解析交易状态 (jybz: 1=交易日, 0=非交易日)
                    trading_status = item.get("jybz", "0") == "1"
                    
                    # 构建交易日数据字典
                    trading_day = {
                        "date": date_str,
                        "weekday": weekday,
                        "a_share_trading": trading_status,
                        "hk_trading": None,  # 港股交易状态，暂时无值
                        "us_trading": None   # 美股交易状态，暂时无值
                    }
                    
                    trading_days.append(trading_day)
                
                logger.info(f"成功获取{month}月份交易日数据，共{len(trading_days)}天")
                return trading_days
            else:
                logger.warning(f"获取{month}月份交易日数据失败：返回数据格式不正确")
                return None
                
        except Exception as e:
            logger.error(f"获取交易日数据失败: {str(e)}")
            return None
    
    async def initialize_trading_days(self, start_month: str, end_month: str) -> int:
        """
        初始化指定月份范围内的交易日数据
        
        Args:
            start_month: 开始月份，格式为"YYYY-MM"
            end_month: 结束月份，格式为"YYYY-MM"
            
        Returns:
            int: 成功初始化的交易日数据条数
        """
        try:
            # 解析开始和结束月份
            start_date = datetime.strptime(start_month, "%Y-%m")
            end_date = datetime.strptime(end_month, "%Y-%m")
            
            total_count = 0
            
            # 遍历月份范围
            current_date = start_date
            while current_date <= end_date:
                month_str = current_date.strftime("%Y-%m")
                
                # 获取该月份的交易日数据
                trading_days = await self.fetch_trading_days(month_str)
                
                if trading_days:
                    total_count += len(trading_days)
                    # 这里可以添加将数据保存到数据库的逻辑
                    logger.info(f"已获取{month_str}月份的{len(trading_days)}条交易日数据")
                
                # 移动到下一个月
                if current_date.month == 12:
                    current_date = current_date.replace(year=current_date.year + 1, month=1)
                else:
                    current_date = current_date.replace(month=current_date.month + 1)
                
                # 添加延迟，避免请求过于频繁
                await asyncio.sleep(1)
            
            logger.info(f"初始化完成，共获取{total_count}条交易日数据")
            return total_count
            
        except Exception as e:
            logger.error(f"初始化交易日数据失败: {str(e)}")
            return 0


# 创建一个全局的交易日获取器实例，方便API层调用
trading_day_fetcher = TradingDayFetcher()
