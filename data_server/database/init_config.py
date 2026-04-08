from models.config import Config
from database.init_db import SessionLocal
import logging

logger = logging.getLogger(__name__)

def init_config():
    """初始化配置信息并写入config表"""
    
    db = SessionLocal()
    try:
        # 定义初始配置项
        configs = [
            {
                "key": "system_name",
                "value": "基金数据服务",
                "description": "系统名称"
            },
            {
                "key": "version",
                "value": "1.0.0",
                "description": "系统版本"
            },
            {
                "key": "data_source",
                "value": "multiple",
                "description": "数据来源"
            },
            {
                "key": "update_interval",
                "value": "300",
                "description": "数据更新间隔（秒）"
            },
            {
                "key": "cnindex_list",
                "value": "https://www.cnindex.com.cn/index_1020/brochures_1593/201912/P020260107583650840041.xlsx",
                "description": "国证指数列表URL"
            }
        ]
        
        for config_data in configs:
            # 检查配置是否已存在
            existing_config = db.query(Config).filter(Config.key == config_data["key"]).first()
            if not existing_config:
                # 创建新配置
                new_config = Config(
                    key=config_data["key"],
                    value=config_data["value"],
                    description=config_data["description"]
                )
                db.add(new_config)
                logger.info(f"Added new config: {config_data['key']} = {config_data['value']}")
            else:
                # 更新现有配置
                existing_config.value = config_data["value"]
                existing_config.description = config_data["description"]
                logger.info(f"Updated config: {config_data['key']} = {config_data['value']}")
        
        db.commit()
        logger.info("Config initialization completed successfully!")
    except Exception as e:
        logger.error(f"Error initializing config: {e}")
        db.rollback()
    finally:
        db.close()
