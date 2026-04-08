from sqlalchemy import Column, Integer, String, Float, Date, DateTime
from models.base import Base

class CSIndexInfo(Base):
    """中证指数基本信息表"""
    __tablename__ = 'csindex_info'
    
    index_code = Column(String(20), primary_key=True, nullable=False, comment='指数代码')  # 指数代码，主键
    index_short_name = Column(String(100), nullable=False, comment='指数简称')  # 指数简称
    index_full_name = Column(String(255), nullable=False, comment='指数全称')  # 指数全称
    base_date = Column(Date, nullable=False, comment='基日')  # 基日
    base_point = Column(Float, nullable=False, comment='基点')  # 基点
    index_series = Column(String(100), comment='指数系列')  # 指数系列
    sample_count = Column(Integer, comment='样本数量')  # 样本数量
    latest_close = Column(Float, comment='最新收盘')  # 最新收盘
    one_month_return = Column(Float, comment='近一个月收益率')  # 近一个月收益率
    asset_class = Column(String(100), comment='资产类别')  # 资产类别
    index_hotspot = Column(String(255), comment='指数热点')  # 指数热点
    index_currency = Column(String(10), comment='指数币种')  # 指数币种
    cooperative_index = Column(String(255), comment='合作指数')  # 合作指数
    tracking_product = Column(String(255), comment='跟踪产品')  # 跟踪产品
    index_compliance = Column(String(100), nullable=False, comment='指数合规')  # 指数合规
    index_category = Column(String(100), comment='指数类别')  # 指数类别
    release_time = Column(DateTime, comment='发布时间')  # 发布时间

    def __repr__(self):
        return f"<CSIndexInfo(index_code={self.index_code}, index_short_name={self.index_short_name})>"
