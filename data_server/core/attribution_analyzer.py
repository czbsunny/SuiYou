#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
归因分析模块
实现 Lasso 回归算法，用于修正行业 Beta
"""

import logging
from datetime import datetime, timedelta
from typing import Dict, Tuple
import pandas as pd
from sklearn.linear_model import Lasso
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import cross_val_score
from models.fund_index_mapping import FundIndexMapping
from models.fund_portfolio_hold import FundPortfolioHold
from models.stock_daily_quote import StockDailyQuote
from models.index_daily_quote import IndexDailyQuote
from models.fund import Fund
from utils.helpers import standardize_symbol

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class AttributionAnalyzer:
    """
    归因分析器类，负责执行 Lasso 回归分析，修正行业 Beta
    """
    
    def __init__(self, db_session, alpha: float = 0.01):
        """
        初始化归因分析器
        
        Args:
            db_session: 数据库会话对象
            alpha: Lasso 回归的正则化参数
        """
        self.db = db_session
        self.alpha = alpha
        self.models = {}
        self.scalers = {}
        # 申万一级 31 个行业指数代码
        self.industry_codes = [
            'idx.sw801010',  # 农林牧渔
            'idx.sw801020',  # 采掘
            'idx.sw801030',  # 化工
            'idx.sw801040',  # 钢铁
            'idx.sw801050',  # 有色金属
            'idx.sw801080',  # 电子
            'idx.sw801110',  # 家用电器
            'idx.sw801120',  # 食品饮料
            'idx.sw801130',  # 纺织服饰
            'idx.sw801140',  # 轻工制造
            'idx.sw801150',  # 医药生物
            'idx.sw801160',  # 公用事业
            'idx.sw801170',  # 交通运输
            'idx.sw801180',  # 房地产
            'idx.sw801200',  # 商业贸易
            'idx.sw801210',  # 休闲服务
            'idx.sw801230',  # 综合
            'idx.sw801710',  # 建筑材料
            'idx.sw801720',  # 建筑装饰
            'idx.sw801730',  # 电气设备
            'idx.sw801740',  # 国防军工
            'idx.sw801750',  # 计算机
            'idx.sw801760',  # 传媒
            'idx.sw801770',  # 通信
            'idx.sw801780',  # 银行
            'idx.sw801790',  # 非银金融
            'idx.sw801880',  # 汽车
            'idx.sw801890',  # 机械设备
            'idx.sw801920',  # 煤炭
            'idx.sw801930',  # 石油石化
            'idx.sw801980'   # 美容护理
        ]

        self.industry_second_codes = [
            'idx.sw801016', # 种植业
            'idx.sw801015', # 渔业
            'idx.sw801014', # 饲料
            'idx.sw801012', # 农产品加工
            'idx.sw801017', # 养殖业
            'idx.sw801018', # 动物保健Ⅱ
            'idx.sw801033', # 化学原料
            'idx.sw801034', # 化学制品
            'idx.sw801032', # 化学纤维
            'idx.sw801036', # 塑料
            'idx.sw801037', # 橡胶
            'idx.sw801038', # 农化制品
            'idx.sw801039', # 非金属材料Ⅱ
            'idx.sw801043', # 冶钢原料
            'idx.sw801044', # 普钢
            'idx.sw801045', # 特钢Ⅱ
            'idx.sw801051', # 金属新材料
            'idx.sw801055', # 工业金属
            'idx.sw801053', # 贵金属
            'idx.sw801054', # 小金属
            'idx.sw801056', # 能源金属
            'idx.sw801081', # 半导体
            'idx.sw801083', # 元件
            'idx.sw801084', # 光学光电子
            'idx.sw801082', # 其他电子Ⅱ
            'idx.sw801085', # 消费电子
            'idx.sw801086', # 电子化学品Ⅱ
            'idx.sw801093', # 汽车零部件
            'idx.sw801092', # 汽车服务
            'idx.sw801881', # 摩托车及其他
            'idx.sw801095', # 乘用车
            'idx.sw801096', # 商用车
            'idx.sw801111', # 白色家电
            'idx.sw801112', # 黑色家电
            'idx.sw801113', # 小家电
            'idx.sw801114', # 厨卫电器
            'idx.sw801115', # 照明设备Ⅱ
            'idx.sw801116', # 家电零部件Ⅱ
            'idx.sw801124', # 食品加工
            'idx.sw801125', # 白酒Ⅱ
            'idx.sw801126', # 非白酒
            'idx.sw801127', # 饮料乳品
            'idx.sw801128', # 休闲食品
            'idx.sw801129', # 调味发酵品Ⅱ
            'idx.sw801131', # 纺织制造
            'idx.sw801132', # 服装家纺
            'idx.sw801133', # 饰品
            'idx.sw801143', # 造纸
            'idx.sw801141', # 包装印刷
            'idx.sw801142', # 家居用品
            'idx.sw801145', # 文娱用品
            'idx.sw801151', # 化学制药
            'idx.sw801155', # 中药Ⅱ
            'idx.sw801152', # 生物制品
            'idx.sw801154', # 医药商业
            'idx.sw801153', # 医疗器械
            'idx.sw801156', # 医疗服务
            'idx.sw801161', # 电力
            'idx.sw801163', # 燃气Ⅱ
            'idx.sw801178', # 物流
            'idx.sw801179', # 铁路公路
            'idx.sw801991', # 航空机场
            'idx.sw801992', # 航运港口
            'idx.sw801181', # 房地产开发
            'idx.sw801183', # 房地产服务
            'idx.sw801202', # 贸易Ⅱ
            'idx.sw801203', # 一般零售
            'idx.sw801204', # 专业连锁Ⅱ
            'idx.sw801206', # 互联网电商
            'idx.sw801218', # 专业服务
            'idx.sw801219', # 酒店餐饮
            'idx.sw801993', # 旅游及景区
            'idx.sw801994', # 教育
            'idx.sw801782', # 国有大型银行Ⅱ
            'idx.sw801783', # 股份制银行Ⅱ
            'idx.sw801784', # 城商行Ⅱ
            'idx.sw801785', # 农商行Ⅱ
            'idx.sw801193', # 证券Ⅱ
            'idx.sw801194', # 保险Ⅱ
            'idx.sw801191', # 多元金融
            'idx.sw801231', # 综合Ⅱ
            'idx.sw801711', # 水泥
            'idx.sw801712', # 玻璃玻纤
            'idx.sw801713', # 装修建材
            'idx.sw801721', # 房屋建设Ⅱ
            'idx.sw801722', # 装修装饰Ⅱ
            'idx.sw801723', # 基础建设
            'idx.sw801724', # 专业工程
            'idx.sw801726', # 工程咨询服务Ⅱ
            'idx.sw801731', # 电机Ⅱ
            'idx.sw801733', # 其他电源设备Ⅱ
            'idx.sw801735', # 光伏设备
            'idx.sw801736', # 风电设备
            'idx.sw801737', # 电池
            'idx.sw801738', # 电网设备
            'idx.sw801072', # 通用设备
            'idx.sw801074', # 专用设备
            'idx.sw801076', # 轨交设备Ⅱ
            'idx.sw801077', # 工程机械
            'idx.sw801078', # 自动化设备
            'idx.sw801741', # 航天装备Ⅱ
            'idx.sw801742', # 航空装备Ⅱ
            'idx.sw801743', # 地面兵装Ⅱ
            'idx.sw801744', # 航海装备Ⅱ
            'idx.sw801745', # 军工电子Ⅱ
            'idx.sw801101', # 计算机设备
            'idx.sw801103', # IT服务Ⅱ
            'idx.sw801104', # 软件开发
            'idx.sw801764', # 游戏Ⅱ
            'idx.sw801765', # 广告营销
            'idx.sw801766', # 影视院线
            'idx.sw801767', # 数字媒体
            'idx.sw801769', # 出版
            'idx.sw801995', # 电视广播Ⅱ
            'idx.sw801223', # 通信服务
            'idx.sw801102', # 通信设备
            'idx.sw801951', # 煤炭开采
            'idx.sw801952', # 焦炭Ⅱ
            'idx.sw801962', # 油服工程
            'idx.sw801963', # 炼化及贸易
            'idx.sw801971', # 环境治理
            'idx.sw801972', # 环保设备Ⅱ
            'idx.sw801981', # 个护用品
            'idx.sw801982'  # 化妆品
        ]

        # 风格因子指数代码
        self.style_factors = {
            'market_cap': ['000852', '000300'],  # 中证1000, 沪深300
            'style': ['399370', '399371'],       # 国证价值, 国证成长
            'hot_tracks': []                     # 热门赛道，如光伏、半导体
        }
    
    def fit_model(self, fund_returns: pd.Series, factor_returns: pd.DataFrame) -> Tuple[Lasso, StandardScaler]:
        """
        训练 Lasso 回归模型
        
        Args:
            fund_returns: 基金收益率序列
            factor_returns: 因子收益率矩阵
            
        Returns:
            (模型, 标准化器) 元组
        """
        # 确保索引对齐
        aligned_index = fund_returns.index.intersection(factor_returns.index)
        fund_returns_aligned = fund_returns.loc[aligned_index]
        factor_returns_aligned = factor_returns.loc[aligned_index]
        
        if len(aligned_index) < 10:
            logger.warning(f"样本数量不足，只有 {len(aligned_index)} 个样本")
            # 返回默认模型
            model = Lasso(alpha=self.alpha, fit_intercept=True, positive=True)
            scaler = StandardScaler()
            return model, scaler
        
        # 标准化因子数据
        scaler = StandardScaler()
        factor_returns_scaled = scaler.fit_transform(factor_returns_aligned)
        
        # 训练模型，设置 positive=True 确保行业暴露为正
        model = Lasso(alpha=self.alpha, fit_intercept=True, positive=True)
        model.fit(factor_returns_scaled, fund_returns_aligned)
        
        # 评估模型
        scores = cross_val_score(model, factor_returns_scaled, fund_returns_aligned, cv=5)
        logger.debug(f"模型交叉验证得分: {scores.mean():.4f} ± {scores.std():.4f}")
        
        return model, scaler
    
    def run_regression(self, fund_code, days=40):
        """
        执行具体的回归算法
        
        Args:
            fund_code: 基金代码
            days: 天数
            
        Returns:
            行业权重字典，格式为 {行业代码: 权重}
        """
        try:
            # 1. 获取基金收益率
            fund_returns = self.get_fund_returns(fund_code, days)
            if fund_returns.empty:
                logger.warning(f"基金 {fund_code} 没有足够的收益率数据")
                return {}
            
            # 2. 获取前十大重仓股及其收益率
            top10_holdings = self.get_top10_holdings(fund_code)
            top10_returns = pd.Series()
            if top10_holdings:
                top10_returns = self.calculate_top10_returns(top10_holdings, days)
            
            # 3. 计算残差收益率（基金收益率 - 前十大重仓股加权收益率）
            y = self.calculate_residual_returns(fund_returns, top10_returns)
            
            # 4. 获取行业指数收益率作为自变量 X
            X = self.get_industry_returns(days)
            if X.empty:
                logger.warning("没有足够的行业指数收益率数据")
                return {}
            
            # 5. 确保 y 和 X 的索引对齐
            aligned_index = y.index.intersection(X.index)
            if len(aligned_index) < 10:
                logger.warning(f"对齐后样本数量不足，只有 {len(aligned_index)} 个样本")
                return {}
            
            y_aligned = y.loc[aligned_index]
            X_aligned = X.loc[aligned_index]
            
            # 6. 执行 Lasso 回归
            model, scaler = self.fit_model(y_aligned, X_aligned)
            
            # 7. 提取 Beta 系数
            betas = {}
            for i, industry in enumerate(X_aligned.columns):
                beta = float(model.coef_[i])
                if beta > 0:  # 只保留正系数
                    betas[industry] = beta
            
            # 8. 归一化权重
            total_weight = sum(betas.values())
            if total_weight > 0:
                for industry in betas:
                    betas[industry] /= total_weight
            
            logger.info(f"基金 {fund_code} 的行业暴露分析完成，识别到 {len(betas)} 个行业")
            logger.debug(f"基金 {fund_code} 的行业权重: {betas}")
            
            return betas
        except Exception as e:
            logger.error(f"执行回归算法失败: {str(e)}")
            return {}
    
    def update_mappings(self):
        """
        全量/增量刷新数据库中的基金指数映射
        """
        # 获取需要执行归因分析的基金
        funds = self.get_target_funds()
        total_funds = len(funds)
        success_count = 0
        
        logger.info(f"开始更新基金指数映射，共 {total_funds} 只基金")
        
        for i, fund in enumerate(funds):
            fund_code = fund.fund_code
            logger.info(f"处理基金 {i+1}/{total_funds}: {fund_code}")
            
            try:
                # 执行回归，获取行业权重
                betas = self.run_regression(fund_code)
                
                if not betas:
                    logger.warning(f"基金 {fund_code} 回归失败，跳过更新")
                    continue
                
                # 获取指数基本信息，用于获取指数名称
                index_basics = {}
                
                # 开始事务
                # 1. 删除该基金现有的映射
                self.db.query(FundIndexMapping).filter(
                    FundIndexMapping.fund_code == fund_code,
                    FundIndexMapping.proxy_type == 'Index'  # 只删除指数类型的映射
                ).delete()
                
                # 2. 插入新的映射
                for index_code, weight in betas.items():
                    index_name = index_basics.get(index_code, index_code)
                    mapping = FundIndexMapping(
                        fund_code=fund_code,
                        index_code=index_code,
                        index_name=index_name,
                        proxy_type='Index',
                        effective_weight=weight,
                        data_source='attribution',
                        last_update=datetime.now()
                    )
                    self.db.add(mapping)
                
                # 提交事务
                self.db.commit()
                success_count += 1
                logger.info(f"基金 {fund_code} 映射更新成功")
                
            except Exception as e:
                logger.error(f"更新基金 {fund_code} 映射失败: {str(e)}")
                self.db.rollback()
                continue
        
        logger.info(f"基金指数映射更新完成，成功 {success_count}/{total_funds} 只基金")
        return success_count
    
    def cold_start(self):
        """
        冷启动：系统首次部署时执行的全量归因分析
        
        目的：在系统首次进行 5 分钟实时估值之前，先跑一遍全量归因，
        确保 FundIndexMapping 被填满，系统具备实战能力。
        """
        logger.info("===== 开始执行冷启动归因分析 =====")
        
        try:
            # 执行全量更新
            success_count = self.update_mappings()
            
            logger.info(f"===== 冷启动归因分析完成 =====")
            logger.info(f"成功处理 {success_count} 只基金的归因分析")
            logger.info("系统现在具备实战能力，可以开始进行 5 分钟实时估值")
            
            return success_count
        except Exception as e:
            logger.error(f"冷启动归因分析失败: {str(e)}")
            return 0
    
    def daily_calibration(self):
        """
        日常增量校准：每日盘后凌晨执行
        
        目的：基金公司公布了昨日真实的净值后，将窗口向后推一天，
        重新跑回归，更新基金指数映射，以捕捉基金经理的调仓行为。
        """
        logger.info("===== 开始执行日常增量校准 =====")
        
        try:
            # 执行全量更新（实际上是增量校准，因为每天都只处理最新数据）
            success_count = self.update_mappings()
            
            logger.info(f"===== 日常增量校准完成 =====")
            logger.info(f"成功处理 {success_count} 只基金的归因分析")
            logger.info("基金指数映射已更新，系统已准备好明天的 5 分钟实时估值")
            
            return success_count
        except Exception as e:
            logger.error(f"日常增量校准失败: {str(e)}")
            return 0
    
    def calculate_betas(self, fund_code: str, fund_returns: pd.Series, factor_returns: pd.DataFrame) -> Dict[str, float]:
        """
        计算基金对各因子的 Beta 系数
        
        Args:
            fund_code: 基金代码
            fund_returns: 基金收益率序列
            factor_returns: 因子收益率矩阵
            
        Returns:
            Beta 系数字典，格式为 {"factor1": 0.5, "factor2": -0.2, ...}
        """
        try:
            # 训练模型
            model, scaler = self.fit_model(fund_returns, factor_returns)
            
            # 保存模型和标准化器
            self.models[fund_code] = model
            self.scalers[fund_code] = scaler
            
            # 提取 Beta 系数
            betas = {}
            for i, factor in enumerate(factor_returns.columns):
                betas[factor] = float(model.coef_[i])
            
            # 添加截距项
            betas["intercept"] = float(model.intercept_)
            
            logger.debug(f"基金 {fund_code} 的 Beta 系数: {betas}")
            
            return betas
            
        except Exception as e:
            logger.error(f"计算基金 {fund_code} 的 Beta 系数失败: {str(e)}")
            return {}
    
    def predict_returns(self, fund_code: str, factor_returns: pd.DataFrame) -> pd.Series:
        """
        使用训练好的模型预测基金收益率
        
        Args:
            fund_code: 基金代码
            factor_returns: 因子收益率矩阵
            
        Returns:
            预测的基金收益率序列
        """
        if fund_code not in self.models:
            logger.warning(f"基金 {fund_code} 没有训练好的模型")
            return pd.Series(dtype=float)
        
        try:
            model = self.models[fund_code]
            scaler = self.scalers[fund_code]
            
            # 标准化因子数据
            factor_returns_scaled = scaler.transform(factor_returns)
            
            # 预测
            predictions = model.predict(factor_returns_scaled)
            
            return pd.Series(predictions, index=factor_returns.index)
            
        except Exception as e:
            logger.error(f"预测基金 {fund_code} 的收益率失败: {str(e)}")
            return pd.Series(dtype=float)
    
    def calculate_residuals(self, fund_returns: pd.Series, predicted_returns: pd.Series) -> pd.Series:
        """
        计算残差
        
        Args:
            fund_returns: 实际基金收益率序列
            predicted_returns: 预测的基金收益率序列
            
        Returns:
            残差序列
        """
        # 确保索引对齐
        aligned_index = fund_returns.index.intersection(predicted_returns.index)
        fund_returns_aligned = fund_returns.loc[aligned_index]
        predicted_returns_aligned = predicted_returns.loc[aligned_index]
        
        # 计算残差
        residuals = fund_returns_aligned - predicted_returns_aligned
        
        return residuals
    
    def optimize_weights(self, betas: Dict[str, float], target_exposure: Dict[str, float]) -> Dict[str, float]:
        """
        优化因子权重，以达到目标暴露
        
        Args:
            betas: 当前 Beta 系数
            target_exposure: 目标暴露
            
        Returns:
            优化后的权重
        """
        # 简单实现：根据目标暴露调整权重
        optimized_weights = {}
        
        for factor, target in target_exposure.items():
            if factor in betas and betas[factor] != 0:
                optimized_weights[factor] = target / betas[factor]
            else:
                optimized_weights[factor] = 0.0
        
        # 归一化
        total_weight = sum(abs(w) for w in optimized_weights.values())
        if total_weight > 0:
            for factor in optimized_weights:
                optimized_weights[factor] /= total_weight
        
        return optimized_weights
    
    def clear_model(self, fund_code: str):
        """
        清除基金的模型
        
        Args:
            fund_code: 基金代码
        """
        if fund_code in self.models:
            del self.models[fund_code]
        if fund_code in self.scalers:
            del self.scalers[fund_code]
    
    def clear_all_models(self):
        """
        清除所有模型
        """
        self.models.clear()
        self.scalers.clear()
    
    def get_target_funds(self):
        """
        根据基金类型过滤需要执行归因分析的基金
        
        Returns:
            需要执行归因分析的基金列表
        """
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
        
        # 查询所有符合条件的基金
        try:
            funds = self.db.query(Fund).filter(
                Fund.fund_type.notin_(filtered_fund_types),
                Fund.is_valid_fund == True,
                Fund.latest_scale.notin_(['---', '0亿份'])
            ).all()
            
            logger.info(f"筛选出 {len(funds)} 只需要执行归因分析的基金")
            return funds
        except Exception as e:
            logger.error(f"筛选目标基金失败: {str(e)}")
            return []
    
    def get_fund_returns(self, fund_code, days=40):
        """
        获取基金过去指定天数的日收益率
        
        Args:
            fund_code: 基金代码
            days: 天数
            
        Returns:
            基金收益率序列
        """
        from models.fund_nav_history import FundNavHistory
        
        try:
            # 计算起始日期
            end_date = datetime.now().date()
            start_date = end_date - timedelta(days=days*2)  # 留出足够的缓冲
            
            # 查询基金净值历史
            nav_history = self.db.query(FundNavHistory).filter(
                FundNavHistory.fund_code == fund_code,
                FundNavHistory.date >= start_date,
                FundNavHistory.date <= end_date
            ).order_by(FundNavHistory.date).all()
            
            if not nav_history:
                logger.warning(f"基金 {fund_code} 没有足够的净值历史数据")
                return pd.Series()
            
            # 转换为DataFrame并计算收益率
            df = pd.DataFrame([
                {'date': item.date, 'nav': item.nav}
                for item in nav_history
            ])
            df.set_index('date', inplace=True)
            
            # 计算日收益率
            returns = df['nav'].pct_change().dropna()
            
            # 只保留最近days天的数据
            if len(returns) > days:
                returns = returns.tail(days)
            
            logger.debug(f"获取到基金 {fund_code} 的 {len(returns)} 个交易日收益率数据")
            return returns
        except Exception as e:
            logger.error(f"获取基金 {fund_code} 收益率失败: {str(e)}")
            return pd.Series()
    
    def get_industry_returns(self, days=40):
        """
        获取行业指数过去指定天数的日收益率
        
        Args:
            days: 天数
            
        Returns:
            行业指数收益率矩阵
        """
        try:
            # 计算起始日期
            end_date = datetime.now().date()
            start_date = end_date - timedelta(days=days*2)  # 留出足够的缓冲
            
            # 查询所有行业指数的价格历史
            index_data = self.db.query(IndexDailyQuote).filter(
                IndexDailyQuote.index_code.in_(self.industry_codes),
                IndexDailyQuote.date >= start_date,
                IndexDailyQuote.date <= end_date
            ).all()
            
            if len(index_data) == 0:
                logger.warning("没有足够的行业指数历史数据")
                return pd.DataFrame()
            
            # 转换为DataFrame
            df = pd.DataFrame([
                {'date': item.date, 'symbol': item.index_code, 'close': item.close}
                for item in index_data
            ])
            
            # 按指数代码透视
            pivot_df = df.pivot(index='date', columns='symbol', values='close')
            
            # 计算日收益率
            returns = pivot_df.pct_change().dropna()
            
            # 只保留最近days天的数据
            if len(returns) > days:
                returns = returns.tail(days)
            
            logger.debug(f"获取到 {len(returns.columns)} 个行业指数的 {len(returns)} 个交易日收益率数据")
            return returns
        except Exception as e:
            logger.error(f"获取行业指数收益率失败: {str(e)}")
            return pd.DataFrame()
    
    def get_top10_holdings(self, fund_code):
        """
        获取基金的前十大重仓股及其权重
        
        Args:
            fund_code: 基金代码
            
        Returns:
            前十大重仓股列表，每个元素为 (股票代码, 权重) 元组
        """
        try:
            # 查询基金最新的季度
            from sqlalchemy import func
            
            latest_quarter = self.db.query(
                func.max(FundPortfolioHold.quarter)
            ).filter(
                FundPortfolioHold.fund_code == fund_code
            ).scalar()
            
            if not latest_quarter:
                logger.warning(f"基金 {fund_code} 没有持仓数据")
                return []
            
            # 查询基金最新季度的持仓，按权重排序取前十大
            holdings = self.db.query(FundPortfolioHold).filter(
                FundPortfolioHold.fund_code == fund_code,
                FundPortfolioHold.quarter == latest_quarter  # 最新季度
            ).order_by(
                FundPortfolioHold.weight.desc()  # 按权重降序排序
            ).limit(10).all()
            
            if not holdings:
                logger.warning(f"基金 {fund_code} 在季度 {latest_quarter} 没有持仓数据")
                return []
            
            # 提取股票代码和权重
            top10 = [(h.stock_code, h.weight) for h in holdings]
            logger.debug(f"获取到基金 {fund_code} 在季度 {latest_quarter} 的前十大重仓股数据")
            return top10
        except Exception as e:
            logger.error(f"获取基金 {fund_code} 前十大重仓股失败: {str(e)}")
            return []
    
    def calculate_top10_returns(self, top10_holdings, days=40):
        """
        计算前十大重仓股的加权收益率
        
        Args:
            top10_holdings: 前十大重仓股列表，每个元素为 (股票代码, 权重) 元组
            days: 天数
            
        Returns:
            前十大重仓股加权收益率序列
        """
        if not top10_holdings:
            return pd.Series()
        
        try:
            # 计算起始日期
            end_date = datetime.now().date()
            start_date = end_date - timedelta(days=days*2)  # 留出足够的缓冲
            
            # 提取股票代码
            stock_codes = [standardize_symbol(stock) for stock, _ in top10_holdings]
            
            # 查询股票价格历史
            stock_data = self.db.query(StockDailyQuote).filter(
                StockDailyQuote.stock_code.in_(stock_codes),
                StockDailyQuote.date >= start_date,
                StockDailyQuote.date <= end_date
            ).all()
            
            if not stock_data:
                logger.warning("没有足够的股票价格历史数据")
                return pd.Series()
            
            # 转换为DataFrame
            df = pd.DataFrame([
                {'date': item.date, 'stock_code': item.stock_code, 'close': item.adj_close}
                for item in stock_data
            ])
            
            # 按股票代码透视
            pivot_df = df.pivot(index='date', columns='stock_code', values='close')
            
            # 计算日收益率
            returns = pivot_df.pct_change().dropna()
            
            # 只保留最近days天的数据
            if len(returns) > days:
                returns = returns.tail(days)
            
            # 计算加权收益率
            weights = {stock: weight for stock, weight in top10_holdings}
            weighted_returns = pd.Series(0, index=returns.index)
            
            for stock, weight in weights.items():
                if stock in returns.columns:
                    weighted_returns += returns[stock] * (weight / 100)  # 权重转换为小数
            
            logger.debug(f"计算得到前十大重仓股的 {len(weighted_returns)} 个交易日加权收益率数据")
            return weighted_returns
        except Exception as e:
            logger.error(f"计算前十大重仓股加权收益率失败: {str(e)}")
            return pd.Series()
    
    def calculate_residual_returns(self, fund_returns, top10_returns):
        """
        计算残差收益率（基金收益率 - 前十大重仓股加权收益率）
        
        Args:
            fund_returns: 基金收益率序列
            top10_returns: 前十大重仓股加权收益率序列
            
        Returns:
            残差收益率序列
        """
        # 确保索引对齐
        aligned_index = fund_returns.index.intersection(top10_returns.index)
        
        if len(aligned_index) == 0:
            logger.warning("基金收益率和前十大重仓股收益率没有重叠的日期")
            return fund_returns  # 如果没有重仓股数据，返回原始基金收益率
        
        fund_returns_aligned = fund_returns.loc[aligned_index]
        top10_returns_aligned = top10_returns.loc[aligned_index]
        
        # 计算残差
        residual_returns = fund_returns_aligned - top10_returns_aligned
        
        logger.debug(f"计算得到 {len(residual_returns)} 个交易日的残差收益率数据")
        return residual_returns

class FactorExtractor:
    """
    因子提取器类，负责从市场数据中提取因子
    """
    
    @staticmethod
    def extract_industry_factors(stock_returns: pd.DataFrame, industry_mapping: Dict[str, str]) -> pd.DataFrame:
        """
        提取行业因子
        
        Args:
            stock_returns: 股票收益率矩阵
            industry_mapping: 股票到行业的映射
            
        Returns:
            行业因子收益率矩阵
        """
        # 创建行业分组
        industry_groups = {}
        for stock, industry in industry_mapping.items():
            if industry not in industry_groups:
                industry_groups[industry] = []
            if stock in stock_returns.columns:
                industry_groups[industry].append(stock)
        
        # 计算行业收益率
        industry_returns = pd.DataFrame()
        for industry, stocks in industry_groups.items():
            if stocks:
                # 计算行业平均收益率
                industry_returns[industry] = stock_returns[stocks].mean(axis=1)
        
        return industry_returns
    
    @staticmethod
    def extract_style_factors(stock_data: pd.DataFrame) -> pd.DataFrame:
        """
        提取风格因子
        
        Args:
            stock_data: 股票数据，包含市值、估值等信息
            
        Returns:
            风格因子收益率矩阵
        """
        # 这里可以实现规模、价值、动量等风格因子的提取
        # 简单实现：返回空矩阵
        return pd.DataFrame(index=stock_data.index)