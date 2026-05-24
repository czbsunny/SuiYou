# 资产页曲线图修复 - 产品需求文档

## Overview
- **Summary**: 简化资产页头部曲线图，移除复杂的渐变区域，只保留平滑折线图和实心圆点
- **Purpose**: 修复曲线图绘制问题，简化视觉效果
- **Target Users**: 小程序用户

## Why
当前曲线图实现过于复杂，使用了渐变区域和 clip-path，导致绘制不准确。需要简化为更清晰、更易维护的实现方式。

## What Changes
- 移除渐变区域（chart-area）
- 移除斜线（line）
- 使用 SVG 或 Canvas 实现平滑折线图
- 在每个月份位置显示实心圆点

## Impact
- Affected specs: assets_timeline_fix
- Affected code: src/pages/assets/index.vue

## ADDED Requirements

### Requirement: 平滑折线图
The system SHALL display a smooth line chart

#### Scenario: 折线图显示
- **WHEN** 资产页面加载
- **THEN** 显示一条平滑的上升折线
- **AND** 折线颜色为主题色 ($primary)

### Requirement: 月份实心圆点
The system SHALL display solid dots at each month position

#### Scenario: 圆点显示
- **WHEN** 折线图渲染完成
- **THEN** 在每个月份（5个点）的位置显示实心圆点
- **AND** 圆点大小一致

## Technical Approach
- 使用 SVG 的 polyline 或 path 实现折线
- 使用 circle 元素显示实心圆点
- 保持与现有时间标签的对齐