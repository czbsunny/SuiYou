#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试估算净值偏差分析脚本
功能：
1. 计算目标净值与估算净值的偏差
2. 按照偏差从小到大排序
3. 按照0.1%一个等级划分偏差单位
"""

import csv

def calculate_nav_deviation(target_nav, estimate_nav):
    """
    计算净值偏差
    :param target_nav: 目标净值
    :param estimate_nav: 估算净值
    :return: 偏差百分比
    """
    return estimate_nav - target_nav

def get_deviation_level(deviation):
    """
    获取偏差等级（0.1%为一个单位）
    :param deviation: 偏差百分比
    :return: 偏差等级
    """
    return round(abs(deviation) / 0.1, 1)

def analyze_nav_deviation(data):
    """
    分析净值偏差
    :param data: 包含target_nav、estimate_nav、fund_code和fund_type的字典列表
    :return: 分析结果
    """
    # 计算偏差
    results = []
    for item in data:
        target_nav = item['target_nav']
        estimate_nav = item['estimate_nav']
        fund_code = item.get('fund_code', '')
        fund_type = item.get('fund_type', '')
        deviation = calculate_nav_deviation(target_nav, estimate_nav)
        deviation_level = get_deviation_level(deviation)
        
        results.append({
            'fund_code': fund_code,
            'fund_type': fund_type,
            'target_nav': target_nav,
            'estimate_nav': estimate_nav,
            'deviation': deviation,
            'deviation_level': deviation_level
        })
    
    # 按照偏差绝对值从小到大排序
    results.sort(key=lambda x: abs(x['deviation']))
    
    return results

def print_analysis_results(results):
    """
    打印分析结果
    :param results: 分析结果
    """
    print(f"{'基金代码':<10} {'基金类型':<20} {'实际涨跌':<10} {'预估涨跌':<10} {'偏差':<10} {'偏差等级(0.1%单位)':<20}")
    print("-" * 100)
    
    for result in results:
        fund_code = result['fund_code']
        fund_type = result['fund_type']
        target_nav = result['target_nav']
        estimate_nav = result['estimate_nav']
        deviation = result['deviation']
        deviation_level = result['deviation_level']
        
        # 格式化输出
        deviation_str = f"{deviation:.4f}%"
        level_str = f"{deviation_level:.1f} (0.1%单位)"
        
        print(f"{fund_code:<10} {fund_type:<20} {target_nav:<10.4f} {estimate_nav:<10.4f} {deviation_str:<10} {level_str:<20}")

def write_results_to_csv(results, output_file='config\\nav_deviation_analysis.csv'):
    """
    将分析结果写入CSV文件
    :param results: 分析结果
    :param output_file: 输出文件路径
    """
    with open(output_file, 'w', newline='', encoding='utf-8-sig') as f:
        fieldnames = ['fund_code', 'fund_type', 'actual_growth', 'estimated_growth', 'deviation', 'deviation_level']
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        
        # 写入表头
        writer.writeheader()
        
        # 写入数据
        for result in results:
            writer.writerow({
                'fund_code': result['fund_code'],
                'fund_type': result['fund_type'],
                'actual_growth': result['target_nav'],
                'estimated_growth': result['estimate_nav'],
                'deviation': result['deviation'],
                'deviation_level': result['deviation_level']
            })
    print(f"分析结果已写入 {output_file}")

def generate_test_data():
    """
    从CSV文件读取测试数据
    :return: 测试数据
    """
    # 读取实际涨跌数据
    actual_data = {}
    with open('config\\fund_nav.csv', 'r', encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        for row in reader:
            fund_code = row['fund_code']
            fund_type = row.get('fund_type', '')
            try:
                daily_growth_rate = float(row['daily_growth_rate']) if row['daily_growth_rate'] else 0.0
            except ValueError:
                daily_growth_rate = 0.0
            actual_data[fund_code] = {
                'daily_growth_rate': daily_growth_rate,
                'fund_type': fund_type
            }
    
    # 读取预估涨跌数据
    estimate_data = {}
    with open('config\\fund_nav_estimate.csv', 'r', encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        for row in reader:
            fund_code = row['fund_code']
            try:
                # 预估涨跌需要除以100
                nav_estimate = float(row['nav_estimate'])
            except ValueError:
                nav_estimate = 0.0
            estimate_data[fund_code] = nav_estimate
    
    # 合并数据
    test_data = []
    for fund_code, data in actual_data.items():
        if fund_code in estimate_data:
            test_data.append({
                'target_nav': data['daily_growth_rate'],  # 实际涨跌
                'estimate_nav': estimate_data[fund_code],  # 预估涨跌（已除以100）
                'fund_code': fund_code,
                'fund_type': data['fund_type']
            })
    
    return test_data

def analyze_by_fund_type(results):
    """
    按基金类型统计分析
    :param results: 分析结果
    """
    # 按基金类型分组
    type_groups = {}
    for result in results:
        fund_type = result['fund_type']
        if fund_type not in type_groups:
            type_groups[fund_type] = []
        type_groups[fund_type].append(result)
    
    # 统计每个基金类型的偏差情况
    print("\n=== 按基金类型统计分析 ===")
    print(f"{'基金类型':<20} {'数量':<8} {'平均偏差':<12} {'最大偏差':<12} {'最小偏差':<12}")
    print("-" * 80)
    
    for fund_type, type_results in sorted(type_groups.items()):
        count = len(type_results)
        deviations = [abs(item['deviation']) for item in type_results]
        avg_deviation = sum(deviations) / count if count > 0 else 0
        max_deviation = max(deviations) if count > 0 else 0
        min_deviation = min(deviations) if count > 0 else 0
        
        print(f"{fund_type:<20} {count:<8} {avg_deviation:<12.4f} {max_deviation:<12.4f} {min_deviation:<12.4f}")
    
    return type_groups

def write_statistics_to_file(results, type_groups, avg_deviation, max_deviation, min_deviation):
    """
    将统计分析结果写入文本文件
    :param results: 分析结果
    :param type_groups: 按基金类型分组的结果
    :param avg_deviation: 平均偏差
    :param max_deviation: 最大偏差
    :param min_deviation: 最小偏差
    """
    output_file = 'config\\nav_deviation_statistics.txt'
    
    with open(output_file, 'w', encoding='utf-8') as f:
        # 整体统计分析
        f.write("=== 整体统计分析 ===\n")
        f.write(f"平均偏差: {avg_deviation:.4f}%\n")
        f.write(f"最大偏差: {max_deviation:.4f}%\n")
        f.write(f"最小偏差: {min_deviation:.4f}%\n")
        f.write(f"总基金数: {len(results)}\n\n")
        
        # 按基金类型统计分析
        f.write("=== 按基金类型统计分析 ===\n")
        f.write(f"{'基金类型':<20} {'数量':<8} {'平均偏差':<12} {'最大偏差':<12} {'最小偏差':<12}\n")
        f.write("-" * 80 + "\n")
        
        for fund_type, type_results in sorted(type_groups.items()):
            count = len(type_results)
            type_deviations = [abs(item['deviation']) for item in type_results]
            type_avg = sum(type_deviations) / count if count > 0 else 0
            type_max = max(type_deviations) if count > 0 else 0
            type_min = min(type_deviations) if count > 0 else 0
            
            f.write(f"{fund_type:<20} {count:<8} {type_avg:<12.4f} {type_max:<12.4f} {type_min:<12.4f}\n")
    
    print(f"统计分析结果已写入 {output_file}")

if __name__ == "__main__":
    print("=== 净值偏差分析测试 ===")
    print()
    
    # 生成测试数据
    test_data = generate_test_data()
    print(f"生成了 {len(test_data)} 条测试数据")
    print()
    
    # 分析偏差
    results = analyze_nav_deviation(test_data)
    
    # 打印结果
    print_analysis_results(results)
    print()
    
    # 写入CSV文件
    write_results_to_csv(results)
    print()
    
    # 统计分析
    if results:
        deviations = [abs(item['deviation']) for item in results]
        avg_deviation = sum(deviations) / len(deviations)
        max_deviation = max(deviations)
        min_deviation = min(deviations)
        
        print("=== 整体统计分析 ===")
        print(f"平均偏差: {avg_deviation:.4f}%")
        print(f"最大偏差: {max_deviation:.4f}%")
        print(f"最小偏差: {min_deviation:.4f}%")
        
        # 按基金类型统计分析
        type_groups = analyze_by_fund_type(results)
        
        # 将统计分析结果写入文件
        write_statistics_to_file(results, type_groups, avg_deviation, max_deviation, min_deviation)
    else:
        print("没有找到匹配的数据")
