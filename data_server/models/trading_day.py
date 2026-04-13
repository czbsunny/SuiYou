from datetime import datetime
from sqlalchemy import Column, Date, Boolean, Integer, DateTime
from models.base import Base

class TradingDay(Base):
    __tablename__ = 'trading_days'
    
    # 主键：日期
    date = Column(Date, primary_key=True)
    
    # 星期几 (1-7, 1=周一, 7=周日)
    weekday = Column(Integer, nullable=False)
    
    # A股是否交易日 (1=是, 0=否)
    a_share_trading = Column(Boolean, nullable=False)
    
    # 港股是否交易日 (可以有但无值)
    hk_trading = Column(Boolean, nullable=True)
    
    # 美股是否交易日 (可以有但无值)
    us_trading = Column(Boolean, nullable=True)
    
    # 时间戳
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)
    
    def __repr__(self):
        return f"<TradingDay(date={self.date}, weekday={self.weekday}, a_share_trading={self.a_share_trading})>"
