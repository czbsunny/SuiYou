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
from data_server.database.init_db import get_db

def test_attribution_analyzer():
    """
    测试 AttributionAnalyzer 类的核心功能
    """
    print("===== 开始测试 AttributionAnalyzer =====")
    
    # 获取数据库会话
    db_session = next(get_db())
    
    # 初始化归因分析器
    analyzer = AttributionAnalyzer(db_session)
    
    try:
        print("\n测试 cold_start 方法")
        success_count = analyzer.cold_start()
        print(f"冷启动成功处理 {success_count} 只基金")
        
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
