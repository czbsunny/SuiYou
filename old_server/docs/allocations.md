# 账户资金分配表

## 模块概述
账户资金分配表（allocations）用于关联账户资金和目标，实现物理资金与逻辑目标的映射关系。该表记录了用户将账户中的资金分配到特定目标的操作，支持资金的灵活管理和追踪。

## 数据结构

### 表结构
```sql
CREATE TABLE `allocations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  -- 【归属与权限】
  `family_id` bigint NOT NULL COMMENT '归属家庭ID',
  `creator_id` bigint NOT NULL COMMENT '操作人ID',
  `visible_scope` varchar(20) NOT NULL DEFAULT 'PRIVATE' COMMENT '可见性: PRIVATE/FAMILY (需与关联的Account/Blueprint保持一致)',
  
  -- 【核心分配要素】
  `account_id` bigint NOT NULL COMMENT '资金来源账户 (物理资金在哪里)',
  `goal_id` bigint NOT NULL COMMENT '资金去向目标 (逻辑上是为了什么)',
  
  `amount` decimal(19,4) NOT NULL COMMENT '分配金额 (正数=存入目标, 负数=从目标取出)',
  `allocation_time` datetime NOT NULL COMMENT '操作时间',
  
  -- 【快照/记录】
  -- 记录操作时的汇率或备注，防止未来逻辑混乱
  `memo` varchar(255) DEFAULT NULL COMMENT '分配备注 (如: 年终奖存入)',
  
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`id`),
  
  -- 索引优化
  KEY `idx_goal` (`goal_id`), -- 核心查询：查某个目标现在攒了多少钱 (SUM amount)
  KEY `idx_account` (`account_id`),     -- 辅助查询：查某个账户被分配到了哪些目标
  KEY `idx_family` (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金分配记录表';
```

### 核心字段说明

| 字段名 | 类型 | 描述 | 业务逻辑 |
| --- | --- | --- | --- |
| `family_id` | bigint | 归属家庭ID | 标记分配记录所属的家庭，用于家庭级别的财务统计和权限控制 |
| `creator_id` | bigint | 操作人ID | 记录执行分配操作的用户，用于审计和个人操作记录 |
| `visible_scope` | varchar(20) | 可见性 | 控制分配记录的可见范围，需与关联的Account/Blueprint保持一致 |
| `account_id` | bigint | 资金来源账户ID | 记录资金的物理存储位置，即实际资金所在的账户 |
| `goal_id` | bigint | 资金去向目标ID | 记录资金的逻辑用途，即资金分配的目标 |
| `amount` | decimal(19,4) | 分配金额 | 正数表示存入目标，负数表示从目标取出，支持精确到分的财务计算 |
| `allocation_time` | datetime | 操作时间 | 记录分配操作发生的时间，用于按时间维度查询和统计 |
| `memo` | varchar(255) | 分配备注 | 记录分配操作的附加信息，如资金来源、用途说明等 |

## 业务逻辑

### 分配关系
- **资金来源**：通过`account_id`关联到实际的账户，代表物理资金的位置
- **资金去向**：通过`goal_id`关联到具体的目标，代表资金的逻辑用途
- **金额流向**：
  - 正数金额：将资金从账户分配到目标（存入目标）
  - 负数金额：将资金从目标返还到账户（从目标取出）

### 可见性控制
- 分配记录的可见性需与关联的Account和Blueprint保持一致
- 确保数据访问权限的一致性，避免权限泄漏

### 目标资金统计
- 通过`idx_goal`索引快速查询某个目标的所有分配记录
- 统计`amount`字段的总和，即可得到该目标当前的已分配资金总额

## 相关文件

1. **实体类**：`com.suiyou.model.Allocation`
   - 对应allocations表的实体类
   - 使用JPA注解定义表结构和字段映射

2. **数据访问层**：`com.suiyou.repository.AllocationRepository`
   - 提供分配记录的CRUD操作
   - 支持按目标ID、账户ID、家庭ID等多维度查询
   - 提供金额统计功能，便于快速获取目标或账户的分配总额

## 注意事项

1. 资金分配表暂不需要接口功能，仅用于数据存储和后端处理
2. 金额字段使用decimal(19,4)类型，确保财务数据的精度
3. allocation_time字段记录实际操作时间，而非创建时间
4. visible_scope字段需与关联的Account和Blueprint保持一致，确保权限控制的一致性
5. 通过goal_id查询并统计amount字段，可以快速获取目标的已分配资金总额
