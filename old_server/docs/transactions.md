# 交易流水表

## 模块概述
交易流水表（transactions）用于记录用户的所有资金交易活动，包括支出、收入和转账操作。该表采用复式记账逻辑，支持家庭和个人的财务记录管理，并提供灵活的可见性控制。

## 数据结构

### 表结构
```sql
CREATE TABLE `transactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  -- 【归属与权限】
  `family_id` bigint NOT NULL COMMENT '当前归属家庭ID',
  `user_id` bigint NOT NULL COMMENT '操作人/记账人ID',
  `visible_scope` varchar(20) NOT NULL DEFAULT 'PRIVATE' COMMENT '可见性: PRIVATE/FAMILY (冗余字段，写入时计算)',

  -- 【核心交易要素】
  `type` varchar(20) NOT NULL COMMENT '交易类型: EXPENSE(支出), INCOME(收入), TRANSFER(转账)',
  `amount` decimal(19,4) NOT NULL COMMENT '金额 (绝对值，恒为正数)',
  `trans_time` datetime NOT NULL COMMENT '交易发生时间',
  
  -- 【资金流向 - 核心复式记账逻辑】
  -- 支出: source=钱包, target=NULL
  -- 收入: source=NULL, target=钱包
  -- 转账: source=钱包A, target=钱包B
  `source_account_id` bigint DEFAULT NULL COMMENT '流出账户 (支出/转出)',
  `target_account_id` bigint DEFAULT NULL COMMENT '流入账户 (收入/转入)',
  
  -- 【分类信息】
  -- 仅用于 EXPENSE 和 INCOME。TRANSFER 时通常为空或为特定“转账”分类
  `category_id` bigint DEFAULT NULL COMMENT '关联交易分类ID',
  
  -- 【元数据】
  `description` varchar(255) DEFAULT NULL COMMENT '备注/描述',
  `tags` json DEFAULT NULL COMMENT '标签 (如: #出差 #装修)',
  `location` varchar(100) DEFAULT NULL COMMENT '地理位置',
  
  -- 【关联扩展】
  -- 如果这笔交易是由“分配”操作触发的自动转账，可关联 allocation_id
  -- 但通常建议保持解耦，这里不强制关联
  
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`id`),
  
  -- 索引优化
  KEY `idx_family_time` (`family_id`, `trans_time`), -- 查家庭月度流水
  KEY `idx_user_time` (`user_id`, `trans_time`),     -- 查个人流水
  KEY `idx_source` (`source_account_id`),             -- 查账户流出
  KEY `idx_target` (`target_account_id`)              -- 查账户流入
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易流水表';
```

### 核心字段说明

| 字段名 | 类型 | 描述 | 业务逻辑 |
| --- | --- | --- | --- |
| `family_id` | bigint | 当前归属家庭ID | 标记交易所属的家庭，用于家庭级别的财务统计和权限控制 |
| `user_id` | bigint | 操作人/记账人ID | 记录执行该交易的用户，用于个人记账统计和审计 |
| `visible_scope` | varchar(20) | 可见性 | 控制交易的可见范围：PRIVATE(仅自己可见)或FAMILY(家庭共享) |
| `type` | varchar(20) | 交易类型 | 区分支出(EXPENSE)、收入(INCOME)和转账(TRANSFER) |
| `amount` | decimal(19,4) | 金额 | 交易金额的绝对值，恒为正数，便于统计计算 |
| `trans_time` | datetime | 交易发生时间 | 记录实际交易发生的时间，用于按时间维度查询和统计 |
| `source_account_id` | bigint | 流出账户ID | 支出和转账时的资金来源账户 |
| `target_account_id` | bigint | 流入账户ID | 收入和转账时的资金去向账户 |
| `category_id` | bigint | 交易分类ID | 关联交易分类表，用于支出和收入的分类统计 |
| `tags` | json | 标签 | 灵活的标签系统，支持多维度的交易标记和查询 |

## 业务逻辑

### 交易类型与资金流向

| 交易类型 | source_account_id | target_account_id | 业务含义 |
| --- | --- | --- | --- |
| EXPENSE | 钱包ID | NULL | 从钱包支出一笔费用 |
| INCOME | NULL | 钱包ID | 收入一笔资金到钱包 |
| TRANSFER | 钱包A ID | 钱包B ID | 从钱包A转账到钱包B |

### 可见性控制
- **PRIVATE**：仅记账人自己可见
- **FAMILY**：家庭成员共享可见

### 标签系统
支持JSON格式的标签，如：
```json
["出差", "装修", "生日礼物"]
```
便于按标签维度进行交易统计和分析。

## 相关文件

1. **实体类**：`com.suiyou.model.Transaction`
   - 对应transactions表的实体类
   - 使用JPA注解定义表结构和字段映射

2. **数据访问层**：`com.suiyou.repository.TransactionRepository`
   - 提供交易数据的CRUD操作
   - 支持按家庭、用户、时间范围、账户等多维度查询

## 注意事项

1. 流水表暂不需要接口功能，仅用于数据存储和后端处理
2. 金额字段使用decimal(19,4)类型，确保财务数据的精度
3. trans_time字段记录实际交易发生时间，而非创建时间
4. 资金流向字段(source_account_id和target_account_id)遵循复式记账逻辑，确保资金的来源和去向清晰可查
5. visible_scope字段为冗余字段，在写入时根据业务规则计算得出
