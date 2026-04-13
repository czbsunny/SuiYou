import asyncio
import logging
from datetime import datetime, date, timedelta
from typing import List, Dict, Optional, Any, Set, Tuple
from sqlalchemy.orm import Session
from sqlalchemy import func
from elasticsearch import Elasticsearch
from datafetch.fund_fetcher import FundFetcher
from database.init_db import get_db
from models.fund import Fund
from models.fund_nav_history import FundNavHistory

# 配置日志
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

# 初始化ES客户端
def init_es():
    try:
        es = Elasticsearch(
            "http://localhost:9200",
            request_timeout=5
        )

        if not es.ping():
            raise RuntimeError("ES ping failed")

        logger.info("ES客户端初始化成功")
        return es

    except Exception:
        logger.exception("ES客户端初始化失败")
        return None

es = init_es()

# 假设的获取器实例
fund_fetcher = FundFetcher()

def get_valid_funds_map(db: Session) -> Dict[str, Fund]:
    """获取所有有效的非货币型基金映射"""
    valid_funds = db.query(Fund).filter(
        Fund.fund_type.notin_(['货币型-普通货币', '货币型-浮动净值', '货币型']),
        Fund.is_valid_fund == True,
        Fund.latest_scale.notin_(['---', '0亿份'])
    ).all()
    return {fund.fund_code: fund for fund in valid_funds}

async def save_nav_to_db(db: Session, fund_code: str, nav_data: Dict[str, List[float]], target_date: Optional[date] = None) -> bool:
    """
    通用逻辑：将抓取到的净值字典处理并保存到数据库
    nav_data 格式: { "2023-10-27": [净值, 涨跌幅, 累计净值, 累计收益率], ... }
    """
    if not nav_data:
        return False

    # 1. 筛选出需要插入的记录
    new_records = []
    latest_record = None

    # 获取已存在的日期，避免重复插入（如果是全量初始化，数据量大，建议按需查询）
    # 这里为了性能，如果是单日更新且指定了target_date，我们直接判断
    if target_date:
        # 检查数据库是否已有该日期记录
        exists = db.query(FundNavHistory.id).filter(
            FundNavHistory.fund_code == fund_code,
            FundNavHistory.date == target_date
        ).first()
        if exists:
            return False
        
        # 从返回的多条数据中寻找目标日期
        target_date_str = target_date.strftime('%Y-%m-%d')
        if target_date_str in nav_data:
            values = nav_data[target_date_str]
            latest_record = create_nav_record(fund_code, target_date_str, values)
            if latest_record:
                new_records.append(latest_record)
    else:
        # 初始化逻辑：处理所有日期，需排重
        existing_dates = {d[0] for d in db.query(FundNavHistory.date).filter(FundNavHistory.fund_code == fund_code).all()}
        for date_str, values in nav_data.items():
            record = create_nav_record(fund_code, date_str, values)
            if record and record.date not in existing_dates:
                new_records.append(record)
                if not latest_record or record.date > latest_record.date:
                    latest_record = record

    if not new_records:
        return False

    try:
        # 2. 批量插入净值历史
        db.bulk_save_objects(new_records)

        # 3. 更新 Fund 主表的最快信息
        if latest_record:
            # 先查询当前基金的最新净值日期
            current_fund = db.query(Fund).filter(Fund.fund_code == fund_code).first()
            if not current_fund or (current_fund.latest_nav_date is None) or (latest_record.date > current_fund.latest_nav_date):
                # 直接通过 update 语句更新，减少一次 select 基金对象的开销
                db.query(Fund).filter(Fund.fund_code == fund_code).update({
                    "latest_nav": latest_record.nav,
                    "latest_nav_date": latest_record.date,
                    "daily_growth": latest_record.daily_growth_rate,
                    "nav_updated_at": datetime.now()
                }) 
        
        db.commit()
        
        # 更新ES中的基金信息
        if latest_record and es:
            try:
                # 获取更新后的基金信息
                updated_fund = db.query(Fund).filter(Fund.fund_code == fund_code).first()
                if updated_fund:
                    # 准备ES文档
                    es_doc = {
                        "fundCode": updated_fund.fund_code,
                        "name": updated_fund.name,
                        "fullName": updated_fund.full_name,
                        "fundType": updated_fund.fund_type,
                        "company": updated_fund.company,
                        "manager": updated_fund.manager,
                        "establishDate": updated_fund.establish_date.isoformat() if updated_fund.establish_date else None,
                        "latestNav": updated_fund.latest_nav,
                        "latestNavDate": updated_fund.latest_nav_date.isoformat() if updated_fund.latest_nav_date else None,
                        "dailyGrowth": updated_fund.daily_growth,
                        "navUpdatedAt": datetime.now().isoformat(),
                        "rating": updated_fund.rating,
                        "riskLevel": updated_fund.risk_level
                    }
                    
                    # 更新ES索引
                    es.index(
                        index="funds",
                        id=fund_code,
                        body=es_doc,
                        refresh=True
                    )
                    logger.debug(f"更新基金 {fund_code} 到ES成功")
            except Exception as e:
                logger.error(f"更新基金 {fund_code} 到ES失败: {str(e)}")
        
        return True
    except Exception as e:
        db.rollback()
        logger.error(f"存储基金 {fund_code} 数据失败: {str(e)}")
        return False

def create_nav_record(fund_code: str, nav_date_str: str, values: List[float]) -> Optional[FundNavHistory]:
    """创建记录对象"""
    try:
        return FundNavHistory(
            fund_code=fund_code,
            date=datetime.strptime(nav_date_str, '%Y-%m-%d').date(),
            nav=values[0],
            daily_growth_rate=values[1],
            accumulated_nav=values[2],
            accumulated_return_rate=values[3]
        )
    except: return None

# =================================================================
# 核心任务函数
# =================================================================

async def process_fund_task(fund_code: str, task_type: str, **kwargs) -> Dict[str, Any]:
    """
    通用并发处理封装
    task_type: 'init' 或 'update'
    """
    db = next(get_db())
    result = {'fund_code': fund_code, 'status': 'error'}
    
    try:
        nav_data = {}
        target_date = kwargs.get('target_date')

        if task_type == 'init':
            nav_data = await fund_fetcher.get_fund_all_history(fund_code)
            logger.debug(f"正在初始化基金 {fund_code} 的全量数据")
            pass 
        
        elif task_type == 'update':
            # 分页获取，取第一页最近的数据
            page = kwargs.get('page', 1)
            page_size = kwargs.get('page_size', 20)
            target_date_str = target_date.strftime('%Y-%m-%d')
            
            nav_data = {}
            while True:
                nav_data = await fund_fetcher.get_fund_latest_nav(fund_code, page=page, page_size=page_size)
                if not nav_data:
                    continue
                if target_date_str in nav_data:
                    break
                page += 1
                
            logger.debug(f"正在更新基金 {fund_code} 的分页数据 (P{page})")
            pass

        # 处理并保存
        success = await save_nav_to_db(db, fund_code, nav_data, target_date)
        result['status'] = 'processed' if success else 'skipped'

    except Exception as e:
        logger.error(f"处理基金 {fund_code} 异常: {str(e)}")
        result['status'] = 'error'
    finally:
        db.close()
    return result

# =================================================================
# 外部调用接口
# =================================================================

async def initialize_fund_nav_data_concurrent(batch_size: int = 20):
    """并发初始化：抓取全量历史"""
    logger.info("开始执行基金净值【全量初始化】任务")
    
    db = next(get_db())
    valid_funds = get_valid_funds_map(db)
    
    # 筛选记录数过少的基金（说明需要初始化）
    check_sql = db.query(FundNavHistory.fund_code).group_by(FundNavHistory.fund_code).having(func.count(FundNavHistory.id) > 10).all()
    existing_codes = {r.fund_code for r in check_sql}
    db.close()

    todo_codes = [code for code in valid_funds.keys() if code not in existing_codes]
    
    for i in range(0, len(todo_codes), batch_size):
        batch = todo_codes[i : i + batch_size]
        tasks = [process_fund_task(code, 'init') for code in batch]
        await asyncio.gather(*tasks)
        await asyncio.sleep(0.5) # 防频率限制

async def update_daily_fund_nav_concurrent(batch_size: int = 50, target_date: Optional[date] = None) -> Set[str]:
    """并发更新：抓取当日/昨日分页数据"""
    current_hour = datetime.now().hour
    if not (19 <= current_hour <= 23) and target_date is None:
        return set()

    db = next(get_db())
    valid_funds = get_valid_funds_map(db)

    today = target_date if target_date else date.today()
    yesterday = today - timedelta(days=1)
    
    # 获取每个基金目前的最后日期
    latest_dates = {r.fund_code: r.m_date for r in db.query(FundNavHistory.fund_code, func.max(FundNavHistory.date).label('m_date')).group_by(FundNavHistory.fund_code).all()}
    db.close()

    updated_funds = set()
    todo_list = []

    for code, fund in valid_funds.items():
        is_qdii = fund.fund_type and ('QDII' in fund.fund_type or '海外股票' in fund.fund_type)
        fund_target_date = yesterday if is_qdii else today
        
        if latest_dates.get(code) != fund_target_date:
            todo_list.append((code, fund_target_date))

    logger.info(f"共有 {len(todo_list)} 只基金需要更新当日净值")

    for i in range(0, len(todo_list), batch_size):
        batch = todo_list[i : i + batch_size]
        # 传参：fund_code, page, page_size
        tasks = [
            process_fund_task(item[0], 'update', target_date=item[1]) 
            for item in batch
        ]
        results = await asyncio.gather(*tasks)
        
        for r in results:
            if r.get('status') == 'processed':
                updated_funds.add(r['fund_code'])
        
        await asyncio.sleep(0.2)

    return updated_funds