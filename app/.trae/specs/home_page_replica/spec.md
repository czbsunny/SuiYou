# 首页复刻 - 产品需求文档

## Overview
- **Summary**: 按照设计原型 `designs/home.html` 一比一复刻到 `pages/home/index.vue`
- **Purpose**: 用户要求页面设计完全按照原型实现，保持视觉一致性
- **Target Users**: 小程序用户

## Goals
- 完全复刻设计原型中的所有视觉元素和布局
- 保持相同的配色、间距、字体和交互效果
- 记录缺失的图标供后续补充

## Non-Goals (Out of Scope)
- 不修改其他页面
- 不添加额外功能

## Background & Context
- 设计原型使用 Tailwind CSS 和 Material Icons
- 需要转换为 UniApp 的 SCSS 和自定义图标

## Functional Requirements
- **FR-1**: 显示家庭资产净值卡片，包含金额和增长信息
- **FR-2**: 显示4个快速操作按钮（预算、记账、存钱、报告）
- **FR-3**: 显示近期交易列表，包含4条交易记录

## Constraints
- **Technical**: UniApp框架，SCSS样式，不支持Material Icons
- **Dependencies**: 需要使用自定义图标或文字代替Material Icons

## Assumptions
- 小程序自带导航栏已配置
- 底部tab bar已配置

## Acceptance Criteria

### AC-1: Hero资产卡片复刻
- **Given**: 首页加载完成
- **When**: 查看页面顶部
- **Then**: 显示包含"家庭资产净值"标签、金额¥12,482,930.00、本月增长+$42,105.20 +3.2%的卡片
- **Verification**: `human-judgment`

### AC-2: 快速操作按钮复刻
- **Given**: 首页加载完成
- **When**: 查看Hero卡片下方
- **Then**: 显示4个圆形按钮，分别为预算、记账、存钱、报告
- **Verification**: `human-judgment`

### AC-3: 近期交易列表复刻
- **Given**: 首页加载完成
- **When**: 查看快速操作按钮下方
- **Then**: 显示标题"近期交易"和"查看全部"链接，以及4条交易记录
- **Verification**: `human-judgment`

## Open Questions
- [ ] Material Icons 需要替换为自定义图标，哪些图标需要补充？