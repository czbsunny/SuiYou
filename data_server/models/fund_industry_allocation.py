from datetime import datetime
from sqlalchemy import Column, BigInteger, String, Float, Date, DateTime, Index, func
from models.base import Base
from utils.snowflake import snowflake

class FundIndustryAllocation(Base):
    __tablename__ = 'fund_industry_allocation'
    id = Column(BigInteger, primary_key=True, default=lambda: snowflake.get_id())
    fund_code = Column(String(20), nullable=False)

    rank = Column(BigInteger, nullable=True)
    industry_name = Column(String(100), nullable=True)
    net_value_ratio = Column(Float, nullable=True)
    market_value = Column(Float, nullable=True)
    as_of_date = Column(Date, nullable=True)

    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)

    __table_args__ = (
        Index('idx_fund_code', 'fund_code'),
        Index('idx_as_of_date', 'as_of_date'),
    )