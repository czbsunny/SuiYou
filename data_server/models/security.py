from datetime import datetime
from sqlalchemy import Column, String, DateTime, Date, Boolean, Text, Numeric, BigInteger, Index
from models.base import Base

class Security(Base):
    __tablename__ = 'securities'
    
    # 1. 物理主键（建议使用自增ID，Join性能更好）
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    
    # 2. 逻辑主键：采用前缀模式，如 SH.600000, US.AAPL
    # 增加唯一索引，确保全局唯一
    unified_code = Column(String(50), unique=True, nullable=False, index=True) 
    
    # 原始代码：如 600000, AAPL (方便用户搜索)
    symbol = Column(String(20), nullable=False, index=True)
    
    # 证券简称 & 全称
    name = Column(String(100), nullable=False) 
    full_name = Column(String(200), nullable=True)
    
    # 3. 市场与币种
    market = Column(String(20), nullable=False)  # SH, SZ, HK, US, OTC, CRYPTO
    currency = Column(String(10), default='CNY')  # CNY, HKD, USD
    
    # 4. 品种分类（核心枚举）
    # 建议值：STOCK, FUND_OTC, ETF, LOF, BOND_CONV, OPTION, INDEX
    security_type = Column(String(50), nullable=False, index=True)
    
    # 5. 状态管理
    # 建议值：LISTED(上市), DELISTED(退市), SUSPENDED(停牌), PRE_LIST(待上市)
    status = Column(String(20), default='LISTED')
    listed_date = Column(Date, nullable=True)
    delisted_date = Column(Date, nullable=True)

    # 6. 行业与板块
    industry = Column(String(100), nullable=True) # 行业
    sector = Column(String(100), nullable=True)   # 板块 (如主板、创业板、科创板)
    
    # 7. 财务指标（必须用 Numeric 保证精度）
    # 20位长度，4位小数
    total_shares = Column(Numeric(20, 4), nullable=True)  
    circulating_shares = Column(Numeric(20, 4), nullable=True)
    
    # 8. 关联关系（高级功能预留）
    # 例如：分级基金的母基金 ID，或者 ETF 对应的联接基金 ID
    parent_id = Column(BigInteger, nullable=True) 
    underlying_index_code = Column(String(50), nullable=True) # 标的指数代码
    
    # 9. 扩展属性 (使用 JSON 存储那些变动频繁或非通用的属性)
    # 比如：可转债的转股价、期权的行权价、基金的管理者等
    # 如果你的数据库支持 JSONB (如 PostgreSQL)，这是最好的选择
    ext_attrs = Column(Text, nullable=True) 

    # 时间戳
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)

    # 10. 复合索引优化查询
    __table_args__ = (
        Index('idx_market_symbol', 'market', 'symbol'), # 方便按市场和代码组合查询
    )

    def __repr__(self):
        return f"<Security(unified_code={self.unified_code}, name={self.name}, type={self.security_type})>"