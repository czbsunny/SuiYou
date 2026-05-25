# AssetType 枚举重构 - 产品需求文档

## Overview
- **Summary**: 参考 sys_asset_category_init.json 的资产分类结构，重构现有的 AssetType 枚举，使其更符合业务资产分类体系。
- **Purpose**: 统一资产类型枚举与系统资产分类配置，确保数据一致性和扩展性。
- **Target Users**: 后端开发人员、数据分析师

## Goals
- 使 AssetType 枚举与系统资产分类配置保持一致
- 支持资产模块与具体资产类型的关联
- 提供清晰的资产类型分类层次

## Non-Goals (Out of Scope)
- 修改现有的资产分类配置文件 sys_asset_category_init.json
- 修改 Asset 实体的字段结构
- 迁移历史数据

## Background & Context
现有系统通过 sys_asset_category_init.json 配置资产分类，包含：
1. 流动资产 (LIQUID)
2. 投资理财 (INVEST)
3. 固定资产 (FIXED)
4. 其他资产 (OTHER_ASSET)
5. 负债管理 (LOAN)

当前 AssetType 枚举未完全覆盖这些分类，需要重构以保持一致性。

## Functional Requirements
- **FR-1**: 枚举值需覆盖所有主要资产类别
- **FR-2**: 储蓄卡类型改为 CURRENT（活期）
- **FR-3**: 活期理财类型使用 CURRENT_PLUS
- **FR-4**: 现金类型独立为 CASH
- **FR-5**: 包含负债类型

## Non-Functional Requirements
- **NFR-1**: 枚举命名符合 Java 命名规范（大写下划线）
- **NFR-2**: 提供中文描述便于前端展示

## Constraints
- **Technical**: 基于 Spring Boot 3.x + JPA 环境
- **Dependencies**: 需与 AccountModule 实体配合使用

## Assumptions
- 现有代码中使用 AssetType 的地方需要适配新的枚举值
- 数据库迁移需单独处理

## Acceptance Criteria

### AC-1: 枚举包含流动资产类型
- **Given**: 查看 AssetType 枚举定义
- **When**: 检查枚举值列表
- **Then**: 包含 CURRENT（活期）、CURRENT_PLUS（活期理财）、CASH（现金）
- **Verification**: `programmatic`

### AC-2: 枚举包含投资理财类型
- **Given**: 查看 AssetType 枚举定义
- **When**: 检查枚举值列表
- **Then**: 包含 TIME_DEPOSIT、FUND、STOCK、BANK_PRODUCT、GOLD、PORTFOLIO、PRIVATE_FUND、SAVING_INSURANCE、CRYPTO
- **Verification**: `programmatic`

### AC-3: 枚举包含固定资产类型
- **Given**: 查看 AssetType 枚举定义
- **When**: 检查枚举值列表
- **Then**: 包含 REAL_ESTATE、VEHICLE、JEWELRY
- **Verification**: `programmatic`

### AC-4: 枚举包含其他资产类型
- **Given**: 查看 AssetType 枚举定义
- **When**: 检查枚举值列表
- **Then**: 包含 HOUSING_FUND、MEDICAL_INSURANCE、PENSION_ACCOUNT、PRIVATE_PENSION、RECEIVABLE
- **Verification**: `programmatic`

### AC-5: 枚举包含负债类型
- **Given**: 查看 AssetType 枚举定义
- **When**: 检查枚举值列表
- **Then**: 包含 MORTGAGE、CAR_LOAN、CREDIT_CARD、INTERNET_CREDIT、CONSUMER_LOAN、PAYABLE
- **Verification**: `programmatic`

### AC-6: 每个枚举值都有中文描述
- **Given**: 查看 AssetType 枚举定义
- **When**: 检查每个枚举值的 description 属性
- **Then**: 所有枚举值都有非空的中文描述
- **Verification**: `human-judgment`

## Open Questions
- [ ] 是否需要保留原有的 OTHER 类型？
