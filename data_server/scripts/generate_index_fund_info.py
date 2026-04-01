#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成指数权重和基金指数映射数据脚本
功能：
1. 从数据库导出所有指数的股票权重数据
2. 从数据库导出基金指数映射数据
3. 保存到 config 目录
"""

import csv
import sys
import os
from datetime import datetime

# 添加项目根目录到 Python 搜索路径
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from database.init_db import SessionLocal
from models.index_weights import IndexWeight
from models.fund_index_mapping import FundIndexMapping


def export_index_weights():
    """
    导出指数权重数据到CSV
    """
    db = SessionLocal()
    try:
        # 查询所有指数权重数据
        query = db.query(
            IndexWeight.index_code,
            IndexWeight.stock_code,
            IndexWeight.weight
        ).distinct()
        
        results = query.all()
        
        # 写入CSV文件
        output_file = 'config\\index_weights.csv'
        with open(output_file, 'w', newline='', encoding='utf-8-sig') as f:
            writer = csv.writer(f)
            writer.writerow(['index_code', 'stock_code', 'weight'])
            
            for row in results:
                index_code, stock_code, weight = row
                writer.writerow([index_code, stock_code, weight])
        
        print(f"指数权重数据已导出到 {output_file}，共 {len(results)} 条记录")
        return len(results)
    finally:
        db.close()


def export_fund_index_mapping():
    """
    导出基金指数映射数据到CSV
    """
    db = SessionLocal()
    try:
        # 查询所有基金指数映射数据
        query = db.query(
            FundIndexMapping.fund_code,
            FundIndexMapping.index_code,
            FundIndexMapping.effective_weight
        )
        
        results = query.all()
        
        # 写入CSV文件
        output_file = 'config\\fund_index_mapping.csv'
        with open(output_file, 'w', newline='', encoding='utf-8-sig') as f:
            writer = csv.writer(f)
            writer.writerow(['fund_code', 'index_code', 'effective_weight'])
            
            for row in results:
                fund_code, index_code, effective_weight = row
                writer.writerow([fund_code, index_code, float(effective_weight) if effective_weight else 0.0])
        
        print(f"基金指数映射数据已导出到 {output_file}，共 {len(results)} 条记录")
        return len(results)
    finally:
        db.close()


if __name__ == "__main__":
    print("=== 开始导出指数权重和基金指数映射数据 ===")
    print(f"导出时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print()
    
    # 导出指数权重数据
    index_weights_count = export_index_weights()
    print()
    
    # 导出基金指数映射数据
    fund_mapping_count = export_fund_index_mapping()
    print()
    
    print("=== 导出完成 ===")
    print(f"指数权重记录数: {index_weights_count}")
    print(f"基金指数映射记录数: {fund_mapping_count}")
