# -*- coding: utf-8 -*-
import os
import logging
import sys
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from database.init_db import init_db
import uvicorn
from services.valuation_service import valuation_service
from services.attribution_service import attribution_service
from database.init_config import init_config

# 确保中文正常显示
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')
if hasattr(sys.stderr, 'reconfigure'):
    sys.stderr.reconfigure(encoding='utf-8')

from api.fund import router as fund_router
from schedulers.scheduler_entry import scheduler_instance

# 配置日志，确保中文正常显示
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

logging.getLogger("elastic_transport").setLevel(logging.WARNING)

# 初始化数据库
logger.info("初始化数据库...")
init_db()

# 创建FastAPI应用
app = FastAPI(
    title="基金数据服务API",
    description="提供基金数据获取和处理功能的服务",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# 配置CORS中间件
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 在生产环境中应该设置具体的域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(fund_router)

# 根路径
@app.get("/")
async def root():
    """根路径，返回API信息"""
    return {
        "message": "基金数据服务API",
        "version": "1.0.0",
        "docs": "/docs",
        "services": {
            "fund": "/api/fund",
            "portfolio": "/api/portfolio"
        }
    }

# 启动事件
@app.on_event("startup")
async def startup_event():
    """应用启动时执行"""
    logger.info("Initializing fund data service...")
    
    # 初始化配置
    logger.info("Initializing config...")
    init_config()
    
    # 初始化 attribution_service
    attribution_service.init_analyzer()

    # 初始化估值服务并预热
    logger.info("Warming up valuation service...")
    valuation_service.warmup()
    
    # 初始化并启动定时任务调度器
    scheduler_instance.start()
    
    logger.info("Fund data service initialized successfully!")

# 关闭事件
@app.on_event("shutdown")
async def shutdown_event():
    """应用关闭时执行"""
    logger.info("Shutting down fund data service...")
    # 停止定时任务调度器
    scheduler_instance.stop()

# 直接运行时启动服务
if __name__ == "__main__":
    # 获取端口和主机配置
    port = int(os.getenv("PORT", "7575"))
    host = os.getenv("HOST", "0.0.0.0")
    
    logger.info(f"Starting server on {host}:{port}")
    uvicorn.run(
        "main:app",
        host=host,
        port=port,
        reload=True  # 开发环境启用热重载
    )