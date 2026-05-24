# 目标页排序功能 - 产品需求文档

## Overview
- **Summary**: 修改目标页面愿望清单区域的排序功能，使排序选项（chips）默认隐藏，点击排序按钮后显示
- **Purpose**: 优化页面布局，减少视觉干扰，提供更好的交互体验
- **Target Users**: 小程序用户

## Goals
- 实现排序选项的显示/隐藏切换功能
- 点击排序按钮时显示排序选项，再次点击或点击其他区域时隐藏
- 保持现有的排序逻辑不变

## Non-Goals (Out of Scope)
- 不修改其他页面
- 不修改排序功能的具体实现逻辑

## Background & Context
- 当前页面中排序选项（chips）直接显示在页面上
- 用户希望将其改为点击排序按钮才显示，提升界面简洁度

## Functional Requirements
- **FR-1**: 排序选项默认不显示
- **FR-2**: 点击排序按钮（filter图标）时显示排序选项
- **FR-3**: 再次点击排序按钮时隐藏排序选项
- **FR-4**: 点击排序选项后隐藏选项并执行排序

## Constraints
- **Technical**: UniApp框架，Vue 3 Composition API
- **Dependencies**: 需要使用条件渲染控制chips显示

## Acceptance Criteria

### AC-1: 排序选项默认隐藏
- **Given**: 目标页面加载完成
- **When**: 查看愿望清单区域
- **Then**: 排序选项（chips）不显示
- **Verification**: `human-judgment`

### AC-2: 点击排序按钮显示选项
- **Given**: 目标页面加载完成
- **When**: 点击排序按钮（filter图标）
- **Then**: 排序选项（chips）显示
- **Verification**: `human-judgment`

### AC-3: 再次点击隐藏选项
- **Given**: 排序选项已显示
- **When**: 再次点击排序按钮
- **Then**: 排序选项（chips）隐藏
- **Verification**: `human-judgment`

### AC-4: 点击选项后隐藏并排序
- **Given**: 排序选项已显示
- **When**: 点击某个排序选项
- **Then**: 排序选项隐藏，并且列表按照选中选项排序
- **Verification**: `human-judgment`
