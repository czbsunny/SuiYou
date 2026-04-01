from datetime import datetime
from sqlalchemy import Column, String, DateTime, Date, Float, UniqueConstraint
from models.base import Base

class IndexWeight(Base):
    __tablename__ = 'index_weights'
    
    # 复合主键：指数代码 + 股票代码 + 生效日期
    # 指数代码
    index_code = Column(String(20), nullable=False, primary_key=True)
    # 成份券代码（股票代码）
    stock_code = Column(String(20), nullable=False, primary_key=True)
    # 日期（权重生效日）
    effective_date = Column(Date, nullable=False, primary_key=True)
    
    # 指数名称
    index_name = Column(String(100), nullable=True)
    # 指数英文名称
    index_name_en = Column(String(200), nullable=True)
    # 成份券名称（股票名称）
    stock_name = Column(String(100), nullable=True)
    # 成份券英文名称
    stock_name_en = Column(String(200), nullable=True)
    # 交易所
    exchange = Column(String(50), nullable=True)
    # 交易所英文名称
    exchange_en = Column(String(100), nullable=True)
    # 权重（%）
    weight = Column(Float, nullable=True)
    # 数据来源
    source = Column(String(50), nullable=True, default='csindex')
    # 时间戳
    created_at = Column(DateTime, default=datetime.now)
    
    def __repr__(self):
        return f"<IndexWeight(index_code={self.index_code}, stock_code={self.stock_code}, effective_date={self.effective_date}, weight={self.weight})>"
