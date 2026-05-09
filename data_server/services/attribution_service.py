#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
归因服务模块
调度回归算法，并将结果存回数据库
"""

import logging
from datetime import datetime

from database.init_db import SessionLocal
from core.attribution_analyzer import AttributionAnalyzer

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class AttributionService:
    """
    归因服务类，负责调度回归算法，并将结果存回数据库
    """
    
    def __init__(self):
        self.analyzer = AttributionAnalyzer(SessionLocal())
    
    def init_analyzer(self):
        """初始化归因分析器"""
        self.analyzer.cold_start()

    def run_attribution_for_all_funds(self):
        """调用批量归因分析接口（已优化为批量模式）"""
        start_time = datetime.now()
        logger.info("开始归因分析（批量优化版）...")

        try:
            success_count = self.analyzer.daily_calibration()
            
            logger.info(f"完成，成功 {success_count} 只基金")
            logger.info(f"耗时: {(datetime.now()-start_time).total_seconds():.2f}s")
            
            return success_count

        except Exception as e:
            logger.error(f"批量归因分析失败: {e}")
            return 0

# 全局归因服务实例
attribution_service = AttributionService()