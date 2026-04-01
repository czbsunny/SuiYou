from dataclasses import dataclass
from typing import List
from .holding import Holding
from .benchmark import BenchmarkItem

@dataclass
class FundProfile:
    fund_code: str
    fund_type: str
    stock_ratio: float
    top_holdings: List[Holding]
    benchmark_items: List[BenchmarkItem]
