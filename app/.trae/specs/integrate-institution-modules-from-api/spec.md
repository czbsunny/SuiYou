# 机构资产模块从后端获取 - Product Requirement Document

## Overview

* **Summary**: 将 add-account.vue 页面从使用硬编码的 mock 模块数据，改为从后端 API 获取机构对应的资产模块数据（必选、默认、可选三类）并显示

* **Purpose**: 使前端显示的资产模块与后端配置保持一致，支持动态配置和更新

* **Target Users**: 使用 add-account 页面添加账户的用户

## Goals

* 调用后端 API `/api/institutions/${instCode}/modules` 获取机构资产模块

* 根据返回的 required、defaultList、optional 三类数据正确显示模块

* 保持原有模块选择和交互功能

* 处理加载状态和错误情况

## Non-Goals (Out of Scope)

* 修改后端 API 返回格式（已实现完成）

* 修改模块选择交互逻辑

* 添加新的功能特性

## Background & Context

* 后端已提供接口 `/api/institutions/${instCode}/modules`，返回数据格式：

  ```json
  {
    "required": [ { categoryCode, name, iconUrl, ... } ],
    "defaultList": [ { categoryCode, name, iconUrl, ... } ],
    "optional": [ { categoryCode, name, iconUrl, ... } ]
  }
  ```

* 当前代码使用 `loadMockModules()` 加载硬编码数据

* API 调用函数 `getInstitutionModules()` 已在 `@/api/modules/asset.js` 中定义

## Functional Requirements

* **FR-1**: 页面加载时调用后端 API 获取机构模块

* **FR-2**: 正确处理后端返回的三类模块数据并显示

* **FR-3**: 显示加载状态，处理加载错误

## Non-Functional Requirements

* **NFR-1**: API 调用失败时要有友好的错误提示

* **NFR-2**: 保持原有的模块选择交互体验

## Constraints

* **Technical**: 使用现有的 API 调用方式和数据结构

* **Business**: 保持现有 UI 样式和交互不变

## Assumptions

* 后端 API 返回的数据包含完整的模块信息（categoryCode、name、iconUrl、description等）

* API 调用错误处理使用现有的 uni-app 错误提示方式

## Acceptance Criteria

### AC-1: 正确调用后端 API 获取模块

* **Given**: 用户进入 add-account 页面并传入了 instCode

* **When**: 页面加载完成

* **Then**: 调用 `getInstitutionModules(instCode)` API，不再使用 mock 数据

* **Verification**: `programmatic`

### AC-2: 正确显示三类模块

* **Given**: API 返回了三类模块数据

* **When**: 数据加载完成

* **Then**:

  * required 模块显示在「必选模块」分组

  * defaultList 模块显示在「默认模块」分组

  * optional 模块显示在「可选模块」分组

* **Verification**: `programmatic`

### AC-3: 处理加载状态

* **Given**: 正在加载 API 数据

* **When**: API 调用进行中

* **Then**: 显示加载状态（可选，保持现有体验）

* **Verification**: `human-judgment`

### AC-4: 处理错误情况

* **Given**: API 调用失败

* **When**: 发生网络错误或服务器错误

* **Then**: 显示友好的错误提示，不影响页面其他功能

* **Verification**: `human-judgment`

### AC-5: 保持原有模块选择功能

* **Given**: 模块数据已加载

* **When**: 用户点击模块进行选择/取消

* **Then**: 模块选择功能正常工作，与之前一致

* **Verification**: `programmatic`

## Open Questions

* [ ] 无

