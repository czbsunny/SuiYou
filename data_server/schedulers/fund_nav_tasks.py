import asyncio
import logging
from datetime import datetime, date, timedelta
from typing import List, Dict, Optional, Any, Set
from sqlalchemy.orm import Session
from sqlalchemy import func
from core.fund_processor import FundProcessor
from database.init_db import get_db
from models.fund import Fund
from models.fund_nav_history import FundNavHistory

# 配置日志
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

# 创建基金处理器实例
fund_processor = FundProcessor()

def get_valid_funds_map(db: Session) -> Dict[str, Fund]:
    """
    一次性获取所有有效的非货币型基金，并以字典形式返回
    键为基金代码，值为基金对象
    """
    # 一次性查询所有非货币型基金
    valid_funds = db.query(Fund).filter(
        Fund.fund_type.notin_(['货币型-普通货币', '货币型-浮动净值', '货币型']),
        Fund.is_valid_fund == True,
        Fund.latest_scale.notin_(['---', '0亿份'])
    ).all()
    
    # 转换为字典，方便快速查找
    return {fund.fund_code: fund for fund in valid_funds}

def is_valid_fund(fund_code: str, funds_map: Optional[Dict[str, Fund]] = None, 
                  db: Optional[Session] = None) -> Optional[Fund]:
    """
    检查基金是否有效（存在且非货币型）
    
    Args:
        fund_code: 基金代码
        funds_map: 基金字典映射，键为基金代码，值为基金对象
        db: 数据库会话（当funds_map为None时使用）
    
    Returns:
        基金对象或None
    """
    # 优先使用内存中的基金映射
    if funds_map is not None:
        if fund_code not in funds_map:
            logger.warning(f"基金代码{fund_code}在数据库中不存在或为货币型，跳过")
            return None
        return funds_map[fund_code]
    
    # 如果没有提供funds_map，回退到原来的单条查询方式
    # （这种情况应该尽可能避免）
    if db is None:
        logger.error(f"检查基金{fund_code}有效性时缺少必要参数")
        return None
    
    fund = db.query(Fund).filter(Fund.fund_code == fund_code).first()
    if not fund:
        logger.warning(f"基金代码{fund_code}在数据库中不存在，跳过")
        return None
    
    # 过滤货币型基金，因为它们没有净值数据
    if fund.fund_type and fund.fund_type in ['货币型-普通货币', '货币型-浮动净值']:
        logger.info(f"基金{fund_code}是货币型基金，跳过")
        return None
    
    return fund

def create_nav_record(fund_code: str, nav_date_str: str, values: List[float]) -> Optional[FundNavHistory]:
    """
    创建基金净值记录
    """
    try:
        # 解析日期
        nav_date = datetime.strptime(nav_date_str, '%Y-%m-%d').date()
        
        # 创建新记录
        return FundNavHistory(
            fund_code=fund_code,
            date=nav_date,
            nav=values[0],
            daily_growth_rate=values[1],
            accumulated_nav=values[2],
            accumulated_return_rate=values[3]
        )
    except Exception as e:
        logger.error(f"处理基金{fund_code}日期{nav_date_str}的数据时出错: {str(e)}")
        return None

async def process_fund_nav_data(
    db: Session,
    fund_code: str,
    nav_data: Dict[str, List[float]],
    date_filter: Optional[date] = None
) -> bool:
    """
    处理基金净值数据并保存到数据库
    
    Args:
        db: 数据库会话
        fund_code: 基金代码
        nav_data: 净值数据字典
        date_filter: 可选的日期过滤器，只处理指定日期的数据
    
    Returns:
        是否成功处理数据
    """
    if not nav_data:
        return False
    
    has_new_record = False
    new_records = []
    
    # 一次性获取基金在数据库中已存在的所有日期
    existing_dates = set()
    if not date_filter:  # 只有在不使用日期过滤器时才批量查询现有记录
        existing_records = db.query(FundNavHistory.date).filter(
            FundNavHistory.fund_code == fund_code
        ).all()
        existing_dates = {record.date for record in existing_records}
    
    latest_nav = None
    latest_nav_date = None
    daily_growth = None
    # 处理获取到的净值数据
    for nav_date_str, values in nav_data.items():
        new_nav = create_nav_record(fund_code, nav_date_str, values)
        if not new_nav:
            continue
        
        # 如果指定了日期过滤器，只处理匹配的日期
        if date_filter and new_nav.date != date_filter:
            continue
        
        if date_filter or new_nav.date not in existing_dates:
            new_records.append(new_nav)
            has_new_record = True
            
            # 更新 latest_nav 数据（选最新日期的）
            if not latest_nav_date or new_nav.date > latest_nav_date:
                latest_nav = new_nav.nav
                latest_nav_date = new_nav.date
                daily_growth = new_nav.daily_growth_rate
    
    if has_new_record:
        try:
            db.bulk_save_objects(new_records)

            fund_obj = db.query(Fund).filter(Fund.fund_code == fund_code).first()
            if fund_obj:
                fund_obj.latest_nav = latest_nav
                fund_obj.latest_nav_date = latest_nav_date
                fund_obj.daily_growth = daily_growth
                fund_obj.nav_updated_at = datetime.now()

            db.commit()
            return True
        except Exception as e:
            db.rollback()
            logger.error(f"提交基金{fund_code}的净值数据时出错: {str(e)}")
    
    return False

async def process_single_fund_concurrent(
    fund_code: str,
    funds_map: Dict[str, Fund],
    period: Optional[int] = None,
    target_date: Optional[date] = None
) -> Dict[str, Any]:
    """
    并发处理单个基金的数据
    
    Args:
        fund_code: 基金代码
        process_type: 处理类型，'init' 或 'update'
        funds_map: 有效的非货币型基金映射表
        period: 获取数据的周期
        target_date: 目标日期（更新时使用）
    
    Returns:
        Dict: 包含处理结果的字典
    """
    # 每个并发任务使用独立的数据库会话
    db = next(get_db())
    result = {
        'fund_code': fund_code,
        'status': 'error',
        'error': None
    }
    
    try:
        # 检查基金是否有效
        if fund_code not in funds_map:
            result['status'] = 'skipped'
            return result

        # 获取基金净值数据
        nav_data = await fund_processor.get_fund_nav(fund_code, period=period)
        
        # 处理净值数据
        success = await process_fund_nav_data(db, fund_code, nav_data, target_date)
        
        if success:
            result['status'] = 'processed'
        else:
            result['status'] = 'skipped'
            
    except Exception as e:
        logger.error(f"并发处理基金{fund_code}时出错: {str(e)}")
        result['error'] = str(e)
        result['status'] = 'error'
    finally:
        try:
            db.close()
        except:
            pass
    
    return result

async def initialize_fund_nav_data_concurrent(batch_size: int = 10):
    """
    并发初始化基金历史净值数据
    启动时执行一次，检查基金是否存在净值数据，不存在则拉取所有历史数据
    """
    logger.info(f"开始执行基金净值初始化任务（并发），时间：{datetime.now()}")
    start_time = datetime.now()
    
    db: Session = None
    try:
        # 获取数据库会话以获取有效基金
        db = next(get_db())
        
        # 一次性获取所有有效的非货币型基金
        valid_funds_map = get_valid_funds_map(db)
                
        # 查询所有基金净值记录数大于10的基金代码
        funds_with_more_than_10_records = db.query(
            FundNavHistory.fund_code
        ).group_by(
            FundNavHistory.fund_code
        ).having(
            func.count(FundNavHistory.id) > 10
        ).all()
        
        existing_funds = {record.fund_code for record in funds_with_more_than_10_records}

        # 关闭数据库会话，后续使用并发函数中的独立会话
        db.close()
        db = None
        
        valid_fund_codes = [fund_code for fund_code in list(valid_funds_map.keys()) if fund_code not in existing_funds]
        total_count = len(valid_fund_codes)
        logger.info(f"获取到{total_count}个有效的非货币型基金，开始并发初始化")
        
        # 统计信息
        initialized_count = 0
        skipped_count = 0
        error_count = 0
        
        # 分批处理基金
        for batch_start in range(0, total_count, batch_size):
            batch_end = min(batch_start + batch_size, total_count)
            current_batch = valid_fund_codes[batch_start:batch_end]
            logger.info(f"开始处理批次 {batch_start//batch_size + 1}/{(total_count + batch_size - 1)//batch_size}, 共{len(current_batch)}只基金")
            
            # 创建并发任务
            tasks = [
                process_single_fund_concurrent(fund_code, valid_funds_map, period=None)
                for fund_code in current_batch
            ]
            
            # 执行并发任务
            batch_results = await asyncio.gather(*tasks, return_exceptions=True)
            
            # 处理结果
            for result in batch_results:
                if isinstance(result, Exception):
                    logger.error(f"任务执行异常: {str(result)}")
                    error_count += 1
                else:
                    status = result.get('status', 'error')
                    if status == 'processed':
                        initialized_count += 1
                    elif status == 'skipped':
                        skipped_count += 1
                    else:
                        error_count += 1
            
            # 记录进度
            processed = min(batch_end, total_count)
            if processed % 10 == 0 or processed == total_count:
                logger.info(f"已处理{processed}/{total_count}个基金，初始化{initialized_count}个，跳过{skipped_count}个")
            
            # 批次之间添加延迟，避免请求过于频繁
            if batch_end < total_count:
                await asyncio.sleep(0.5)
        
        # 计算耗时
        duration = (datetime.now() - start_time).total_seconds()
        logger.info(f"基金净值初始化任务完成，总共{total_count}个有效基金，初始化{initialized_count}个，跳过{skipped_count}个，错误{error_count}个，耗时：{duration:.2f}秒，时间：{datetime.now()}")
        
    except Exception as e:
        logger.error(f"基金净值初始化任务执行失败: {str(e)}")
    finally:
        # 确保数据库会话关闭
        if db:
            try:
                db.close()
            except:
                pass

async def initialize_fund_nav_data():
    """
    初始化基金历史净值数据（兼容原接口，内部调用并发处理）
    启动时执行一次，检查基金是否存在净值数据，不存在则拉取所有历史数据
    """
    # 直接调用并发处理函数
    await initialize_fund_nav_data_concurrent(batch_size=10)

async def update_daily_fund_nav_concurrent(batch_size: int = 50) -> Set[str]:
    """
    并发更新基金当日净值数据
    每晚7点-凌晨12点每隔10分钟执行一次
    只更新当天净值的数据，QDII基金更新前一天的数据
    
    Returns:
        成功更新的基金代码集合
    """
    logger.info(f"开始执行基金每日净值更新任务（并发），时间：{datetime.now()}")
    start_time = datetime.now()
    
    # 检查当前时间是否在7点到24点之间
    current_hour = datetime.now().hour
    if not (19 <= current_hour <= 23):
        logger.info(f"当前时间不在19点-23点之间，跳过更新任务")
        return
    
    db: Session = None
    try:
        # 获取数据库会话以获取有效基金和最新净值日期
        db = next(get_db())
        
        # 获取今天和昨天的日期
        today = date.today()
        yesterday = today - timedelta(days=1)
        
        # 一次性获取所有有效的非货币型基金
        valid_funds_map = get_valid_funds_map(db)
        valid_fund_codes = list(valid_funds_map.keys())
        total_funds = len(valid_fund_codes)
        
        logger.info(f"获取到{total_funds}个非货币型基金需要检查")
        
        # 查询每个基金的最新净值日期
        fund_latest_dates = db.query(
            FundNavHistory.fund_code,
            func.max(FundNavHistory.date).label('latest_date')
        ).group_by(FundNavHistory.fund_code).all()
        
        # 转换为字典，方便查找
        latest_date_map = {item.fund_code: item.latest_date for item in fund_latest_dates}
        
        # 找出需要更新的基金
        funds_to_update = []
        for fund_code in valid_fund_codes:
            fund = valid_funds_map[fund_code]
            is_qdii = fund.fund_type and ('QDII' in fund.fund_type or '指数型-海外股票' == fund.fund_type)
            target_date = yesterday if is_qdii else today
            
            # 检查是否需要更新
            latest_date = latest_date_map.get(fund_code)
            if not latest_date or latest_date < target_date:
                funds_to_update.append((fund_code, target_date))
        
        # 关闭数据库会话，后续使用并发函数中的独立会话
        db.close()
        db = None
        
        total_to_update = len(funds_to_update)
        logger.info(f"需要更新的基金数量: {total_to_update}/{total_funds}")
        
        updated_count = 0
        skipped_count = 0
        error_count = 0
        total_skipped = total_funds - total_to_update  # 预先计算跳过的数量
        updated_fund_codes = set()  # 存储成功更新的基金代码
        
        # 分批处理需要更新的基金
        for batch_start in range(0, total_to_update, batch_size):
            batch_end = min(batch_start + batch_size, total_to_update)
            current_batch = funds_to_update[batch_start:batch_end]
            logger.info(f"开始处理批次 {batch_start//batch_size + 1}/{(total_to_update + batch_size - 1)//batch_size}, 共{len(current_batch)}只基金")
            
            # 创建并发任务
            tasks = [
                process_single_fund_concurrent(fund_code, valid_funds_map, period=1, target_date=target_date)
                for fund_code, target_date in current_batch
            ]
            
            # 执行并发任务
            batch_results = await asyncio.gather(*tasks, return_exceptions=True)
            
            # 处理结果
            for result in batch_results:
                if isinstance(result, Exception):
                    logger.error(f"任务执行异常: {str(result)}")
                    error_count += 1
                else:
                    status = result.get('status', 'error')
                    if status == 'processed':
                        updated_count += 1
                        # 添加到更新的基金代码集合
                        updated_fund_codes.add(result.get('fund_code'))
                    elif status == 'skipped':
                        skipped_count += 1
                        total_skipped += 1
                    else:
                        error_count += 1
            
            # 记录进度
            processed = min(batch_end, total_to_update)
            if processed % 10 == 0 or processed == total_to_update:
                logger.info(f"已处理{processed}/{total_to_update}个待更新基金，更新{updated_count}个，跳过{total_skipped}个")
            
            # 批次之间添加延迟，避免请求过于频繁
            if batch_end < total_to_update:
                await asyncio.sleep(0.5)
        
        # 计算耗时
        duration = (datetime.now() - start_time).total_seconds()
        logger.info(f"基金每日净值更新任务完成，总共{total_funds}个基金，更新{updated_count}个，跳过{total_skipped}个，错误{error_count}个，耗时：{duration:.2f}秒，时间：{datetime.now()}")
        
        # 返回成功更新的基金代码集合
        return updated_fund_codes
        
    except Exception as e:
        logger.error(f"基金每日净值更新任务执行失败: {str(e)}")
        return set()
    finally:
        # 确保数据库会话关闭
        if db:
            try:
                db.close()
            except:
                pass

async def update_daily_fund_nav() -> Set[str]:
    """
    更新基金当日净值数据（兼容原接口，内部调用并发处理）
    每晚7点-凌晨12点每隔5分钟执行一次
    只更新当天净值的数据，QDII基金更新前一天的数据
    
    Returns:
        成功更新的基金代码集合
    """
    # 直接调用并发处理函数
    return await update_daily_fund_nav_concurrent(batch_size=50)