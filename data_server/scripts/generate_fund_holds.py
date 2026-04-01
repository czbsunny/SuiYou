import pandas as pd
import csv
import sys
import os
from datetime import datetime

# 添加项目根目录到 Python 搜索路径
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from database.init_db import SessionLocal
from models.fund_asset_allocation import FundAssetAllocation
from models.fund_portfolio_hold import FundPortfolioHold

def load_fund_holds():
    db = SessionLocal()
    try:
        query = db.query(
            FundPortfolioHold.fund_code,
            FundPortfolioHold.stock_code,
            FundPortfolioHold.weight
        ).filter(
            FundPortfolioHold.quarter == '2025年4季度股票投资明细'
        )
        
        results = query.all()
        return [(row.fund_code, row.stock_code, row.weight) for row in results if row.fund_code]
    finally:
        db.close()

def load_fund_asset_allocation():
    """
    从数据库导出基金资产配置数据
    """
    db = SessionLocal()
    try:
        # 查询2025年最后一天的基金资产配置数据
        query = db.query(
            FundAssetAllocation.fund_code,
            FundAssetAllocation.asset_type,
            FundAssetAllocation.allocation_ratio
        ).filter(
            FundAssetAllocation.last_update == datetime(2025, 12, 31)
        )
        
        results = query.all()
        
        # 写入CSV文件
        output_file = 'config\\fund_asset_allocation.csv'
        with open(output_file, 'w', newline='', encoding='utf-8-sig') as f:
            writer = csv.writer(f)
            writer.writerow(['fund_code', 'asset_type', 'allocation_ratio'])
            
            for row in results:
                fund_code, asset_type, allocation_ratio = row
                writer.writerow([fund_code, asset_type, float(allocation_ratio) if allocation_ratio else 0.0])
        
        print(f"基金资产配置数据已导出到 {output_file}，共 {len(results)} 条记录")
        return len(results)
    finally:
        db.close()

if __name__ == "__main__":
    print("=== 开始导出基金持仓和资产配置数据 ===")
    print()
    
    # 导出基金持仓数据
    data = load_fund_holds()
    pd.DataFrame(data, columns=['fund_code', 'stock_code', 'weight']).to_csv('config\\fund_portfolio_holds_2025q4.csv', index=False)
    print(f"基金持仓数据已导出到 config\\fund_portfolio_holds_2025q4.csv，共 {len(data)} 条记录")
    print()
    
    # 导出基金资产配置数据
    load_fund_asset_allocation()
    print()
    
    print("=== 导出完成 ===")