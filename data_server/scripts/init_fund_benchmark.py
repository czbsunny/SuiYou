#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
初始化基金跟踪指数脚本
按照指定的过滤条件，对所有符合条件的基金初始化跟踪指数
"""

import logging
import sys
import os

# 添加项目根目录到 Python 搜索路径
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from database.init_db import SessionLocal
from services.fund_service import FundService

# 配置日志
log_format = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
logging.basicConfig(
    level=logging.INFO,
    format=log_format
)
logger = logging.getLogger(__name__)

# 添加文件处理器，将日志写入到 output.txt
script_dir = os.path.dirname(os.path.abspath(__file__))
log_file = os.path.join(script_dir, 'output.txt')
file_handler = logging.FileHandler(log_file, mode='w', encoding='utf-8')
file_handler.setLevel(logging.INFO)
file_handler.setFormatter(logging.Formatter(log_format))
logger.addHandler(file_handler)


def get_valid_fund_codes():
    """
    从数据库获取符合条件的基金代码
    按照 benchmark_analysis.py 中的过滤条件
    """
    db = SessionLocal()
    try:
        # 构建查询
        from models.fund import Fund
        
        # 先获取所有有效基金
        query = db.query(Fund.fund_code, Fund.fund_type).filter(
            Fund.is_valid_fund == True
        )
        
        # 执行查询
        funds = query.all()
        
        # 定义需要排除的基金类型
        exclude_types = {
            '货币型-普通货币', '货币型-浮动净值', '货币型',
            '混合型-绝对收益', 'Reits', '其他', '商品',
            '债券型-中短债', '债券型-长债',
            '债券型-混合一级', '债券型-混合二级',
            '指数型-海外股票', '指数型-固收',
            'FOF-稳健型', 'FOF-进取型', 'FOF-均衡型',
            'QDII-REITs','QDII-FOF','QDII-商品','QDII-纯债','QDII-混合债'
        }
        
        # 在内存中过滤
        valid_funds = []
        for fund in funds:
            fund_code, fund_type = fund
            if fund_type not in exclude_types:
                valid_funds.append(fund_code)
        
        # 返回基金代码列表
        return valid_funds
    finally:
        db.close()


def init_fund_benchmark():
    """
    初始化基金跟踪指数
    """
    logger.info("开始初始化基金跟踪指数")
    
    # 获取符合条件的基金代码
    fund_codes = get_valid_fund_codes()
    logger.info(f"共获取到 {len(fund_codes)} 只符合条件的基金")
    
    # 遍历基金代码，初始化跟踪指数
    success_count = 0
    failed_count = 0
    failed_funds = []
    
    for fund_code in fund_codes:
        db = SessionLocal()
        try:
            fund_service = FundService(db)
            fund_service.update_benchmark_by_code(fund_code)
            db.commit()
            success_count += 1
            logger.info(f"基金 {fund_code} 跟踪指数初始化成功")
        except Exception as e:
            db.rollback()
            failed_count += 1
            failed_funds.append((fund_code, str(e)))
            logger.error(f"基金 {fund_code} 跟踪指数初始化失败: {str(e)}")
        finally:
            db.close()
    
    # 输出结果
    logger.info(f"基金跟踪指数初始化完成")
    logger.info(f"成功: {success_count} 只")
    logger.info(f"失败: {failed_count} 只")
    
    if failed_funds:
        logger.info("失败的基金列表:")
        for fund_code, error in failed_funds:
            logger.info(f"  {fund_code}: {error}")


if __name__ == "__main__":
    init_fund_benchmark()
