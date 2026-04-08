from sqlalchemy import Column, Integer, String, Float, Date, DateTime
from models.base import Base

class CSIndexInfo(Base):
    """中证指数基本信息表"""
    __tablename__ = 'csindex_info'
    
    index_code = Column(String(20), primary_key=True, nullable=False)  # 指数代码，主键
    index_short_name = Column(String(100), nullable=False)  # 指数简称
    index_full_name = Column(String(255), nullable=False)  # 指数全称
    base_date = Column(Date, nullable=False)  # 基日
    base_point = Column(Float, nullable=False)  # 基点
    index_series = Column(String(100))  # 指数系列
    sample_count = Column(Integer)  # 样本数量
    latest_close = Column(Float)  # 最新收盘
    one_month_return = Column(Float)  # 近一个月收益率
    asset_class = Column(String(100))  # 资产类别
    index_hotspot = Column(String(255))  # 指数热点
    index_currency = Column(String(10))  # 指数币种
    cooperative_index = Column(String(255))  # 合作指数
    tracking_product = Column(String(255))  # 跟踪产品
    index_compliance = Column(String(100))  # 指数合规
    index_category = Column(String(100))  # 指数类别
    release_time = Column(DateTime)  # 发布时间

    def __repr__(self):
        return f"<CSIndexInfo(index_code={self.index_code}, index_short_name={self.index_short_name})>"
