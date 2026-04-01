#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
归因分析模块 (高性能批量优化版)
实现 LassoCV 回归算法，用于修正行业 Beta
"""

import logging
from datetime import datetime, timedelta
from typing import Dict, List, Tuple
import pandas as pd
from sklearn.linear_model import LassoCV
from sklearn.preprocessing import StandardScaler
from sqlalchemy import func

from models.fund_index_mapping import FundIndexMapping
from models.fund import Fund
from utils.helpers import standardize_symbol
from utils.db_utils import batch_query_stock_quotes, batch_query_index_returns, batch_query_fund_returns, batch_query_holds

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class AttributionAnalyzer:
    def __init__(self, db_session):
        self.db = db_session
        
        # 申万二级行业指数
        self.all_industry_codes = [
            'idx.sw801016','idx.sw801015','idx.sw801014','idx.sw801012',
            'idx.sw801017','idx.sw801018','idx.sw801033','idx.sw801034',
            'idx.sw801032','idx.sw801036','idx.sw801037','idx.sw801038',
            'idx.sw801039','idx.sw801043','idx.sw801044','idx.sw801045',
            'idx.sw801051','idx.sw801055','idx.sw801053','idx.sw801054',
            'idx.sw801056','idx.sw801081','idx.sw801083','idx.sw801084',
            'idx.sw801082','idx.sw801085','idx.sw801086','idx.sw801093',
            'idx.sw801092','idx.sw801881','idx.sw801095','idx.sw801096',
            'idx.sw801111','idx.sw801112','idx.sw801113','idx.sw801114',
            'idx.sw801115','idx.sw801116','idx.sw801124','idx.sw801125',
            'idx.sw801126','idx.sw801127','idx.sw801128','idx.sw801129',
            'idx.sw801131','idx.sw801132','idx.sw801133','idx.sw801143',
            'idx.sw801141','idx.sw801142','idx.sw801145','idx.sw801151',
            'idx.sw801155','idx.sw801152','idx.sw801154','idx.sw801153',
            'idx.sw801156','idx.sw801161','idx.sw801163','idx.sw801178',
            'idx.sw801179','idx.sw801991','idx.sw801992','idx.sw801181',
            'idx.sw801183','idx.sw801202','idx.sw801203','idx.sw801204',
            'idx.sw801206','idx.sw801218','idx.sw801219','idx.sw801993',
            'idx.sw801994','idx.sw801782','idx.sw801783','idx.sw801784',
            'idx.sw801785','idx.sw801193','idx.sw801194','idx.sw801191',
            'idx.sw801231','idx.sw801711','idx.sw801712','idx.sw801713',
            'idx.sw801721','idx.sw801722','idx.sw801723','idx.sw801724',
            'idx.sw801726','idx.sw801731','idx.sw801733','idx.sw801735',
            'idx.sw801736','idx.sw801737','idx.sw801738','idx.sw801072',
            'idx.sw801074','idx.sw801076','idx.sw801077','idx.sw801078',
            'idx.sw801741','idx.sw801742','idx.sw801743','idx.sw801744',
            'idx.sw801745','idx.sw801101','idx.sw801103','idx.sw801104',
            'idx.sw801764','idx.sw801765','idx.sw801766','idx.sw801767',
            'idx.sw801769','idx.sw801995','idx.sw801223','idx.sw801102',
            'idx.sw801951','idx.sw801952','idx.sw801962','idx.sw801963',
            'idx.sw801971','idx.sw801972','idx.sw801981','idx.sw801982'
        ]

    def _load_all_index_returns(self, start_date: datetime.date, end_date: datetime.date) -> pd.DataFrame:
        """【优化】一次性加载所有行业指数的收益率矩阵"""
        logger.info("批量加载行业指数数据...")
        return batch_query_index_returns(self.db, self.all_industry_codes, start_date, end_date)

    def _load_all_fund_returns(self, fund_codes: List[str], start_date: datetime.date, end_date: datetime.date) -> pd.DataFrame:
        """【优化】一次性加载所有目标基金的收益率矩阵"""
        logger.info("批量加载基金净值数据...")
        return batch_query_fund_returns(self.db, fund_codes, start_date, end_date)

    def _load_all_holdings_and_stocks(self, fund_codes: List[str], start_date: datetime.date, end_date: datetime.date) -> Tuple[Dict, pd.DataFrame]:
        """【优化】一次性加载所有基金的最新前十大重仓，以及这些股票的历史收益率"""
        logger.info("批量加载基金披露持仓股数据...")
        
        # 1. 批量查询所有基金的最新前十大重仓
        holdings_query = batch_query_holds(self.db, fund_codes)

        fund_holdings = {}
        unique_stocks = set()
        for fund_code, stock_code, weight in holdings_query:
            if fund_code not in fund_holdings:
                fund_holdings[fund_code] =[]
            
            std_stock = standardize_symbol(stock_code)
            fund_holdings[fund_code].append((std_stock, float(weight)))
            unique_stocks.add(std_stock)

        logger.info(f"批量加载 {len(unique_stocks)} 只股票的行情数据...")
        # 3. 批量加载这些股票的行情
        stock_returns = pd.DataFrame()
        if unique_stocks:
            stock_list = list(unique_stocks)
            stock_returns = batch_query_stock_quotes(self.db, stock_list, start_date, end_date)

        return fund_holdings, stock_returns

    def get_target_funds(self) -> List[str]:
        """获取需要归因的基金代码列表"""
        # 定义需要过滤的基金类型列表
        filtered_fund_types = [
            '货币型-普通货币', '货币型-浮动净值', '货币型',
            '混合型-绝对收益', 'Reits', '其他', '商品',
            '债券型-中短债', '债券型-长债',
            '债券型-混合一级', '债券型-混合二级',
            '指数型-海外股票', '指数型-固收',
            'FOF-稳健型', 'FOF-进取型', 'FOF-均衡型',
            'QDII-REITs','QDII-FOF','QDII-商品','QDII-纯债','QDII-混合债'
        ]
        funds = self.db.query(Fund.fund_code).filter(
            Fund.fund_type.notin_(filtered_fund_types),
            Fund.is_valid_fund == True,
            Fund.latest_scale.notin_(['---', '0亿份'])
        ).all()
        return [f[0] for f in funds]

    def batch_update_mappings(self, days: int = 60):
        """
        【核心入口】执行批量归因分析和入库
        Args:
            days: 取过去多少个交易日的数据（建议 60-90 天以上保证 LassoCV 样本量）
        """
        logger.info("===== 开始执行批量增量校准 =====")
        
        # 1. 确定时间窗口 (自然日放宽到 days * 1.5 保证交易日数量)
        end_date = datetime.now().date()
        start_date = end_date - timedelta(days=int(days * 1.5))
        
        fund_codes = self.get_target_funds()
        if not fund_codes:
            logger.warning("没有找到目标基金")
            return 0

        # 2. 【无 N+1】一次性加载所有矩阵数据
        industry_returns = self._load_all_index_returns(start_date, end_date)
        fund_returns = self._load_all_fund_returns(fund_codes, start_date, end_date)
        fund_holdings, stock_returns = self._load_all_holdings_and_stocks(fund_codes, start_date, end_date)

        if industry_returns.empty or fund_returns.empty:
            logger.error("底层行情数据加载失败")
            return 0

        new_mappings =[]
        success_count = 0

        # 3. 遍历单只基金进行本地内存计算
        for fund_code in fund_codes:
            if fund_code not in fund_returns.columns:
                continue
                
            y_fund = fund_returns[fund_code].dropna()
            
            # 计算重仓股基准收益
            top10 = fund_holdings.get(fund_code,[])
            y_top10 = pd.Series(0.0, index=y_fund.index)
            
            if top10 and not stock_returns.empty:
                for stock_code, weight in top10:
                    if stock_code in stock_returns.columns:
                        y_top10 = y_top10.add(stock_returns[stock_code] * (weight / 100.0), fill_value=0)

            # 计算残差 (基金实际收益 - 重仓股解释的收益)
            y_residual = y_fund - y_top10

            # 索引对齐
            aligned_index = y_residual.index.intersection(industry_returns.index)
            if len(aligned_index) < 20: # 样本过少跳过
                continue
                
            y = y_residual.loc[aligned_index]
            X = industry_returns.loc[aligned_index]

            # 4. 【LassoCV】自动交叉验证寻找最优 Alpha
            try:
                scaler = StandardScaler()
                X_scaled = scaler.fit_transform(X)
                
                # cv=5: 5折交叉验证, positive=True: 约束系数为正(禁止做空行业)
                model = LassoCV(cv=5, positive=True, random_state=42, n_jobs=-1)
                model.fit(X_scaled, y)
                
                # 提取 Beta
                betas = {}
                for i, industry in enumerate(X.columns):
                    coef = float(model.coef_[i])
                    # 只保留权重大于 0.01 (1%) 的行业，过滤噪音
                    if coef > 0.01: 
                        betas[industry] = coef
                        
                # 权重归一化
                total_weight = sum(betas.values())
                if total_weight > 0:
                    for industry in betas:
                        normalized_weight = betas[industry] / total_weight
                        new_mappings.append(
                            FundIndexMapping(
                                fund_code=fund_code,
                                index_code=industry,
                                index_name=industry, # 实际应查字典替换中文名
                                proxy_type='Index',
                                effective_weight=normalized_weight,
                                data_source='attribution',
                                last_update=datetime.now()
                            )
                        )
                    success_count += 1
                    
            except Exception as e:
                logger.debug(f"基金 {fund_code} LassoCV 拟合失败: {e}")
                continue

        # 5. 【批量写库】性能提升 10倍+ 的关键
        if new_mappings:
            try:
                # 获取本次更新成功的基金代码
                updated_funds = list(set([m.fund_code for m in new_mappings]))
                
                # 批量删除旧映射 (只删除本次计算成功的，计算失败的保留旧数据兜底)
                self.db.query(FundIndexMapping).filter(
                    FundIndexMapping.fund_code.in_(updated_funds),
                    FundIndexMapping.proxy_type == 'Index',
                    FundIndexMapping.data_source == 'attribution'
                ).delete(synchronize_session=False)
                
                # 批量插入新映射
                self.db.bulk_save_objects(new_mappings)
                self.db.commit()
                logger.info(f"===== 批量归因完成，成功写入 {success_count} 只基金的映射数据 =====")
            except Exception as e:
                self.db.rollback()
                logger.error(f"批量写入数据库失败: {e}")
                
        return success_count

    def daily_calibration(self):
        """每日盘后调用接口"""
        # 考虑到 120 个特征(行业)，LassoCV 建议至少取 60 天的数据防过拟合
        return self.batch_update_mappings(days=60)

    def cold_start(self):
        """冷启动接口"""
        return self.batch_update_mappings(days=60)