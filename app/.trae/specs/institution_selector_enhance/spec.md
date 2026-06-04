# 机构选择页面增强 - 产品需求文档

## Overview
- **Summary**: 为机构选择页面添加右侧字母索引条功能，并从 web 端拷贝机构 logo 图标资源。
- **Purpose**: 提升用户体验，支持快速定位机构列表，并显示机构 logo。
- **Target Users**: App 用户搜索和选择金融机构。

## Goals
- 添加右侧字母索引条，支持点击快速滚动定位
- 拷贝 web 端的机构 logo 图标到 app 端
- 更新机构卡片组件显示 logo

## Non-Goals (Out of Scope)
- 修改其他页面或组件
- 添加新的业务功能

## Background & Context
web 端已实现完整的字母索引功能，需要同步到 app 端。API 返回的机构数据包含 logoUrl 字段，需要相应的图标资源支持。

## Functional Requirements
- **FR-1**: 添加右侧字母索引条，点击字母可滚动到对应分组
- **FR-2**: 列表按字母分组展示，每组显示字母标题
- **FR-3**: 滚动列表时索引条高亮当前字母
- **FR-4**: 从 web 端拷贝所有机构 logo 图标

## Non-Functional Requirements
- **NFR-1**: 索引条响应迅速，点击后快速滚动
- **NFR-2**: logo 图标加载正常，不影响页面性能

## Constraints
- **Technical**: UniApp/Vue 3 语法，小程序环境
- **Dependencies**: 需要从 web 端拷贝资源

## Assumptions
- API 返回的机构数据包含 indexLetter 和 logoUrl 字段

## Acceptance Criteria

### AC-1: 字母索引条显示
- **Given**: 页面已加载
- **When**: 查看页面右侧
- **Then**: 显示字母索引条，包含热门和字母 A-Z
- **Verification**: `human-judgment`

### AC-2: 点击索引滚动
- **Given**: 索引条已显示
- **When**: 点击某个字母
- **Then**: 列表滚动到对应字母分组
- **Verification**: `human-judgment`

### AC-3: 滚动高亮索引
- **Given**: 列表正在滚动
- **When**: 滚动经过字母分组
- **Then**: 索引条对应字母高亮
- **Verification**: `human-judgment`

### AC-4: Logo 图标显示
- **Given**: 机构数据包含 logoUrl
- **When**: 页面加载机构列表
- **Then**: 每个机构卡片显示对应 logo
- **Verification**: `human-judgment`

## Open Questions
- [ ] None