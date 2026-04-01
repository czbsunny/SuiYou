import logging
from database.init_db import SessionLocal
from models.fund_asset_allocation import FundAssetAllocation
from datetime import datetime, timedelta
import akshare as ak
from models.fund import Fund

logger = logging.getLogger(__name__)

async def update_fund_asset_allocation():
    """
    更新基金资产配置数据
    这里可以根据实际需求实现数据获取和更新逻辑
    """
    logger.info("开始更新基金资产配置数据")
    
    # 计算上一个季度最后一天
    last_quarter_end = get_last_quarter_end()
    logger.info(f"上一个季度最后一天: {last_quarter_end}")
    
    # 格式化为 akshare 要求的日期格式
    date_str = last_quarter_end.strftime("%Y%m%d")
    logger.info(f"使用的日期参数: {date_str}")
    
    # 获取所有基金代码
    db_temp = SessionLocal()
    try:
        fund_codes = get_all_fund_codes(db_temp)
        logger.info(f"共获取到 {len(fund_codes)} 只基金")
        
        # 查询需要更新的基金代码（last_update < 上一个季度最后一天）
        funds_to_update = get_funds_to_update(db_temp, last_quarter_end)
        logger.info(f"需要更新的基金数量: {len(funds_to_update)}")
    finally:
        db_temp.close()
    
    # 遍历需要更新的基金
    for fund_code in funds_to_update:
        # 为每个基金创建独立的数据库会话
        db = SessionLocal()
        try:
            # 使用 akshare 获取基金资产配置数据
            fund_individual_detail_hold_xq_df = ak.fund_individual_detail_hold_xq(symbol=fund_code, date=date_str)
            
            if not fund_individual_detail_hold_xq_df.empty:
                # 处理数据并更新到数据库
                update_fund_asset_data(db, fund_code, fund_individual_detail_hold_xq_df, last_quarter_end)
                logger.info(f"基金 {fund_code} 资产配置数据更新成功")
            else:
                logger.warning(f"基金 {fund_code} 无资产配置数据")
        except KeyError as e:
            if 'data' in str(e):
                logger.warning(f"基金 {fund_code} 无资产配置数据（API 返回无 data 字段）")
            else:
                logger.error(f"更新基金 {fund_code} 资产配置数据时出错: {e}")
            # 发生错误时回滚事务
            db.rollback()
        except Exception as e:
            logger.error(f"更新基金 {fund_code} 资产配置数据时出错: {e}")
            # 发生错误时回滚事务
            db.rollback()
        finally:
            db.close()
    
    logger.info("基金资产配置数据更新完成")


def get_last_quarter_end():
    """
    计算上一个季度最后一天
    """
    now = datetime.now()
    # 计算当前季度的第一天
    current_quarter_start = datetime(now.year, ((now.month - 1) // 3) * 3 + 1, 1)
    # 上一个季度的最后一天是当前季度第一天减一天
    last_quarter_end = current_quarter_start - timedelta(days=1)
    
    return last_quarter_end


def get_all_fund_codes(db):
    """
    从数据库获取所有有效基金代码
    """
    funds = db.query(Fund.fund_code).filter(Fund.is_valid_fund == True).all()
    return [fund[0] for fund in funds]


def get_funds_to_update(db, last_quarter_end):
    """
    查询需要更新的基金代码（last_update < 上一个季度最后一天）
    """
    
    # 查询所有基金代码
    all_fund_codes = get_all_fund_codes(db)
    
    # 查询已经更新过的基金代码（last_update >= 上一个季度最后一天）
    updated_funds = db.query(FundAssetAllocation.fund_code).filter(
        FundAssetAllocation.last_update >= last_quarter_end
    ).distinct().all()
    updated_fund_codes = {fund[0] for fund in updated_funds}
    
    # 需要更新的基金代码 = 所有基金代码 - 已经更新过的基金代码
    funds_to_update = [code for code in all_fund_codes if code not in updated_fund_codes]
    
    return funds_to_update


def update_fund_asset_data(db, fund_code, df, last_quarter_end):
    """
    更新单个基金的资产配置数据
    """
    # 遍历数据
    for _, row in df.iterrows():
        asset_type = row.get('资产类型')
        allocation_ratio = row.get('仓位占比')
        
        if asset_type and allocation_ratio is not None:
            # 将百分比转换为小数（除以100）
            allocation_ratio_decimal = allocation_ratio / 100
            
            # 检查是否已存在记录
            existing = db.query(FundAssetAllocation).filter(
                FundAssetAllocation.fund_code == fund_code,
                FundAssetAllocation.asset_type == asset_type
            ).first()
            
            if existing:
                # 更新现有记录
                existing.allocation_ratio = allocation_ratio_decimal
                existing.last_update = last_quarter_end
            else:
                # 创建新记录
                new_asset = FundAssetAllocation(
                    fund_code=fund_code,
                    asset_type=asset_type,
                    allocation_ratio=allocation_ratio_decimal,
                    last_update=last_quarter_end
                )
                db.add(new_asset)
    
    # 提交事务
    db.commit()