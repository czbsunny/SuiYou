#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
任务定义模块
包含具体的任务函数，如实时估值计算、缓存预热等
"""

import logging
from datetime import datetime
from itertools import islice
from typing import Dict
import math
from elasticsearch import Elasticsearch
from utils.helpers import is_trading_day

from datafetch.realtime_fetcher import realtime_fetcher
from services.valuation_service import valuation_service
from database.init_db import SessionLocal
from models.fund_nav_realtime import FundNavRealtime

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 初始化 Elasticsearch 客户端
def init_es():
    try:
        es = Elasticsearch(
            "http://localhost:9200",
            request_timeout=5
        )
        
        if not es.ping():
            raise RuntimeError("ES ping failed")

        logger.info("ES客户端初始化成功")
        return es

    except Exception:
        logger.exception("ES客户端初始化失败")
        return None

es = init_es()

async def calculate_fund_valuation():
    """
    计算基金实时估值
    每5分钟执行一次
    """
    start_time = datetime.now()
    logger.info(f"[{start_time.strftime('%Y-%m-%d %H:%M:%S')}] 开始执行基金估值计算任务...")
        
    # 检查是否为交易日
    today = start_time.date()
    if not is_trading_day(today):
        logger.info(f"[{start_time.strftime('%Y-%m-%d %H:%M:%S')}] 今天是{today}，非交易日，跳过基金估值计算任务")
        return

    try:
        # 1. 获取市场数据
        market_data = realtime_fetcher.fetch_all_market_data()
        
        if not market_data:
            logger.warning("市场数据为空，跳过估值计算")
            return
        
        # 2. 计算基金估值
        valuation_results = valuation_service.update_market_data(market_data)
        
        if not valuation_results:
            logger.warning("估值计算结果为空")
            return
        
        # 3. 存储结果
        # 写入 Elasticsearch
        write_valuation_to_es(valuation_results)
        
        # 写入数据库
        write_valuation_to_db(valuation_results)
        
        end_time = datetime.now()
        duration = (end_time - start_time).total_seconds()
        logger.info(f"[{end_time.strftime('%Y-%m-%d %H:%M:%S')}] 基金估值计算任务完成，总耗时: {duration:.2f}秒，成功计算 {len(valuation_results)} 只基金")
        
    except Exception as e:
        logger.error(f"基金估值计算任务失败: {str(e)}")

def chunk_dict(data, size):
    """将dict分批"""
    it = iter(data.items())
    while True:
        chunk = dict(islice(it, size))
        if not chunk:
            break
        yield chunk

def write_valuation_to_es(valuation_results: Dict[str, float]):
    """
    将估值结果写入 Elasticsearch
    """
    if not es:
        logger.error("ES客户端未初始化，无法写入数据")
        return

    index_name = "fund_change_latest"

    try:

        total = len(valuation_results)
        written = 0

        for batch in chunk_dict(valuation_results, 500):
            bulk_data = []
            for fund_code, change in batch.items():
                bulk_data.append({
                    "index": {
                        "_index": index_name,
                        "_id": fund_code
                    }
                })

                bulk_data.append({
                    "fund_id": fund_code,
                    "change": change,
                    "timestamp": datetime.now().isoformat()
                })

            resp = es.bulk(body=bulk_data)
            if resp.get("errors"):
                logger.error("ES批量写入部分失败")
            written += len(batch)
            
        logger.info(f"ES写入完成 {written}/{total}")

    except Exception as e:
        logger.error(f"写入ES失败: {str(e)}")

def write_valuation_to_db(valuation_results: Dict[str, float]):
    """
    将估值结果写入数据库
    
    Args:
        valuation_results: 估值结果，格式为 {"000001": 0.3, "000002": -0.1, ...}
    """
    db = SessionLocal()
    try:
        for fund_code, change in valuation_results.items():
            try:
                # 处理 NaN 值，将其转换为 None
                if isinstance(change, float) and math.isnan(change):
                    change = None
                
                # 查找是否已存在该基金的最新记录
                latest_record = db.query(FundNavRealtime).filter(
                    FundNavRealtime.fund_code == fund_code
                ).order_by(FundNavRealtime.datetime.desc()).first()
                
                if latest_record:
                    # 更新现有记录
                    latest_record.daily_growth_rate = change
                    latest_record.datetime = datetime.now()
                    latest_record.updated_at = datetime.now()
                else:
                    # 创建新记录（注意：这里需要有nav值，暂时使用1.0作为默认值）
                    # 实际应用中，应该从其他来源获取最新的nav值
                    new_record = FundNavRealtime(
                        fund_code=fund_code,
                        nav=1.0,  # 默认值，实际应用中需要替换
                        daily_growth_rate=change,
                        weight_ready=0,  # 默认值
                        datetime=datetime.now()
                    )
                    db.add(new_record)
            except Exception as e:
                logger.error(f"处理基金 {fund_code} 时出错: {str(e)}")
                continue
        
        db.commit()
        logger.info(f"成功将 {len(valuation_results)} 条估值结果写入数据库")
        
    except Exception as e:
        logger.error(f"写入数据库失败: {str(e)}")
        db.rollback()
    finally:
        db.close()

async def warmup_cache():
    """
    预热缓存
    系统启动时执行
    """
    start_time = datetime.now()
    logger.info(f"[{start_time.strftime('%Y-%m-%d %H:%M:%S')}] 开始执行缓存预热任务...")
    
    try:
        # 预热估值服务
        valuation_service.warmup()
        
        # 预热市场数据
        realtime_fetcher.fetch_all_market_data()
        
        end_time = datetime.now()
        duration = (end_time - start_time).total_seconds()
        logger.info(f"[{end_time.strftime('%Y-%m-%d %H:%M:%S')}] 缓存预热任务完成，总耗时: {duration:.2f}秒")
        
    except Exception as e:
        logger.error(f"缓存预热任务失败: {str(e)}")

async def update_fund_weights():
    """
    更新基金权重矩阵
    每日执行一次
    """
    start_time = datetime.now()
    logger.info(f"[{start_time.strftime('%Y-%m-%d %H:%M:%S')}] 开始执行基金权重更新任务...")
    
    try:
        # 重建权重矩阵
        matrices = valuation_service.rebuild_matrix()
        
        end_time = datetime.now()
        duration = (end_time - start_time).total_seconds()
        logger.info(f"[{end_time.strftime('%Y-%m-%d %H:%M:%S')}] 基金权重更新任务完成，总耗时: {duration:.2f}秒，成功更新 {len(matrices)} 只基金的权重矩阵")
        
    except Exception as e:
        logger.error(f"基金权重更新任务失败: {str(e)}")

async def health_check():
    """
    健康检查任务
    每小时执行一次
    """
    try:
        # 检查估值引擎状态
        engine_status = valuation_service.engine.get_status()
        
        # 检查市场数据
        market_data_count = len(realtime_fetcher.fetch_all_market_data())
        
        logger.info(f"健康检查结果: 估值引擎状态={engine_status}, 市场数据数量={market_data_count}")
        
    except Exception as e:
        logger.error(f"健康检查失败: {str(e)}")
