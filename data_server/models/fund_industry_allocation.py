from datetime import datetime
from sqlalchemy import Column, BigInteger, String, Float, Date, DateTime, func
from models.base import Base
from utils.snowflake import snowflake
from datetime import datetime

class FundIndustryAllocation(Base):
    __tablename__ = 'fund_industry_allocation'
    id = Column(BigInteger, primary_key=True, default=lambda: snowflake.get_id())
    fund_code = Column(String(20), nullable=False, index=True)

    rank = Column(BigInteger, nullable=True)  # 序号
    industry_name = Column(String(100), nullable=True)  # 行业类别
    net_value_ratio = Column(Float, nullable=True)  # 占净值比例(%)
    market_value = Column(Float, nullable=True)  # 市值(万元)
    as_of_date = Column(Date, nullable=True)  # 截止时间

    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)