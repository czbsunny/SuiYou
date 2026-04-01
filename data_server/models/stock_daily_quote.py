from datetime import date
from sqlalchemy import Column, Integer, String, Float, Date, UniqueConstraint
from models.base import Base

class StockDailyQuote(Base):
    """个股历史行情表"""
    __tablename__ = 'stock_daily_quotes'
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    stock_code = Column(String(20), index=True, nullable=False) # sh.600519
    date = Column(Date, index=True, nullable=False)
    
    close = Column(Float)         # 当日收盘价
    adj_close = Column(Float)     # 【重要】后复权收盘价 (用于计算跨季度累计涨幅)
    change_pct = Column(Float)    # 当日涨跌幅
    
    __table_args__ = (
        UniqueConstraint('stock_code', 'date', name='_stock_date_uc'),
    )

    def __repr__(self):
        return f"<StockDailyQuote(stock_code={self.stock_code}, date={self.date}, close={self.close})>"
