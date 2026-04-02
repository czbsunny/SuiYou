from datetime import datetime
from sqlalchemy import Column, String, Date, Float, DateTime
from models.base import Base

class FundTrackingIndex(Base):
    __tablename__ = 'fund_tracking_index'
    
    # 产品代码（主键）
    product_code = Column(String(20), primary_key=True, nullable=False)
    
    # 产品名称
    product_name = Column(String(100), nullable=True)
    
    # 标的指数代码
    index_code = Column(String(20), nullable=True)
    
    # 标的指数
    index_name = Column(String(100), nullable=True)
    
    # 资产类别
    asset_class = Column(String(50), nullable=True)
    
    # 产品类型
    product_type = Column(String(50), nullable=True)
    
    # 上市地
    listing_location = Column(String(50), nullable=True)
    
    # 资产净值（亿元）
    net_assets = Column(Float, nullable=True)
    
    # 成立日期
    establish_date = Column(Date, nullable=True)
    
    # 管理人
    manager = Column(String(100), nullable=True)
    
    # 时间戳
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)
    
    def __repr__(self):
        return f"<FundTrackingIndex(product_code={self.product_code}, product_name={self.product_name}, index_code={self.index_code})>"
