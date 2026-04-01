#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
基金数据获取模块
提供基金数据获取和定时更新功能
"""

from .fund_fetcher import FundFetcher
from .realtime_fetcher import RealtimeFetcher

__all__ = ["FundFetcher", "RealtimeFetcher"]
