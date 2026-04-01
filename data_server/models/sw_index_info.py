from datetime import datetime
from sqlalchemy import Column, String, Integer, Float, DateTime
from models.base import Base

class SwIndexInfo(Base):
    __tablename__ = 'sw_index_infos'
    
    # 行业代码
    industry_code = Column(String(20), primary_key=True)
    # 行业名称
    industry_name = Column(String(100), nullable=False)
    # 上级行业
    parent_industry = Column(String(100), nullable=True)
    # 行业级别（1级、2级、3级）
    industry_level = Column(Integer, nullable=False)
    # 成份个数
    component_count = Column(Integer, nullable=True)
    # 静态市盈率
    static_pe = Column(Float, nullable=True)
    # TTM(滚动)市盈率
    ttm_pe = Column(Float, nullable=True)
    # 市净率
    pb = Column(Float, nullable=True)
    # 静态股息率
    static_dividend_yield = Column(Float, nullable=True)
    
    # 时间戳
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)
    
    def __repr__(self):
        return f"<SwIndexInfo(industry_code={self.industry_code}, industry_name={self.industry_name}, industry_level={self.industry_level})>"

