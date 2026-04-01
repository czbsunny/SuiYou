# services/audit_service.py
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
from typing import List
from models.fund_portfolio_hold import FundPortfolioHold
from models.fund_valuation_audit import FundValuationAudit
from models.stock_daily_quote import StockDailyQuote
from models.fund_nav_history import FundNavHistory
import logging
from utils.helpers import standardize_symbol

logger = logging.getLogger(__name__)

class AuditService:
    def __init__(self, db_session):
        self.db = db_session

    def generate_historical_audit(self, fund_codes: List[str], days: int = 60):
        logger.info(f"开始批量审计任务...")

        # 1. 预加载配置 (找出每只基金的季报截止日 Q_date)
        # config_map: {fund_code: {'q_date': date, 'holds': {s: w}, 'total_ratio': r}}
        config_map = self._load_config_batch(fund_codes)
        
        if not config_map:
            logger.warning("未找到有效的基金持仓配置")
            return
        logger.info(f"加载到 {len(config_map)} 只基金的持仓配置")

        # 2. 【核心修正】确定“全局最早起始日期”
        # 逻辑：我们需要的数据必须覆盖到所有基金的最早一个季报截止日
        all_q_dates = [cfg['q_date'] for cfg in config_map.values()]
        earliest_q_date = min(all_q_dates)
        
        # 审计需要的起始日 (例如今天向前推60天)
        audit_start_limit = datetime.now().date() - timedelta(days=days)
        
        # 实际加载数据的起点 = 季报截止日 和 审计起点 之间的最小值
        data_load_start_date = min(earliest_q_date, audit_start_limit)

        # 3. 批量加载行情和净值 (从 data_load_start_date 开始)
        nav_map = self._load_nav_batch(fund_codes, data_load_start_date)
        logger.info(f"加载到 {len(nav_map)} 只基金的净值数据")
        
        all_stocks = set()
        for cfg in config_map.values():
            all_stocks.update(cfg['holds'].keys())
        # 价格矩阵也从 data_load_start_date 开始加载
        price_matrix = self._load_price_matrix(list(all_stocks), data_load_start_date)
        logger.info(f"加载到 {len(price_matrix)} 只股票的价格数据")
        if price_matrix.empty:
            logger.warning("未加载到任何股票价格数据")
            return

        # 4. 循环计算每只基金的审计记录
        logger.info(f"开始计算 {len(fund_codes)} 只基金的审计记录")
        all_audit_records = []
        for idx, code in enumerate(fund_codes):
            if code not in nav_map or code not in config_map: 
                continue
            
            fund_nav = nav_map[code]
            cfg = config_map[code]
            q_date = cfg['q_date']
            
            fund_records = self._calculate_single_fund_audit(
                code, fund_nav, cfg['holds'], price_matrix, q_date, audit_start_limit
            )
            all_audit_records.extend(fund_records)

            if (idx + 1) % 100 == 0:
                self._batch_upsert_audit(all_audit_records)
                self.db.commit()
                all_audit_records = []
                logger.info(f"已处理 {idx + 1} 只基金")
        
        if all_audit_records:
            self._batch_upsert_audit(all_audit_records)
            self.db.commit()
            logger.info(f"成功插入 {len(all_audit_records)} 条审计记录")
        else:
            logger.warning("未生成任何审计记录")

    def _load_config_batch(self, fund_codes: List[str]):
        """
        批量加载基金持仓配置
        """
        """
        获取基金的基准配置：
        1. 最近季报日期
        2. 十大重仓(折算为NAV占比)
        3. 总股票占比
        """
        # 1. 计算当前日期对应的上一个季度
        current_date = datetime.now()
        # 确定当前季度
        current_quarter = (current_date.month - 1) // 3 + 1
        last_quarter_year = current_date.year
        last_quarter = current_quarter - 1
        # 确定上一个季度的年和季度
        if current_quarter == 1:
            last_quarter_year = current_date.year - 1
            last_quarter = 4
        
        # 2. 构建季度字符串格式，如 "2025年4季度"
        target_quarter_prefix = f"{last_quarter_year}年{last_quarter}季度"
        
        # 3. 查询该基金上一个季度的持仓明细
        holds = self.db.query(FundPortfolioHold).filter(
            FundPortfolioHold.quarter.like(f"{target_quarter_prefix}%"),
        ).all()
        
        # 4. 计算季度结束日期
        # 1季度: 3月31日, 2季度: 6月30日, 3季度: 9月30日, 4季度: 12月31日
        quarter_end_month = last_quarter * 3
        quarter_end_day = 30
        if quarter_end_month == 3:
            quarter_end_day = 31
        elif quarter_end_month == 12:
            quarter_end_day = 31
        
        # 创建季度结束日期对象
        q_date = datetime(last_quarter_year, quarter_end_month, quarter_end_day).date()
        
        # 5. 按基金代码分组处理
        config_map = {}
        # 先按基金代码分组
        fund_holds_map = {}
        for hold in holds:
            if hold.fund_code not in fund_codes: 
                continue
            if hold.fund_code not in fund_holds_map:
                fund_holds_map[hold.fund_code] = []
            fund_holds_map[hold.fund_code].append(hold)
        
        # 对每个基金计算配置
        for fund_code, fund_holds in fund_holds_map.items():
            # 计算总股票占比（这里简单计算为所有持仓权重之和）
            total_ratio = sum(hold.weight / 100.0 for hold in fund_holds) if fund_holds else 0
            # 构建持仓字典
            holds_dict = {standardize_symbol(hold.stock_code): hold.weight / 100.0 for hold in fund_holds}
            # 添加到config_map
            config_map[fund_code] = {
                'q_date': q_date,
                'holds': holds_dict,
                'total_ratio': total_ratio
            }
        
        return config_map

    def _load_nav_batch(self, fund_codes: List[str], q_date):
        """
        批量加载基金净值数据
        """
        records = self.db.query(
            FundNavHistory.fund_code,
            FundNavHistory.date,
            FundNavHistory.accumulated_nav, # 使用累计净值
            FundNavHistory.daily_growth_rate
        ).filter(
            FundNavHistory.fund_code.in_(fund_codes),
            FundNavHistory.date >= q_date
        ).order_by(FundNavHistory.date.asc()).all()

        if not records:
            return {}

        # 2. 转换为 DataFrame
        df_all = pd.DataFrame([{
            'fund_code': r.fund_code,
            'date': r.date,
            'nav': float(r.accumulated_nav) if r.accumulated_nav else np.nan,
            'change_pct': float(r.daily_growth_rate) / 100 if r.daily_growth_rate else np.nan
        } for r in records])

        nav_map = {}
        
        # 3. 按基金分组修复
        for fund_code, group in df_all.groupby('fund_code'):
            # 排序并设置索引
            temp_df = group.set_index('date').sort_index()
            
            # --- 修复逻辑 A: 修复 change_pct ---
            calc_change = temp_df['nav'].pct_change().fillna(0)
            temp_df['change_pct'] = temp_df['change_pct'].fillna(calc_change)

            # --- 修复逻辑 B: 修复累计净值 (关键) ---
            # 情况1: 中间偶发的缺失 -> 使用 ffill (假设停牌或数据未更新)
            temp_df['nav'] = temp_df['nav'].ffill()
            
            if temp_df['nav'].isnull().any():
                # 找到第一个非空的净值点作为基准
                first_nav_idx = temp_df['nav'].first_valid_index()
                if first_nav_idx:
                    first_nav = temp_df.loc[first_nav_idx, 'nav']
                    # 计算从基准点开始的累计收益倍数
                    # cumprod() 是连乘
                    cum_rtn = (1 + temp_df.loc[first_nav_idx:, 'change_pct']).cumprod()
                    reconstructed_nav = first_nav * cum_rtn
                    # 填充回去
                    temp_df.update(reconstructed_nav.to_frame('nav'))

            # --- 修复逻辑 C: 最后兜底 ---
            temp_df['nav'] = temp_df['nav'].ffill().bfill()
            temp_df['change_pct'] = temp_df['change_pct'].fillna(0)
            
            nav_map[fund_code] = temp_df

        return nav_map

    def _load_price_matrix(self, stock_codes: list, start_date):
        """
        批量加载股票后复权价格矩阵
        """
        if not stock_codes:
            return pd.DataFrame()

        # 1. 从数据库查询长表数据
        # 字段：stock_code, date, adj_close
        quotes = self.db.query(
            StockDailyQuote.stock_code,
            StockDailyQuote.date,
            StockDailyQuote.adj_close
        ).filter(
            StockDailyQuote.stock_code.in_(stock_codes),
            StockDailyQuote.date >= start_date
        ).all()

        if not quotes:
            logger.warning("未查询到股票历史价格数据")
            return pd.DataFrame()
        
        logger.info(f"加载到 {len(quotes)} 条股票历史价格数据")
        
        # 2. 转换为 DataFrame 长表
        df_long = pd.DataFrame([{
            'code': q.stock_code,
            'date': q.date,
            'price': float(q.adj_close)
        } for q in quotes])

        logger.info(f"转换为 {len(df_long)} 条股票历史价格数据长表")

        # 3. 执行 Pivot (长表转宽表矩阵)
        matrix = df_long.pivot(index='date', columns='code', values='price')

        # 4. 【关键步骤】处理数据完整性
        # 1) 按日期排序
        matrix = matrix.sort_index()
        # 2) 前向填充缺失值（确保每个股票都有完整的时间序列）
        matrix = matrix.ffill()
        # 3) 填充剩余缺失值为0（如果有股票在 q_date 之前没有数据）
        matrix = matrix.fillna(0)

        logger.info(f"转换为 {len(matrix)} 只股票的价格数据矩阵")
        return matrix

    def _calculate_single_fund_audit(self, code, fund_nav, initial_holds, price_matrix, q_date, audit_start_limit):
        """
        修正版计算逻辑
        :param audit_start_limit: 只有大于这个日期的记录才会被保存到数据库
        """
        records = []
        # T0 点的基准数据 (必须在 pre-loaded 数据中)
        try:
            nav_0 = fund_nav.loc[q_date]['nav']
            p_0 = price_matrix.loc[q_date]
        except KeyError:
            return [] 

        # 迭代日期：从 q_date 之后的第一天开始算，一直到今天
        calc_dates = fund_nav[fund_nav.index > q_date].index
        
        for t_date in calc_dates:
            try:
                t_prev = fund_nav.index[fund_nav.index < t_date][-1]
                
                # 漂移计算 (必须用从 q_date 开始的累计涨幅)
                fund_cum_factor = fund_nav.loc[t_prev]['nav'] / nav_0
                top10_contribution = 0
                for s_code, w0 in initial_holds.items():
                    if s_code not in price_matrix.columns: continue
                    # 股票累计表现
                    stock_cum_factor = price_matrix.loc[t_prev][s_code] / p_0[s_code]
                    # 漂移权重
                    w_drifted = w0 * (stock_cum_factor / fund_cum_factor)
                    # 当日贡献
                    stock_daily_rtn = (price_matrix.loc[t_date][s_code] - price_matrix.loc[t_prev][s_code]) / price_matrix.loc[t_prev][s_code]
                    top10_contribution += (w_drifted * stock_daily_rtn)
                
                # --- 核心：只保存符合 days 要求的数据 ---
                if t_date >= audit_start_limit:
                    actual_change = fund_nav.loc[t_date]['change_pct']
                    records.append({
                        "fund_code": code,
                        "date": t_date,
                        "actual_change_pct": actual_change,
                        "top10_contribution": top10_contribution,
                        "residual_change": actual_change - top10_contribution
                    })
            except Exception:
                continue
        return records

    def _batch_upsert_audit(self, records_list):
        logger.info(f"开始批量 upsert {len(records_list)} 条基金损失审计记录")
        if not records_list:
            return

        fund_codes = list(set([r['fund_code'] for r in records_list]))
        all_dates = [r['date'] for r in records_list]
        min_date = min(all_dates)
        max_date = max(all_dates)

        try:
            # 2. 批量删除旧的审计记录 (防止主键冲突)
            self.db.query(FundValuationAudit).filter(
                FundValuationAudit.fund_code.in_(fund_codes),
                FundValuationAudit.date >= min_date,
                FundValuationAudit.date <= max_date
            ).delete(synchronize_session=False)

            logger.info(f"成功删除 {len(records_list)} 条旧审计记录")
            # 3. 使用 SQLAlchemy 的批量映射插入 (性能极好，仅次于原生SQL)
            self.db.bulk_insert_mappings(FundValuationAudit, records_list)
            logger.info(f"成功插入 {len(records_list)} 条基金损失审计记录")
        except Exception as e:
            logger.error(f"批量审计入库失败: {e}")
            raise