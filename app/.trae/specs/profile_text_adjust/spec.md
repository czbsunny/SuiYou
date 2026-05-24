# 个人中心页文案和样式调整 - 产品需求文档

## Overview
- **Summary**: 修改个人中心页面的用户描述文案，并调整功能入口卡片的大小
- **Purpose**: 更新用户信息文案，优化功能入口的视觉效果
- **Target Users**: 小程序用户

## Why
1. 用户信息描述需要更新为"志同道合，同行xx天"格式
2. 功能入口卡片当前过大，需要调整到合适大小

## What Changes
- 修改 role 文案为"志同道合，同行xx天"（动态计算天数）
- 调整功能入口卡片尺寸，使其更紧凑

## Impact
- Affected specs: profile_restructure
- Affected code: src/pages/profile/index.vue

## ADDED Requirements

### Requirement: 动态天数计算
The system SHALL calculate and display days since registration

#### Scenario: 天数显示
- **WHEN** 个人中心页面加载
- **THEN** 显示"志同道合，同行xx天"
- **AND** 天数从2012年计算到当前日期