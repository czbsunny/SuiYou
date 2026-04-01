#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
归因分析器测试脚本
"""

import sys
import os
from datetime import datetime

# 添加项目根目录到 Python 路径
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..')))

from data_server.core.attribution_analyzer import AttributionAnalyzer
from data_server.models.db import get_db_session

def test_attribution_analyzer():
    """
    测试 AttributionAnalyzer 类的核心功能
    """
    print("===== 开始测试 AttributionAnalyzer =====")
    
    # 获取数据库会话
    db_session = get_db_session()
    
    # 初始化归因分析器
    analyzer = AttributionAnalyzer(db_session)
    
    try:
        # 测试 1: 获取目标基金
        print("\n1. 测试 get_target_funds 方法")
        funds = analyzer.get_target_funds()
        print(f"获取到 {len(funds)} 只需要执行归因分析的基金")
        
        # 测试 2: 执行回归分析（选择第一只基金）
        if funds:
            fund = funds[0]
            fund_code = fund.fund_code
            print(f"\n2. 测试 run_regression 方法，基金代码: {fund_code}")
            betas = analyzer.run_regression(fund_code)
            print(f"回归结果: {betas}")
            print(f"识别到 {len(betas)} 个行业")
        
        # 测试 3: 更新映射（只测试前 3 只基金）
        print("\n3. 测试 update_mappings 方法（只测试前 3 只基金）")
        # 为了测试速度，我们修改一下 get_target_funds 方法的返回值，只返回前 3 只基金
        original_get_target_funds = analyzer.get_target_funds
        analyzer.get_target_funds = lambda: original_get_target_funds()[:3] if len(original_get_target_funds()) > 3 else original_get_target_funds()
        success_count = analyzer.update_mappings()
        print(f"更新成功 {success_count} 只基金的映射")
        
        # 测试 4: 测试冷启动
        print("\n4. 测试 cold_start 方法")
        success_count = analyzer.cold_start()
        print(f"冷启动成功处理 {success_count} 只基金")
        
        # 测试 5: 测试日常校准
        print("\n5. 测试 daily_calibration 方法")
        success_count = analyzer.daily_calibration()
        print(f"日常校准成功处理 {success_count} 只基金")
        
        print("\n===== AttributionAnalyzer 测试完成 =====")
        return True
        
    except Exception as e:
        print(f"测试失败: {str(e)}")
        return False
    finally:
        # 关闭数据库会话
        db_session.close()

if __name__ == "__main__":
    test_attribution_analyzer()
