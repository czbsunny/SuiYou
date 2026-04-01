#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
手动更新基金资产配置数据脚本

此脚本用于手动执行基金资产配置数据的更新，不依赖 data_server 的启动
"""

import sys
import os

# 添加项目根目录到 Python 路径
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import logging
from schedulers.fund_asset_scheduler import update_fund_asset_allocation
import asyncio

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)

async def main():
    """主函数"""
    logger.info("开始手动执行基金资产配置更新")
    try:
        await update_fund_asset_allocation()
        logger.info("基金资产配置更新完成")
    except Exception as e:
        logger.error(f"执行更新时出错: {e}")
        raise

if __name__ == "__main__":
    asyncio.run(main())
