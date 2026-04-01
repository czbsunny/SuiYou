from datetime import datetime, date
from sqlalchemy import Column, Integer, String, Float, Date, DateTime, UniqueConstraint
from models.base import Base

class FundValuationAudit(Base):
    __tablename__ = 'fund_valuation_audit'
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    fund_code = Column(String(20), index=True, nullable=False)
    date = Column(Date, index=True, nullable=False)
    
    # 1. 真实值 (当日晚间公布)
    actual_change_pct = Column(Float)  # 真实涨跌幅 (如 0.0123)
    
    # 2. 预估值 (当日15:00计算结果)
    est_change_pct = Column(Float)     # 总估算涨跌幅
    
    # 3. 剥离计算 (用于回归的核心数据)
    top10_contribution = Column(Float) # 前十大重仓股的加权贡献合计
    # 残差项 = 真实涨跌幅 - 前十大贡献
    # 这个值就是 Lasso 回归的 因变量 Y
    residual_change = Column(Float)    
    
    # 4. 误差分析
    error = Column(Float)              # abs(actual - est)
    bias = Column(Float)               # actual - est (判断是估高了还是估低了)
    
    created_at = Column(DateTime, default=datetime.now)

    __table_args__ = (
        UniqueConstraint('fund_code', 'date', name='_fund_date_uc'),
    )

    def __repr__(self):
        return f"<FundValuationAudit(fund_code={self.fund_code}, date={self.date})>"
