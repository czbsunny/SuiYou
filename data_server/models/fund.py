from datetime import datetime
from sqlalchemy import Column, String, DateTime, Text, Date, Boolean, Float
from models.base import Base

class Fund(Base):
    __tablename__ = 'funds'
    
    # 基础信息
    fund_code = Column(String(20), primary_key=True)
    name = Column(String(100), nullable=True)  # 基金名称
    full_name = Column(String(200), nullable=True)  # 基金全称
    fund_type = Column(String(50), nullable=True)  # 基金类型
    
    # 发行信息
    company = Column(String(100), nullable=True)  # 基金公司
    manager = Column(String(100), nullable=True)  # 基金经理
    establish_date = Column(Date, nullable=True)  # 成立日期

    # 规模信息
    latest_scale = Column(String(50), nullable=True)  # 最新规模（用户提供的格式）

    # 其他信息
    rating_agency = Column(String(50), nullable=True)  # 评级机构
    custodian_bank = Column(String(100), nullable=True)  # 托管银行
    rating = Column(String(50), nullable=True)  # 基金评级
    investment_strategy = Column(Text, nullable=True)  # 投资策略
    investment_goal = Column(Text, nullable=True)  # 投资目标
    benchmark = Column(String(1000), nullable=True)  # 业绩比较基准
    investment_scope = Column(Text, nullable=True)  # 投资范围
    risk_level = Column(String(20), nullable=True)  # 风险等级

    is_valid_fund = Column(Boolean, nullable=True)  # 是否为有效基金

    # 净值信息
    latest_nav = Column(Float, nullable=True)  # 最新净值
    latest_nav_date = Column(Date, nullable=True)  # 最新净值日期
    daily_growth = Column(Float, nullable=True)  # 日增长率
    nav_updated_at = Column(DateTime, nullable=True)  # 净值更新时间

    # 时间戳
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)

    # 扩展信息
    tracking_index = Column(String(500), nullable=True)  # 跟踪指数
    is_on_market = Column(Boolean, nullable=True)  # 是否场内基金
    is_backend_fund = Column(Boolean, nullable=True)  # 是否后端基金

    def __repr__(self):
        return f"<Fund(fund_code={self.fund_code}, name={self.name}, company={self.company})>"