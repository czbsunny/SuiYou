from pydantic import BaseModel
from typing import List


class PortfolioItem(BaseModel):
    """
    组合配置项，类似PortfolioConfig模型
    用于表示投资组合中的单个基金配置
    """
    fund_code: str  # 基金代码
    weight: float  # 配置比例（0-1之间的值）


class PortfolioConfig(BaseModel):
    """
    组合配置请求模型
    包含多个基金配置项的列表
    """
    name: str  # 组合名称
    items: List[PortfolioItem]  # 基金配置项列表