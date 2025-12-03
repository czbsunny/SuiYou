# 家庭模块 (Family Module)

## 1. 模块概述

家庭模块用于管理用户的家庭/账套信息。每个用户注册时会自动创建一个家庭，作为用户的原生家庭标识。用户可以在后续加入其他家庭，但原生家庭永远保留。

## 2. 数据结构

### 2.1 家庭表 (families)

| 字段名 | 数据类型 | 约束 | 描述 |
| ------ | -------- | ---- | ---- |
| id | bigint | PRIMARY KEY, AUTO_INCREMENT | 家庭ID |
| name | varchar(50) | NOT NULL | 家庭/账套名称 |
| creatorId | bigint | NOT NULL | 创建者ID（用户的原生家庭标识） |
| currency | varchar(10) | NOT NULL, DEFAULT 'CNY' | 本位币（家庭报表统计时的基准货币） |
| status | tinyint(1) | NOT NULL, DEFAULT 1 | 状态：1=正常, 0=已归档/停用 |
| createdAt | datetime | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updatedAt | datetime | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 2.2 索引设计

- `idx_creator`：用于快速查找用户的原生家庭（用户退出家庭时使用）

## 3. API接口

### 3.1 说明

目前不支持手动创建家庭，家庭只能在用户注册或微信登录时自动创建。

## 4. 业务逻辑

### 4.1 自动创建家庭

- **触发时机**：
  1. 用户手机号注册成功后
  2. 微信登录且是新用户时

- **创建规则**：
  - 家庭名称格式：`用户名 + 的家庭`
  - 默认本位币：CNY（人民币）
  - 初始状态：正常（status=1）

### 4.2 原生家庭标识

每个用户的原生家庭通过`creatorId`字段关联，该字段永远指向创建用户的ID。当用户退出其他家庭时，可以通过`SELECT * FROM families WHERE creator_id = userId`找回原生家庭。

## 5. 相关文件

| 文件路径 | 描述 |
| ------ | ---- |
| `src/main/java/com/suiyou/model/Family.java` | 家庭实体类 |
| `src/main/java/com/suiyou/repository/FamilyRepository.java` | 家庭数据访问接口 |
| `src/main/java/com/suiyou/service/FamilyService.java` | 家庭业务逻辑接口 |
| `src/main/java/com/suiyou/service/impl/FamilyServiceImpl.java` | 家庭业务逻辑实现 |
| `src/main/java/com/suiyou/controller/FamilyController.java` | 家庭控制器（空实现，不提供接口） |
