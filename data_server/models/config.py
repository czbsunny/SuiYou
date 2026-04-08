from sqlalchemy import Column, Integer, String, Text, DateTime
from sqlalchemy.sql import func
from .base import Base

class Config(Base):
    __tablename__ = 'config'
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    key = Column(String(100), unique=True, nullable=False, comment='配置键')
    value = Column(Text, nullable=False, comment='配置值')
    description = Column(Text, comment='配置描述')
    created_at = Column(DateTime(timezone=True), server_default=func.now(), comment='创建时间')
    updated_at = Column(DateTime(timezone=True), onupdate=func.now(), comment='更新时间')
    
    def __repr__(self):
        return f"<Config(key='{self.key}', value='{self.value}')>"
