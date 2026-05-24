# 资产页时间线修复 - 产品需求文档

## Overview
- **Summary**: 修复资产页头部曲线图的时间标签，使其动态显示当前月份往前推一年的12个月
- **Purpose**: 确保时间线始终显示正确的历史数据范围
- **Target Users**: 小程序用户

## Why
当前资产页的时间线使用固定标签，无法反映真实的数据时间范围。需要改为动态生成，从当前月份往前推一年。

## What Changes
- 修改 chart-labels 时间标签为动态生成
- 使用 JavaScript Date API 计算月份
- 保持 5 个时间节点：起始月、+3月、+6月、+9月、当前月

## Impact
- Affected specs: home_page_replica
- Affected code: src/pages/assets/index.vue

## ADDED Requirements

### Requirement: 动态时间线
The system SHALL generate time labels based on current date

#### Scenario: 时间标签显示
- **WHEN** 资产页面加载
- **THEN** 显示5个时间节点：从当前月份往前推一年，均匀分布
- **Example**: 如果当前是 2024年5月，显示：'23.05', '23.08', '23.11', '24.02', '24.05'

## Implementation Notes
- 使用 `new Date()` 获取当前日期
- 使用 `getMonth()` 和 `getFullYear()` 提取年月
- 生成12个月前到当前月的5个等间隔时间点
- 格式为 YY.MM