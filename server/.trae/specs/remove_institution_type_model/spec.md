# 机构类型重构 - 产品需求文档

## Overview
- **Summary**: 移除数据库实体 `SysInstitutionType`，改为直接从枚举 `InstType` 获取机构类型信息，简化架构设计。
- **Purpose**: 减少数据库表依赖，简化数据模型，提升系统维护性。
- **Target Users**: 开发团队

## Goals
- 移除 `SysInstitutionType.java` 实体类
- 移除 `SysInstitutionTypeRepository.java`
- 移除 `SysInstitutionTypeServiceImpl.java`
- 将机构类型服务合并到 `SysInstitutionServiceImpl.java`
- 更新 `InstitutionController.java` 使用新的服务接口

## Non-Goals (Out of Scope)
- 不修改 `InstType.java` 枚举的结构
- 不修改 `SysInstitution.java` 实体的核心结构
- 不修改数据库表结构（schema.sql）

## Background & Context
当前系统中机构类型同时存在枚举定义和数据库实体，导致数据冗余和维护复杂度。通过移除数据库实体，直接使用枚举可以简化架构。

## Functional Requirements
- **FR-1**: 移除 `SysInstitutionType` 实体类及其 Repository
- **FR-2**: 移除 `SysInstitutionTypeServiceImpl` 服务实现
- **FR-3**: 在 `SysInstitutionServiceImpl` 中实现机构类型相关方法
- **FR-4**: 更新 `InstitutionController` 使用新的服务接口

## Non-Functional Requirements
- **NFR-1**: API 接口保持不变，不影响外部调用者
- **NFR-2**: 代码重构不引入新的 bug

## Constraints
- **Technical**: Spring Boot + JPA 框架约束
- **Dependencies**: 需要保持与现有代码的兼容性

## Assumptions
- 机构类型数据是静态的，不需要动态配置
- 枚举 `InstType` 已包含所有必要的机构类型信息

## Acceptance Criteria

### AC-1: SysInstitutionType 实体移除
- **Given**: 项目存在 `SysInstitutionType.java` 实体
- **When**: 执行删除操作
- **Then**: 该文件不再存在于项目中
- **Verification**: `human-judgment`

### AC-2: SysInstitutionTypeRepository 移除
- **Given**: 项目存在 `SysInstitutionTypeRepository.java`
- **When**: 执行删除操作
- **Then**: 该文件不再存在于项目中
- **Verification**: `human-judgment`

### AC-3: SysInstitutionTypeServiceImpl 移除
- **Given**: 项目存在 `SysInstitutionTypeServiceImpl.java`
- **When**: 执行删除操作
- **Then**: 该文件不再存在于项目中
- **Verification**: `human-judgment`

### AC-4: SysInstitutionService 新增机构类型方法
- **Given**: `SysInstitutionService.java` 接口
- **When**: 添加 getAllInstitutionTypes() 和 getInstitutionTypeByCode() 方法
- **Then**: 接口包含这两个新方法
- **Verification**: `human-judgment`

### AC-5: SysInstitutionServiceImpl 实现机构类型逻辑
- **Given**: `SysInstitutionServiceImpl.java`
- **When**: 实现机构类型相关方法
- **Then**: 方法从枚举 `InstType` 获取数据
- **Verification**: `human-judgment`

### AC-6: InstitutionController 更新
- **Given**: `InstitutionController.java` 使用了两个服务
- **When**: 修改为只使用 `SysInstitutionService`
- **Then**: 控制器编译通过
- **Verification**: `programmatic`

## Open Questions
- [ ] 无