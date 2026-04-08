from sqlalchemy import Column, String, Integer, Float, Date, DateTime
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class CNIndexInfo(Base):
    """国证指数基本信息表"""
    __tablename__ = 'cnindex_info'

    指数代码 = Column(String(20), primary_key=True, comment='指数代码')
    指数简称 = Column(String(100), comment='指数简称')
    指数全称 = Column(String(255), comment='指数全称')
    英文名称 = Column(String(255), comment='英文名称')
    发布渠道 = Column(String(100), comment='发布渠道')
    深交所行情代码 = Column(String(20), comment='深交所行情代码')
    RIC = Column(String(50), comment='RIC')
    BLOOMBERG = Column(String(50), comment='BLOOMBERG')
    cni = Column(String(50), comment='.CNI')
    基日 = Column(Date, comment='基日')
    基点 = Column(Float, comment='基点')
    价格收益 = Column(String(50), comment='价格收益')
    资产类别 = Column(String(100), comment='资产类别')
    指数系列 = Column(String(100), comment='指数系列')
    指数类别 = Column(String(100), comment='指数类别')
    指数计算系统 = Column(String(100), comment='指数计算系统')
    覆盖范围 = Column(String(100), comment='覆盖范围')
    发布日期 = Column(Date, comment='发布日期')
