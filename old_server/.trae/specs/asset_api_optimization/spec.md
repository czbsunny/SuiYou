# 资产API优化 - 产品需求文档

## Overview
- **Summary**: 优化资产创建API，使创建资产时只需指定moduleId，系统自动关联资产分类信息，不再需要创建账户（因为module已绑定到account）。
- **Purpose**: 简化资产创建流程，提高API易用性，符合"Account -> AccountModule -> Asset"的数据模型。
- **Target Users**: 前端开发人员、API消费者

## Goals
- 简化CreateAssetDTO，移除accountDTO字段
- 创建资产时自动从AccountModule获取账户关联信息
- 自动从资产类型获取默认分类信息
- 提供清晰的错误处理

## Non-Goals (Out of Scope)
- 修改AccountModule数据模型
- 修改Asset数据模型结构
- 修改资产分类配置文件

## Background & Context
当前数据模型：
```
Account 
   -> AccountModule(assetType, moduleName) 
       -> Asset
```

当前API存在的问题：
1. CreateAssetDTO包含accountDTO字段，与moduleId重复（因为module已绑定account）
2. 创建资产时需要手动指定groupType和category，应该从资产类型自动推断

## Functional Requirements
- **FR-1**: 移除CreateAssetDTO中的accountDTO字段
- **FR-2**: 创建资产时自动从AccountModule获取account关联
- **FR-3**: 根据assetType自动填充groupType和category
- **FR-4**: 提供清晰的错误提示（如module不存在、权限验证等）

## Non-Functional Requirements
- **NFR-1**: API响应时间<100ms
- **NFR-2**: 错误信息友好且明确

## Constraints
- **Technical**: Spring Boot 3.x + JPA
- **Dependencies**: AccountModuleService、SysAssetCategoryRepository

## Assumptions
- AccountModule已存在且绑定到Account
- AssetType与SysAssetCategory存在映射关系

## Acceptance Criteria

### AC-1: CreateAssetDTO简化
- **Given**: 查看CreateAssetDTO定义
- **When**: 检查字段列表
- **Then**: 不包含accountDTO字段
- **Verification**: `programmatic`

### AC-2: 自动关联账户
- **Given**: 创建资产时只提供moduleId
- **When**: 调用createAsset接口
- **Then**: 资产自动关联到module对应的账户
- **Verification**: `programmatic`

### AC-3: 自动填充分类信息
- **Given**: 创建资产时提供assetType
- **When**: 调用createAsset接口
- **Then**: groupType和category自动填充
- **Verification**: `programmatic`

### AC-4: 错误处理
- **Given**: 使用不存在的moduleId创建资产
- **When**: 调用createAsset接口
- **Then**: 返回400错误，提示"资产模块不存在"
- **Verification**: `programmatic`

## Open Questions
- [ ] 是否需要保留category字段供用户自定义？
