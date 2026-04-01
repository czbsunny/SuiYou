import asyncio
import logging
from datetime import datetime
from typing import List, Dict, Any, Optional
from sqlalchemy.orm import Session

from datafetch.fund_fetcher import FundFetcher
from database.init_db import get_db
from models.fund import Fund

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 初始化基金获取器实例
fund_fetcher = FundFetcher()

async def _process_fund_info(db: Session, fund_code: str, fund: Fund, is_new: bool) -> bool:
    """
    处理单个基金信息（更新或新增）
    
    Args:
        db: 数据库会话
        fund_code: 基金代码
        fund: Fund对象（已有或新建）
        is_new: 是否为新基金
    
    Returns:
        bool: 处理是否成功
    """
    try:
        # 获取基金信息
        fund_info = await fund_fetcher.fetch_fund_profile(fund_code)

        if fund_info:    
            fund.name = fund_info.get('基金名称', '')
            fund.full_name = fund_info.get('基金全称', '')
            fund.fund_type = fund_info.get('基金类型', '')
            fund.company = fund_info.get('基金公司', '')
            fund.manager = fund_info.get('基金经理', '')
            fund.custodian_bank = fund_info.get('托管银行', '')
            fund.latest_scale = fund_info.get('最新规模', '')
            fund.risk_level = fund_info.get('风险等级', '')
            fund.rating_agency = fund_info.get('评级机构', '')
            fund.rating = fund_info.get('基金评级', '')
            fund.invest_strategy = fund_info.get('投资策略', '')
            fund.invest_target = fund_info.get('投资目标', '')
            fund.invest_scope = fund_info.get('投资范围', '')
            fund.benchmark = fund_info.get('业绩比较基准', '')
            fund.tracking_index = fund_info.get('跟踪指数', '')
            
            fund.is_on_market = fund_info.get('场内基金', False)
            fund.is_backend_fund = fund_info.get('后端基金', False)
            fund.is_valid_fund = not fund.is_backend_fund and not fund.is_on_market and fund.full_name != ''

            try:
                fund.establish_date = datetime.strptime(fund_info.get('成立时间', ''), '%Y-%m-%d').date()
            except:
                pass
        else:
            logger.warning(f"未获取到基金{fund_code}的信息")

        # 新增基金需要添加到会话中
        if is_new:
            db.add(fund)

        db.commit()
        return True
    except Exception as e:
        logger.error(f"{'新增' if is_new else '处理'}基金{fund_code}时出错: {str(e)}")
        db.rollback()
        return False

async def _process_single_fund_concurrent(fund_code: str, process_type: str, index: int) -> Dict[str, Any]:
    """
    并发处理单个基金
    
    Args:
        fund_code: 基金代码
        process_type: 处理类型（'update'或'add'）
        index: 基金在批次中的索引
    
    Returns:
        Dict: 包含处理结果的字典
    """
    # 每个并发任务使用独立的数据库会话
    db = next(get_db())
    result = {
        'fund_code': fund_code,
        'success': False,
        'error': None,
        'index': index
    }
    
    try:
        if process_type == 'update':
            # 更新模式：只处理已存在的基金
            existing_fund = db.query(Fund).filter(Fund.fund_code == fund_code).first()
            if existing_fund:
                result['success'] = await _process_fund_info(db, fund_code, existing_fund, False)
        else:
            existing_fund = db.query(Fund).filter(Fund.fund_code == fund_code).first()
            if existing_fund is None:
                existing_fund = Fund(fund_code=fund_code)
            result['success'] = await _process_fund_info(db, fund_code, existing_fund, True)
    except Exception as e:
        logger.error(f"并发处理基金{fund_code}时发生异常: {str(e)}")
        result['error'] = str(e)
    finally:
        try:
            db.close()
        except:
            pass
    
    return result

async def _process_fund_list_concurrent(fund_codes: List[str], process_type: str, batch_size: int = 10) -> int:
    """
    并发处理基金代码列表
    
    Args:
        fund_codes: 基金代码列表
        process_type: 处理类型（'update'或'add'）
        batch_size: 每批次并发处理的基金数量
    
    Returns:
        int: 处理成功的基金数量
    """
    processed_count = 0
    total_count = len(fund_codes)
    log_interval = 100 if process_type == 'update' else 10
    action = "更新" if process_type == 'update' else "新增"
    
    # 分批处理基金
    for batch_start in range(0, total_count, batch_size):
        batch_end = min(batch_start + batch_size, total_count)
        current_batch = fund_codes[batch_start:batch_end]
        logger.info(f"开始处理批次 {batch_start//batch_size + 1}/{(total_count + batch_size - 1)//batch_size}, 共{len(current_batch)}只基金")
        
        # 创建并发任务
        tasks = [
            _process_single_fund_concurrent(fund_code, process_type, i)
            for i, fund_code in enumerate(current_batch, start=batch_start)
        ]
        
        # 执行并发任务
        batch_results = await asyncio.gather(*tasks, return_exceptions=True)
        
        # 处理结果
        for result in batch_results:
            if isinstance(result, Exception):
                logger.error(f"任务执行异常: {str(result)}")
            elif result.get('success', False):
                processed_count += 1
                
                # 记录进度
                if processed_count % log_interval == 0:
                    logger.info(f"已{action}{processed_count}/{total_count}个基金")
        
        # 批次之间添加延迟，避免请求过于频繁
        if batch_end < total_count:
            await asyncio.sleep(1)
    
    return processed_count

async def update_all_fund_info():
    """
    更新所有基金基本信息
    每季度（1、4、7、10月）的周六晚上3点执行
    """
    task_name = "季度基金信息更新任务"
    logger.info(f"开始执行{task_name}，时间：{datetime.now()}")
    start_time = datetime.now()
    
    db = None
    try:
        # 获取所有基金代码
        fund_codes = await fund_fetcher.get_fund_codes()
        total_count = len(fund_codes)
        logger.info(f"获取到{total_count}个基金代码，开始并发更新")
        
        # 直接使用并发处理函数，不再需要传递数据库会话
        updated_count = await _process_fund_list_concurrent(fund_codes, 'update', batch_size=20)
        
        # 计算耗时
        duration = (datetime.now() - start_time).total_seconds()
        logger.info(f"{task_name}完成，更新了{updated_count}/{total_count}个基金，耗时：{duration:.2f}秒，时间：{datetime.now()}")
        
    except Exception as e:
        logger.error(f"{task_name}执行失败: {str(e)}")
    finally:
        # 确保数据库会话关闭
        if db:
            try:
                db.close()
            except:
                pass

async def add_new_funds():
    """
    检查并新增数据库中不存在的基金
    每天晚上3点执行
    """
    task_name = "每日新增基金任务"
    logger.info(f"开始执行{task_name}，时间：{datetime.now()}")
    start_time = datetime.now()
    
    db = None
    try:
        # 获取数据库会话以查询已存在的基金
        db = next(get_db())
        
        # 获取数据库中已存在的基金代码
        existing_fund_codes = [f[0] for f in db.query(Fund.fund_code).filter(Fund.is_valid_fund == True).all()]
        existing_set = set(existing_fund_codes)
        
        # 关闭数据库会话，后续操作使用并发函数中的独立会话
        db.close()
        db = None
        
        # 获取所有基金代码
        fund_codes = await fund_fetcher.get_fund_codes()
        
        # 找出不存在于数据库中的基金代码
        new_fund_codes = [code for code in fund_codes if code not in existing_set]
        total_new_count = len(new_fund_codes)
        logger.info(f"获取到{len(fund_codes)}个基金代码，发现{total_new_count}个新基金，开始并发处理")
        
        # 使用并发处理函数
        added_count = await _process_fund_list_concurrent(new_fund_codes, 'add', batch_size=15)
        
        # 计算耗时
        duration = (datetime.now() - start_time).total_seconds()
        logger.info(f"{task_name}完成，新增了{added_count}/{total_new_count}个基金，耗时：{duration:.2f}秒，时间：{datetime.now()}")
        
    except Exception as e:
        logger.error(f"{task_name}执行失败: {str(e)}")
    finally:
        # 确保数据库会话关闭
        if db:
            try:
                db.close()
            except:
                pass