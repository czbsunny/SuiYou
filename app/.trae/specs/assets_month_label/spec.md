# 资产页时间标签格式修复 - 产品需求文档

## Overview
- **Summary**: 修改资产页曲线图下方的时间标签格式，从 YY.MM 改为中文月份格式（1月、2月等），并添加label位置信息
- **Purpose**: 与设计原型保持一致，使用更直观的中文月份显示
- **Target Users**: 小程序用户

## Why
当前时间标签显示格式为 '23.05、'23.08 等，用户希望改为更直观的中文月份格式如 1月、2月。

## What Changes
- 修改 `getMonthLabel` 函数返回中文月份格式
- 保留每个时间节点对应哪个月份的信息
- 确保标签与折线图上的圆点正确对齐

## Impact
- Affected specs: assets_chart_fix
- Affected code: src/pages/assets/index.vue

## ADDED Requirements

### Requirement: 中文月份标签
The system SHALL display month labels in Chinese format

#### Scenario: 月份标签显示
- **WHEN** 资产页面加载
- **THEN** 显示5个中文月份标签（1月、2月、3月等）
- **AND** 标签位置与折线图圆点对齐

### Requirement: 标签位置对齐
The system SHALL align labels with chart dots

#### Scenario: 位置对齐
- **WHEN** 折线图和标签渲染完成
- **THEN** 每个月份标签与对应的圆点水平居中对齐