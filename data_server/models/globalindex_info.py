from sqlalchemy import Column, String
from models.base import Base

class GlobalIndexInfo(Base):
    """全球指数基本信息表"""
    __tablename__ = 'globalindex_info'
    
    index_code = Column(String(20), primary_key=True, nullable=False, comment='指数代码')  # 指数代码，主键
    index_short_name = Column(String(100), nullable=False, comment='指数简称')  # 指数简称
    index_full_name = Column(String(255), nullable=False, comment='指数全称')  # 指数全称

    def __repr__(self):
        return f"<GlobalIndexInfo(index_code={self.index_code}, index_short_name={self.index_short_name})>"
