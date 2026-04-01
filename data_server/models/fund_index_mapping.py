from datetime import datetime
from sqlalchemy import Column, String, DateTime, Numeric, Index
from sqlalchemy import Enum as SQLEnum
from models.base import Base

class FundIndexMapping(Base):
    __tablename__ = 'fund_index_mapping'
    
    # 基金代码（主键的一部分）
    fund_code = Column(String(20), primary_key=True, nullable=False)
    
    # 指数代码（主键的一部分）
    index_code = Column(String(20), primary_key=True, nullable=False)
    
    # 指数名称（可选，便于前端展示和日志排查）
    index_name = Column(String(50), nullable=True)
    
    # 代理类型（控制估值方式）
    # Index -> 用指数涨跌 × 权重
    # ETF -> 用 ETF 价格 × 权重
    # Futures -> 用期货价格 × 权重
    # Other -> 自定义估值逻辑
    proxy_type = Column(
        SQLEnum('Index', 'ETF', 'Futures', 'Other', name='proxy_type_enum'),
        nullable=False,
        default='Index'
    )
    
    # 有效权重（用历史回归或公告披露数据生成，用于计算基金实时净值）
    effective_weight = Column(Numeric(precision=5, scale=4), nullable=False, default=1.0)
    
    # 汇率代码（只针对跨境基金QDII/美股基金，计算实时净值时做汇率换算）
    fx_code = Column(String(10), nullable=True)
    
    # 数据来源标识（标明基金跟踪指数的数据来源渠道，如：公告、交易所、数据服务商等）
    data_source = Column(String(20), nullable=True)
    
    # 最后更新时间（便于定期校准权重或自动回归更新）
    last_update = Column(DateTime, nullable=True)
    
    # 索引：按 index_code 查询
    __table_args__ = (
        Index('idx_index_code', 'index_code'),
    )
    
    def __repr__(self):
        return f"<FundIndexMapping(fund_code={self.fund_code}, index_code={self.index_code}, proxy_type={self.proxy_type}, weight={self.effective_weight}, data_source={self.data_source})>"
