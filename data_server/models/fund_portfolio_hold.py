from datetime import datetime
from sqlalchemy import Column, String, DateTime, Integer, Float, ForeignKey, UniqueConstraint
from sqlalchemy.orm import relationship
from models.base import Base

class FundPortfolioHold(Base):
    __tablename__ = 'fund_portfolio_holds'
    __table_args__ = (
        UniqueConstraint('fund_code', 'stock_code', 'quarter', name='_fund_stock_quarter_uc'),  # 添加复合唯一约束
    )

    # 主键
    id = Column(Integer, primary_key=True, autoincrement=True)
    
    # 基金代码（外键）
    fund_code = Column(String(20), ForeignKey('funds.fund_code'), nullable=False)
    
    # 持仓信息
    stock_code = Column(String(20), nullable=False)  # 股票代码
    stock_name = Column(String(100), nullable=False)  # 股票名称
    weight = Column(Float, nullable=False)  # 占净值比例
    share = Column(Float, nullable=False)  # 持股数
    market_value = Column(Float, nullable=False)  # 持仓市值
    quarter = Column(String(50), nullable=False)  # 季度
    
    # 时间戳
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)
    
    # 关系
    fund = relationship('Fund', backref='portfolio_holds')
    
    def __repr__(self):
        return f"<FundPortfolioHold(fund_code={self.fund_code}, stock_code={self.stock_code}, quarter={self.quarter})>"
