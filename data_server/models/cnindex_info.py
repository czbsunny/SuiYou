from sqlalchemy import Column, String, Integer, Float, Date, DateTime
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class CNIndexInfo(Base):
    """国证指数基本信息表"""
    __tablename__ = 'cnindex_info'

    index_code = Column(String(20), primary_key=True, comment='指数代码')
    index_name = Column(String(100), comment='指数简称')
    index_full_name = Column(String(255), comment='指数全称')
    english_name = Column(String(255), comment='英文名称')
    release_channel = Column(String(100), comment='发布渠道')
    szse_code = Column(String(20), comment='深交所行情代码')
    ric = Column(String(50), comment='RIC')
    bloomberg = Column(String(50), comment='BLOOMBERG')
    cni = Column(String(50), comment='.CNI')
    base_date = Column(Date, comment='基日')
    base_point = Column(Float, comment='基点')
    price_yield = Column(String(50), comment='价格收益')
    asset_class = Column(String(100), comment='资产类别')
    index_series = Column(String(100), comment='指数系列')
    index_category = Column(String(100), comment='指数类别')
    index_calc_system = Column(String(100), comment='指数计算系统')
    coverage_range = Column(String(100), comment='覆盖范围')
    release_time = Column(DateTime, comment='发布时间')

