from datetime import datetime
from sqlalchemy import Column, String, Numeric, DateTime, Index
from models.base import Base

class FundAssetAllocation(Base):
    __tablename__ = 'fund_asset_allocation'
    
    # 基金代码
    fund_code = Column(String(20), primary_key=True, nullable=False)
    
    # 资产类型（如：股票、债券、现金、其他等）
    asset_type = Column(String(20), primary_key=True, nullable=False)
    
    # 资产占比（百分比，如：0.65表示65%）
    allocation_ratio = Column(Numeric(precision=5, scale=4), nullable=False)
    
    # 最后更新时间
    last_update = Column(DateTime, nullable=True, default=datetime.now)
    
    # 索引：按基金代码查询
    __table_args__ = (
        Index('idx_fund_code', 'fund_code'),
    )
    
    def __repr__(self):
        return f"<FundAssetAllocation(fund_code={self.fund_code}, asset_type={self.asset_type}, allocation_ratio={self.allocation_ratio})>"
