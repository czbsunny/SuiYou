from datetime import date
from sqlalchemy import Column, Integer, String, Float, Date, UniqueConstraint
from models.base import Base

class IndexDailyQuote(Base):
    """指数历史行情表 (行业指数、宽基指数、海外指数)"""
    __tablename__ = 'index_daily_quotes'
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    index_code = Column(String(20), index=True, nullable=False) # idx.sh000300
    date = Column(Date, index=True, nullable=False)
    
    close = Column(Float)         # 指数收盘点位
    change_pct = Column(Float)    # 当日涨跌幅
    
    __table_args__ = (
        UniqueConstraint('index_code', 'date', name='_index_date_uc'),
    )

    def __repr__(self):
        return f"<IndexDailyQuote(index_code={self.index_code}, date={self.date}, close={self.close})>"
