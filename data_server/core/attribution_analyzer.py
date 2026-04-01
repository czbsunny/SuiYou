#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
归因分析模块（优化版）
- 使用申万二级行业
- 自动因子筛选（降维）
- Lasso 自动调参
- 缓存优化（避免重复查库）
"""

import logging
from datetime import datetime, timedelta
from typing import Dict, Tuple
import pandas as pd
import numpy as np
from sklearn.linear_model import Lasso
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import cross_val_score

from models.index_daily_quote import IndexDailyQuote

# 日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class AttributionAnalyzer:

    def __init__(self, db_session):
        self.db = db_session
        self.models = {}
        self.scalers = {}
        self.cache = {}

        # ✅ 申万二级行业（核心）
        self.industry_codes = [
            'idx.sw801016','idx.sw801015','idx.sw801014','idx.sw801012','idx.sw801017',
            'idx.sw801018','idx.sw801033','idx.sw801034','idx.sw801032','idx.sw801036',
            'idx.sw801037','idx.sw801038','idx.sw801039','idx.sw801043','idx.sw801044',
            'idx.sw801045','idx.sw801051','idx.sw801055','idx.sw801053','idx.sw801054',
            'idx.sw801056','idx.sw801081','idx.sw801083','idx.sw801084','idx.sw801082',
            'idx.sw801085','idx.sw801086','idx.sw801093','idx.sw801092','idx.sw801881',
            'idx.sw801095','idx.sw801096','idx.sw801111','idx.sw801112','idx.sw801113',
            'idx.sw801114','idx.sw801115','idx.sw801116','idx.sw801124','idx.sw801125',
            'idx.sw801126','idx.sw801127','idx.sw801128','idx.sw801129','idx.sw801131',
            'idx.sw801132','idx.sw801133','idx.sw801143','idx.sw801141','idx.sw801142',
            'idx.sw801145','idx.sw801151','idx.sw801155','idx.sw801152','idx.sw801154',
            'idx.sw801153','idx.sw801156','idx.sw801161','idx.sw801163','idx.sw801178',
            'idx.sw801179','idx.sw801991','idx.sw801992','idx.sw801181','idx.sw801183',
            'idx.sw801202','idx.sw801203','idx.sw801204','idx.sw801206','idx.sw801218',
            'idx.sw801219','idx.sw801993','idx.sw801994','idx.sw801782','idx.sw801783',
            'idx.sw801784','idx.sw801785','idx.sw801193','idx.sw801194','idx.sw801191',
            'idx.sw801231','idx.sw801711','idx.sw801712','idx.sw801713','idx.sw801721',
            'idx.sw801722','idx.sw801723','idx.sw801724','idx.sw801726','idx.sw801731',
            'idx.sw801733','idx.sw801735','idx.sw801736','idx.sw801737','idx.sw801738',
            'idx.sw801072','idx.sw801074','idx.sw801076','idx.sw801077','idx.sw801078',
            'idx.sw801741','idx.sw801742','idx.sw801743','idx.sw801744','idx.sw801745',
            'idx.sw801101','idx.sw801103','idx.sw801104','idx.sw801764','idx.sw801765',
            'idx.sw801766','idx.sw801767','idx.sw801769','idx.sw801995','idx.sw801223',
            'idx.sw801102','idx.sw801951','idx.sw801952','idx.sw801962','idx.sw801963',
            'idx.sw801971','idx.sw801972','idx.sw801981','idx.sw801982'
        ]

    # =========================
    # 行业收益（带缓存 + 降维）
    # =========================
    def get_industry_returns(self, days=40, top_n=20):

        cache_key = f"industry_{days}"

        if cache_key in self.cache:
            return self.cache[cache_key]

        try:
            end_date = datetime.now().date()
            start_date = end_date - timedelta(days=days * 2)

            data = self.db.query(IndexDailyQuote).filter(
                IndexDailyQuote.index_code.in_(self.industry_codes),
                IndexDailyQuote.date >= start_date,
                IndexDailyQuote.date <= end_date
            ).all()

            if not data:
                return pd.DataFrame()

            df = pd.DataFrame([
                {'date': x.date, 'code': x.index_code, 'close': x.close}
                for x in data
            ])

            pivot = df.pivot(index='date', columns='code', values='close')
            returns = pivot.pct_change().dropna()

            if len(returns) > days:
                returns = returns.tail(days)

            # ✅ 关键：筛选波动最大的行业（降维）
            vol = returns.std().sort_values(ascending=False)
            selected = vol.head(top_n).index
            returns = returns[selected]

            logger.info(f"行业降维完成: {len(selected)} 个因子")

            self.cache[cache_key] = returns
            return returns

        except Exception as e:
            logger.error(f"行业数据获取失败: {str(e)}")
            return pd.DataFrame()

    # =========================
    # Lasso模型（自动调参）
    # =========================
    def fit_model(self, y: pd.Series, X: pd.DataFrame):

        idx = y.index.intersection(X.index)

        if len(idx) < 20:
            logger.warning("样本太少")
            return None, None

        y = y.loc[idx]
        X = X.loc[idx]

        scaler = StandardScaler()
        X_scaled = scaler.fit_transform(X)

        alphas = [0.001, 0.005, 0.01, 0.05, 0.1]

        best_model = None
        best_score = -np.inf

        for alpha in alphas:
            model = Lasso(alpha=alpha, positive=True, max_iter=5000)

            scores = cross_val_score(model, X_scaled, y, cv=3)
            score = scores.mean()

            if score > best_score:
                best_score = score
                best_model = model

        best_model.fit(X_scaled, y)

        logger.info(f"最佳alpha={best_model.alpha}, score={best_score:.4f}")

        return best_model, scaler

    # =========================
    # 核心回归
    # =========================
    def calculate_betas(self, fund_code: str, fund_returns: pd.Series):

        X = self.get_industry_returns()

        if X.empty:
            return {}

        model, scaler = self.fit_model(fund_returns, X)

        if model is None:
            return {}

        betas = {}

        for i, col in enumerate(X.columns):
            beta = float(model.coef_[i])

            # ✅ 去噪声
            if beta > 0.01:
                betas[col] = beta

        # ✅ 归一化
        total = sum(betas.values())
        if total > 0:
            betas = {k: v / total for k, v in betas.items()}

        self.models[fund_code] = model
        self.scalers[fund_code] = scaler

        logger.info(f"{fund_code} 行业暴露数量: {len(betas)}")

        return betas

    # =========================
    # 预测收益（可选）
    # =========================
    def predict(self, fund_code: str, X: pd.DataFrame):

        if fund_code not in self.models:
            return pd.Series()

        model = self.models[fund_code]
        scaler = self.scalers[fund_code]

        X_scaled = scaler.transform(X)

        preds = model.predict(X_scaled)

        return pd.Series(preds, index=X.index)

    # =========================
    # 清理缓存
    # =========================
    def clear_cache(self):
        self.cache.clear()