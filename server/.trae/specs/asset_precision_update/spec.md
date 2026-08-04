
# Asset 金额精度升级 - 产品需求文档

## Overview
- **Summary**: 将 Asset 实体中的金额字段精度从小数点后2位升级为小数点后8位，支持万亿级别的金额表示
- **Purpose**: 满足大额资产（如投资组合、企业资产）的精确计算需求
- **Target Users**: 财务系统用户、资产管理系统

## Goals
- 将 totalBalance、frozenBalance、availableBalance 字段精度从 (18,2) 升级为 (26,8)
- 支持万亿级别的金额表示（最大约 99999999999999.99999999）
- 保持与现有系统的兼容性

## Non-Goals (Out of Scope)
- 不修改其他字段的精度
- 不修改数据库表结构（仅修改实体定义，数据库迁移需单独处理）
- 不涉及其他实体的修改

## Background & Context
- 当前金额字段使用 decimal(18,2)，最大支持约 999999999999.99（千亿级别）
- 用户需要支持万亿级别的金额，且需要更高的小数精度（8位）
- 精度格式为 (26,8)：26 位总长度，8 位小数

## Functional Requirements
- **FR-1**: 修改 totalBalance 字段精度为 decimal(26,8)
- **FR-2**: 修改 frozenBalance 字段精度为 decimal(26,8)
- **FR-3**: 修改 availableBalance 字段精度为 decimal(26,8)

## Non-Functional Requirements
- **NFR-1**: 保持 BigDecimal 运算精度不变
- **NFR-2**: 保持自动计算 availableBalance 的逻辑不变

## Constraints
- **Technical**: Java BigDecimal 类型，MySQL decimal 类型
- **Dependencies**: 数据库迁移脚本需单独执行

## Assumptions
- 数据库支持 decimal(26,8) 类型
- 现有数据迁移不会丢失精度

## Acceptance Criteria

### AC-1: totalBalance 精度升级
- **Given**: Asset 实体已存在
- **When**: 查看 totalBalance 字段定义
- **Then**: 字段精度为 decimal(26,8)
- **Verification**: `human-judgment`

### AC-2: frozenBalance 精度升级
- **Given**: Asset 实体已存在
- **When**: 查看 frozenBalance 字段定义
- **Then**: 字段精度为 decimal(26,8)
- **Verification**: `human-judgment`

### AC-3: availableBalance 精度升级
- **Given**: Asset 实体已存在
- **When**: 查看 availableBalance 字段定义
- **Then**: 字段精度为 decimal(26,8)
- **Verification**: `human-judgment`

### AC-4: 自动计算逻辑保持
- **Given**: Asset 实体已更新
- **When**: 创建或更新 Asset 记录
- **Then**: availableBalance = totalBalance - frozenBalance
- **Verification**: `programmatic`

## Open Questions
- [ ] 是否需要同步更新数据库迁移脚本？
