import sys
import os

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

import akshare as ak
from sqlalchemy.orm import sessionmaker
from database.init_db import engine, init_db, get_db
from models.sw_index_info import SwIndexInfo
from models.fund_portfolio_hold import FundPortfolioHold

from services.market_sync_service import market_sync_service
from services.audit_service import AuditService

# 初始化数据库
def init_sw_index_data():
    print("开始初始化申万行业指数数据...")
    
    # 初始化数据库表
    init_db()
    
    # 创建数据库会话
    Session = sessionmaker(bind=engine)
    session = Session()
    
    try:
        # 1. 获取1级行业指数信息
        print("获取1级行业指数信息...")
        sw_index_first_info_df = ak.sw_index_first_info()
        print(f"获取到{len(sw_index_first_info_df)}条1级行业指数信息")
        
        # 处理并存储1级行业指数信息
        for _, row in sw_index_first_info_df.iterrows():
            sw_index_info = SwIndexInfo(
                industry_code=row['行业代码'],
                industry_name=row['行业名称'],
                parent_industry=None,  # 1级行业没有上级行业
                industry_level=1,
                component_count=row.get('成份个数'),
                static_pe=row.get('静态市盈率'),
                ttm_pe=row.get('TTM(滚动)市盈率'),
                pb=row.get('市净率'),
                static_dividend_yield=row.get('静态股息率')
            )
            session.add(sw_index_info)
        
        # 2. 获取2级行业指数信息
        print("获取2级行业指数信息...")
        sw_index_second_info_df = ak.sw_index_second_info()
        print(f"获取到{len(sw_index_second_info_df)}条2级行业指数信息")
        
        # 处理并存储2级行业指数信息
        for _, row in sw_index_second_info_df.iterrows():
            sw_index_info = SwIndexInfo(
                industry_code=row['行业代码'],
                industry_name=row['行业名称'],
                parent_industry=row.get('上级行业'),
                industry_level=2,
                component_count=row.get('成份个数'),
                static_pe=row.get('静态市盈率'),
                ttm_pe=row.get('TTM(滚动)市盈率'),
                pb=row.get('市净率'),
                static_dividend_yield=row.get('静态股息率')
            )
            session.add(sw_index_info)
        
        # 3. 获取3级行业指数信息
        print("获取3级行业指数信息...")
        sw_index_third_info_df = ak.sw_index_third_info()
        print(f"获取到{len(sw_index_third_info_df)}条3级行业指数信息")
        
        # 处理并存储3级行业指数信息
        for _, row in sw_index_third_info_df.iterrows():
            sw_index_info = SwIndexInfo(
                industry_code=row['行业代码'],
                industry_name=row['行业名称'],
                parent_industry=row.get('上级行业'),
                industry_level=3,
                component_count=row.get('成份个数'),
                static_pe=row.get('静态市盈率'),
                ttm_pe=row.get('TTM(滚动)市盈率'),
                pb=row.get('市净率'),
                static_dividend_yield=row.get('静态股息率')
            )
            session.add(sw_index_info)
        
        # 提交事务
        session.commit()
        print("申万行业指数数据初始化完成！")
        
    except Exception as e:
        print(f"初始化过程中发生错误: {e}")
        session.rollback()
    finally:
        session.close()

# 初始化一级行业历史行情数据
def init_sw_index_quotes():
    print("开始初始化一级行业历史行情数据...")


    try:
        # 调用市场行情同步服务，同步60天的数据
        success_count = market_sync_service.sync_sw_indices(days=60)
        print(f"一级行业历史行情数据初始化完成，成功同步 {success_count} 个指数")
    except Exception as e:
        print(f"初始化一级行业历史行情数据过程中发生错误: {e}")

def init_stock_quotes():
    print("开始初始化股票历史行情数据...")
    try:
        market_sync_service.cold_start_sync()
        print(f"数据初始化完成")
    except Exception as e:
        print(f"初始化历史行情数据过程中发生错误: {e}")

def init_loss_data():
    print("开始初始化股票损失数据...")

    try:
        db = next(get_db())
        service = AuditService(db)
        all_fund_codes = {r.fund_code for r in db.query(FundPortfolioHold.fund_code).filter(FundPortfolioHold.quarter.like('2025年4季度%')).distinct()}
        service.generate_historical_audit(all_fund_codes, days=60)
        print(f"基金损失数据初始化完成")
    except Exception as e:
        print(f"初始化基金损失数据过程中发生错误: {e}")

if __name__ == "__main__":
    # 初始化行业指数基本信息
    init_sw_index_data()
    # 初始化历史行情数据
    init_stock_quotes()
    # 初始化基金损失数据
    init_loss_data()