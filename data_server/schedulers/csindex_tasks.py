#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
中证指数数据批量更新任务模块
"""

import logging
from datetime import datetime
from sqlalchemy.orm import sessionmaker
from datafetch.csindex_fetcher import csindex_fetcher
from datafetch.fund_fetcher import fund_fetcher
from models.csindex_info import CSIndexInfo
from models.fund_index_mapping import FundIndexMapping
from database.init_db import engine
import numpy as np

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 创建数据库会话
Session = sessionmaker(bind=engine)

def update_csindex_info():
    """
    更新中证指数基本信息到csindex_info表
    """
    logger.info("开始更新中证指数基本信息...")
    
    try:
        # 获取指数列表
        df = csindex_fetcher.get_index_list()
        logger.info(f"获取到 {len(df)} 条指数数据")
        
        # 处理数据并更新到数据库
        session = Session()
        try:
            for _, row in df.iterrows():
                # 检查指数是否已存在
                existing_index = session.query(CSIndexInfo).filter_by(index_code=row.get('指数代码')).first()
                
                if existing_index:
                    # 更新现有指数信息
                    existing_index.index_short_name = row.get('指数简称', '')
                    existing_index.index_full_name = row.get('指数全称', '')
                    existing_index.base_date = row.get('基日')
                    existing_index.base_point = row.get('基点')
                    existing_index.index_series = row.get('指数系列')
                    existing_index.sample_count = None if np.isnan(row.get('样本数量')) else row.get('样本数量')
                    existing_index.latest_close = row.get('最新收盘')
                    existing_index.one_month_return = row.get('近一个月收益率')
                    existing_index.asset_class = row.get('资产类别')
                    existing_index.index_hotspot = None if np.isnan(row.get('指数热点')) else row.get('指数热点')
                    existing_index.index_currency = row.get('指数币种')
                    existing_index.cooperative_index = row.get('合作指数')
                    existing_index.tracking_product = row.get('跟踪产品')
                    existing_index.index_compliance = row.get('指数合规')
                    existing_index.index_category = row.get('指数类别')
                    existing_index.release_time = row.get('发布时间')
                else:
                    # 创建新指数记录
                    new_index = CSIndexInfo(
                        index_code=row.get('指数代码'),
                        index_short_name=row.get('指数简称', ''),
                        index_full_name=row.get('指数全称', ''),
                        base_date=row.get('基日'),
                        base_point=row.get('基点'),
                        index_series=row.get('指数系列'),
                        sample_count=None if np.isnan(row.get('样本数量')) else row.get('样本数量'),
                        latest_close=row.get('最新收盘'),
                        one_month_return=row.get('近一个月收益率'),
                        asset_class=row.get('资产类别'),
                        index_hotspot=None if np.isnan(row.get('指数热点')) else row.get('指数热点'),
                        index_currency=row.get('指数币种'),
                        cooperative_index=row.get('合作指数'),
                        tracking_product=row.get('跟踪产品'),
                        index_compliance=row.get('指数合规'),
                        index_category=row.get('指数类别'),
                        release_time=row.get('发布时间')
                    )
                    session.add(new_index)
            
            session.commit()
            logger.info("中证指数基本信息更新完成")
        except Exception as e:
            session.rollback()
            logger.error(f"更新中证指数基本信息失败: {str(e)}")
        finally:
            session.close()
    except Exception as e:
        logger.error(f"获取中证指数列表失败: {str(e)}")

def update_fund_index_mapping():
    """
    更新基金跟踪指数映射到fund_index_mapping表
    """
    logger.info("开始更新基金跟踪指数映射...")
    
    try:
        # 获取基金跟踪指数信息
        df = csindex_fetcher.get_fund_tracking_index_info()
        logger.info(f"获取到 {len(df)} 条基金跟踪指数数据")
        
        # 处理数据并更新到数据库
        session = Session()
        try:
            for _, row in df.iterrows():
                fund_code = row.get('产品代码')
                index_code = row.get('标的指数代码')
                index_name = row.get('标的指数')
                print(f"处理基金 {fund_code} 指数 {index_code} 命名 {index_name}")
                if fund_code and index_code:
                    # 检查是否为国内公募基金
                    if not fund_fetcher.validate_fund_code(fund_code):
                        continue

                    # 拼接指数代码
                    formatted_index_code = f"idx.cs{index_code}"
                    
                    # 检查映射是否已存在
                    existing_mappings = session.query(FundIndexMapping).filter_by(
                        fund_code=fund_code,
                        index_code=formatted_index_code
                    ).all()
                    
                    # 检查是否存在多条记录
                    if len(existing_mappings) > 1:
                        logger.warning(f"发现多条基金跟踪指数映射记录: fund_code={fund_code}, index_code={formatted_index_code}, 记录数={len(existing_mappings)}")
                    
                    # 取第一条记录作为existing_mapping
                    existing_mapping = existing_mappings[0] if existing_mappings else None
                    
                    if existing_mapping:
                        # 更新现有映射
                        existing_mapping.index_name = index_name
                        existing_mapping.proxy_type = 'Index'
                        existing_mapping.effective_weight = 1.0
                        existing_mapping.data_source = 'csindex'
                        existing_mapping.last_update = datetime.now()
                    else:
                        # 创建新映射
                        new_mapping = FundIndexMapping(
                            fund_code=fund_code,
                            index_code=formatted_index_code,
                            index_name=index_name,
                            proxy_type='Index',
                            effective_weight=1.0,
                            data_source='csindex',
                            last_update=datetime.now()
                        )
                        session.add(new_mapping)
            
            session.commit()
            logger.info("基金跟踪指数映射更新完成")
        except Exception as e:
            session.rollback()
            logger.error(f"更新基金跟踪指数映射失败: {str(e)}")
        finally:
            session.close()
    except Exception as e:
        logger.error(f"获取基金跟踪指数信息失败: {str(e)}")

async def batch_update_csindex_data():
    """
    批量更新中证指数数据的主函数
    """
    logger.info("开始执行中证指数数据批量更新任务...")
    
    # 更新指数基本信息
    update_csindex_info()
    
    # 更新基金跟踪指数映射
    update_fund_index_mapping()
    
    logger.info("中证指数数据批量更新任务完成")
