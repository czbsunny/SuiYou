#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
国证指数数据批量更新任务模块
"""

import logging
from sqlalchemy.orm import sessionmaker
from datafetch.cnindex_fetcher import cnindex_fetcher
from models.cnindex_info import CNIndexInfo
from models.config import Config
from database.init_db import engine

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 创建数据库会话
Session = sessionmaker(bind=engine)

def get_config_value(key):
    """
    从配置表中获取指定key的value值
    """
    session = Session()
    try:
        config = session.query(Config).filter_by(key=key).first()
        if config:
            return config.value
        return None
    except Exception as e:
        logger.error(f"获取配置失败: {str(e)}")
        return None
    finally:
        session.close()

def update_cnindex_info():
    """
    更新国证指数基本信息到cnindex_info表
    """
    logger.info("开始更新国证指数基本信息...")
    
    try:
        # 从配置表中获取国证指数列表URL
        url = get_config_value("cnindex_list")
        if not url:
            logger.error("国证指数列表URL配置不存在")
            return
        
        # 获取指数列表
        df = cnindex_fetcher.get_index_list(url)
        logger.info(f"获取到 {len(df)} 条指数数据")
        
        # 处理数据并更新到数据库
        session = Session()
        try:
            for _, row in df.iterrows():
                # 检查指数是否已存在
                existing_index = session.query(CNIndexInfo).filter_by(指数代码=row.get('指数代码')).first()
                
                if existing_index:
                    # 更新现有指数信息
                    existing_index.指数简称 = row.get('指数简称', '')
                    existing_index.指数全称 = row.get('指数全称', '')
                    existing_index.英文名称 = row.get('英文名称', '')
                    existing_index.发布渠道 = row.get('发布渠道', '')
                    existing_index.深交所行情代码 = row.get('深交所行情代码', '')
                    existing_index.RIC = row.get('RIC', '')
                    existing_index.BLOOMBERG = row.get('BLOOMBERG', '')
                    existing_index.cni = row.get('.CNI', '')
                    existing_index.基日 = row.get('基日')
                    existing_index.基点 = row.get('基点')
                    existing_index.价格收益 = row.get('价格收益', '')
                    existing_index.资产类别 = row.get('资产类别', '')
                    existing_index.指数系列 = row.get('指数系列', '')
                    existing_index.指数类别 = row.get('指数类别', '')
                    existing_index.指数计算系统 = row.get('指数计算系统', '')
                    existing_index.覆盖范围 = row.get('覆盖范围', '')
                    existing_index.发布日期 = row.get('发布日期')
                else:
                    # 创建新指数记录
                    new_index = CNIndexInfo(
                        指数代码=row.get('指数代码'),
                        指数简称=row.get('指数简称', ''),
                        指数全称=row.get('指数全称', ''),
                        英文名称=row.get('英文名称', ''),
                        发布渠道=row.get('发布渠道', ''),
                        深交所行情代码=row.get('深交所行情代码', ''),
                        RIC=row.get('RIC', ''),
                        BLOOMBERG=row.get('BLOOMBERG', ''),
                        cni=row.get('.CNI', ''),
                        基日=row.get('基日'),
                        基点=row.get('基点'),
                        价格收益=row.get('价格收益', ''),
                        资产类别=row.get('资产类别', ''),
                        指数系列=row.get('指数系列', ''),
                        指数类别=row.get('指数类别', ''),
                        指数计算系统=row.get('指数计算系统', ''),
                        覆盖范围=row.get('覆盖范围', ''),
                        发布日期=row.get('发布日期')
                    )
                    session.add(new_index)
            
            session.commit()
            logger.info("国证指数基本信息更新完成")
        except Exception as e:
            session.rollback()
            logger.error(f"更新国证指数基本信息失败: {str(e)}")
        finally:
            session.close()
    except Exception as e:
        logger.error(f"获取国证指数列表失败: {str(e)}")

async def batch_update_cnindex_data():
    """
    批量更新国证指数数据的主函数
    """
    logger.info("开始执行国证指数数据批量更新任务...")
    
    # 更新指数基本信息
    update_cnindex_info()
    
    logger.info("国证指数数据批量更新任务完成")
