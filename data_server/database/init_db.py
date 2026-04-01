from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from models.base import Base

# MySQL数据库连接配置
# 格式: mysql+pymysql://username:password@host:port/database
# 注意：需要安装pymysql包: pip install pymysql
DATABASE_URL = "mysql+pymysql://root:Sunny520@localhost:3306/market?charset=utf8mb4"

# 创建数据库引擎，MySQL不需要check_same_thread参数
# 调整连接池配置，增加最大连接数以支持并发处理
engine = create_engine(
    DATABASE_URL,
    pool_size=20,  # 基础连接池大小
    max_overflow=10,  # 最大溢出连接数
    pool_timeout=30,  # 连接超时时间
    pool_recycle=1800  # 连接回收时间
)
SessionLocal = sessionmaker(bind=engine)

def init_db():
    Base.metadata.create_all(bind=engine)

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()