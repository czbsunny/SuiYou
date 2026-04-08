#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
基金数据定时更新任务调度模块
使用apscheduler框架实现定时任务
"""

import logging
import asyncio
from datetime import date
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.cron import CronTrigger
from utils.helpers import is_trading_day

# 导入新增的基金定时任务模块
from schedulers.fund_tasks import update_all_fund_info, add_new_funds
from schedulers.fund_nav_tasks import initialize_fund_nav_data, update_daily_fund_nav
from schedulers.fund_industry_tasks import update_all_fund_industry_allocation
from schedulers.fund_portfolio_tasks import update_all_fund_portfolio_hold
from schedulers.stock_tasks import update_stock_data_and_calculate_fund_change
from schedulers.valuation_tasks import calculate_fund_valuation, update_fund_weights, health_check
from services.attribution_service import attribution_service
from schedulers.fund_asset_tasks import update_fund_asset_allocation
from schedulers.csindex_tasks import batch_update_csindex_data

# 配置日志，确保中文正常显示
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

async def check_and_init_fund_data():
    """
    启动时检查并初始化基金数据
    确保先初始化基金信息，再初始化基金净值
    """
    logger.info("开始执行启动时基金数据初始化流程...")
    
    # 首先检查数据库中是否有基金数据
    try:
        # 执行基金信息初始化
        await add_new_funds()
        
        # 无论是否进行了基金信息初始化，都执行基金净值初始化
        await initialize_fund_nav_data()
        logger.info("启动时基金数据初始化流程完成")
    except Exception as e:
        logger.error(f"启动时基金数据初始化失败: {str(e)}")


async def execute_fund_and_portfolio_update():
    """
    执行基金净值更新，然后根据更新的基金更新相关组合净值
    """

    # 检查是否为交易日
    today = date.today()
    if not is_trading_day(today):
        logger.info(f"今天是{today}，非交易日，跳过基金净值更新任务")
        return
    
    logger.info("开始执行基金净值更新，并准备更新相关组合")
    
    # 执行基金净值更新
    await update_daily_fund_nav()
        
class SchedulerEntry:
    """基金数据定时更新调度器"""
    
    def __init__(self):
        self.scheduler = AsyncIOScheduler()
    
    def start(self):
        """启动调度器"""
        logger.info("启动基金数据定时更新调度器...")
        
        # 添加季度更新所有基金信息任务（1、4、7、10月的周六晚上3点）
        self.scheduler.add_job(
            update_all_fund_info,
            trigger=CronTrigger(month='1,4,7,10', day_of_week='6', hour=3, minute=0),
            id='update_all_fund_info',
            name='季度更新所有基金信息',
            replace_existing=True
        )
        logger.info("添加季度更新所有基金信息的任务")
   
        # 添加每周更新基金资产配置任务（每周六凌晨4点）
        self.scheduler.add_job(
            update_fund_asset_allocation,
            trigger=CronTrigger(day_of_week='6', hour=4, minute=0),
            id='update_fund_asset_allocation',
            name='每周更新基金资产配置',
            replace_existing=True
        )
        logger.info("添加每周更新基金资产配置的任务")

        # 添加季度更新基金行业配置任务（1、4、7、10月每天凌晨2点）
        self.scheduler.add_job(
            update_all_fund_industry_allocation,
            trigger=CronTrigger(month='1,4,7,10', hour=2, minute=0),
            id='update_all_fund_industry_allocation',
            name='季度更新基金行业配置数据',
            replace_existing=True
        )
        logger.info("添加季度更新基金行业配置数据的任务")

        # 添加季度更新基金持仓数据任务（1、4、7、10月每天晚上11点）
        self.scheduler.add_job(
            update_all_fund_portfolio_hold,
            trigger=CronTrigger(month='1,4,7,10', hour=23, minute=0),
            id='update_all_fund_portfolio_hold',
            name='季度更新基金持仓数据',
            replace_existing=True
        )
        logger.info("添加季度更新基金持仓数据的任务")

        # 添加每日检查并新增基金任务（每天晚上3点）
        self.scheduler.add_job(
            check_and_init_fund_data,
            trigger=CronTrigger(hour=3, minute=0),
            id='check_and_init_fund_data',
            name='每日检查并初始化基金数据',
            replace_existing=True
        )
        logger.info("添加每日新增基金的任务")
        
        # 添加基金净值和相关组合更新任务（每晚7点-12点每隔10分钟执行）
        self.scheduler.add_job(
            execute_fund_and_portfolio_update,
            trigger=CronTrigger(hour='19-23', minute='*/10'),
            id='execute_fund_and_portfolio_update',
            name='每晚7-12点每隔10分钟更新基金净值和相关组合',
            replace_existing=True
        )
        logger.info("添加每晚7-12点每隔10分钟更新基金净值和相关组合的任务")
        
        # 添加股票数据更新和基金涨跌计算任务（交易日9:25-11:30, 13:00-15:00每5分钟执行一次）
        self.scheduler.add_job(
            update_stock_data_and_calculate_fund_change,
            trigger=CronTrigger(hour='9-11,13-14', minute='25,30,35,40,45,50,55,0,5,10,15,20'),
            id='update_stock_data_and_calculate_fund_change',
            name='股票数据更新和基金涨跌计算A股运营时间',
            replace_existing=True
        )
        
        # 添加15点后每10分钟一次的调度，4点15分最后一次
        self.scheduler.add_job(
            update_stock_data_and_calculate_fund_change,
            trigger=CronTrigger(hour='15', minute='0,10,20,30,40,50'),
            id='update_stock_data_and_calculate_fund_change_afternoon',
            name='股票数据更新和基金涨跌计算港股运营时间',
            replace_existing=True
        )
        
        # 添加4点15分的最后一次调度
        self.scheduler.add_job(
            update_stock_data_and_calculate_fund_change,
            trigger=CronTrigger(hour='16', minute='15'),
            id='update_stock_data_and_calculate_fund_change_final',
            name='股票数据更新和基金涨跌计算港股运营结束最后修正一次',
            replace_existing=True
        )
        logger.info("添加股票数据更新和基金涨跌计算的任务")
        
        # 添加基于新估值引擎的基金估值计算任务（交易日9:25-11:30, 13:00-15:00每5分钟执行一次）
        self.scheduler.add_job(
            calculate_fund_valuation,
            trigger=CronTrigger(hour='9-11,13-14', minute='25,30,35,40,45,50,55,0,5,10,15,20'),
            id='calculate_fund_valuation',
            name='基于新估值引擎的基金估值计算',
            replace_existing=True
        )
        logger.info("添加基于新估值引擎的基金估值计算任务")
        
        # 添加每日更新基金权重矩阵的任务（每天凌晨0点执行）
        self.scheduler.add_job(
            update_fund_weights,
            trigger=CronTrigger(hour=0, minute=0),
            id='update_fund_weights',
            name='每日更新基金权重矩阵',
            replace_existing=True
        )
        logger.info("添加每日更新基金权重矩阵的任务")
        
        # 添加每小时健康检查任务
        self.scheduler.add_job(
            health_check,
            trigger=CronTrigger(minute=0),
            id='health_check',
            name='每小时健康检查',
            replace_existing=True
        )
        logger.info("添加每小时健康检查任务")
        
        # 添加盘后归因分析任务（每天晚上8点执行）
        self.scheduler.add_job(
            attribution_service.run_attribution_for_all_funds,
            trigger=CronTrigger(hour=20, minute=0),
            id='run_attribution_analysis',
            name='盘后归因分析',
            replace_existing=True
        )
        logger.info("添加盘后归因分析任务")
        
        # 添加中证指数数据批量更新任务（每天凌晨2点执行）
        self.scheduler.add_job(
            batch_update_csindex_data,
            trigger=CronTrigger(hour=2, minute=0),
            id='batch_update_csindex_data',
            name='中证指数数据批量更新',
            replace_existing=True
        )
        logger.info("添加中证指数数据批量更新任务")
        
        # 启动调度器
        self.scheduler.start()
        logger.info("基金数据定时更新调度器启动成功")
    
    def stop(self):
        """停止调度器"""
        logger.info("停止基金数据定时更新调度器...")
        self.scheduler.shutdown()
        logger.info("基金数据定时更新调度器已停止")

# 创建全局调度器实例
scheduler_instance = SchedulerEntry()