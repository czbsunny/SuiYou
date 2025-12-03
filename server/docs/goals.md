# 目标(蓝图)模块设计文档

## 模块概述
目标(蓝图)模块用于管理用户的财务目标，包括目标的创建、账户关联和进度追踪。

## 数据结构

### 1. 目标表 (goals)

#### 表结构
```sql
CREATE TABLE `goals` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  -- 归属与权限
  `family_id` bigint NOT NULL COMMENT '归属家庭ID',
  `creator_id` bigint NOT NULL COMMENT '创建人ID',
  `visible_scope` varchar(20) NOT NULL DEFAULT 'PRIVATE' COMMENT '可见性: PRIVATE(仅自己), FAMILY(全家)',
  -- 核心分类
  `category` varchar(20) NOT NULL COMMENT '一级分类: WISH, BIG_ITEM, SECURITY, GROWTH, WEALTH',
  `sub_category` varchar(50) NOT NULL COMMENT '二级分类代码: TRAVEL, DEBT_CLEARANCE...',
  -- 展示信息
  `name` varchar(50) NOT NULL COMMENT '目标名称',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标/Emoji',
  `color` varchar(20) DEFAULT NULL COMMENT '卡片颜色(Hex)',
  -- 金额管理
  `target_amount` decimal(19,4) NOT NULL COMMENT '目标总金额',
  -- 进度快照
  `current_amount` decimal(19,4) NOT NULL DEFAULT '0.0000' COMMENT '当前进度金额',
  -- 时间规划
  `horizon` varchar(20) NOT NULL DEFAULT 'SHORT' COMMENT '周期: SHORT, MEDIUM, LONG',
  `start_date` date NOT NULL DEFAULT (CURRENT_DATE),
  `deadline` date DEFAULT NULL,
  -- 状态管理
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1=进行中, 2=已达成, 3=已归档, 0=已放弃',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_family_status` (`family_id`, `status`),
  KEY `idx_mode` (`drive_mode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目标(蓝图)表';
```

#### 核心字段说明

| 字段名 | 类型 | 说明 | 核心功能 |
|-------|------|------|---------|
| `family_id` | bigint | 归属家庭ID | 确定目标所属的家庭范围 |
| `creator_id` | bigint | 创建人ID | 记录目标的创建者 |
| `visible_scope` | varchar(20) | 可见性 | 控制目标的查看权限（PRIVATE/FAMILY） |
| `category` | varchar(20) | 一级分类 | 目标的主要类型（WISH, BIG_ITEM, SECURITY, GROWTH, WEALTH） |
| `sub_category` | varchar(50) | 二级分类代码 | 目标的具体子类型（如TRAVEL, DEBT_CLEARANCE等） |
| `target_amount` | decimal(19,4) | 目标总金额 | 目标需要达成的金额 |
| `current_amount` | decimal(19,4) | 当前进度金额 | 目标已累计的金额（冗余字段，提高查询效率） |
| `status` | tinyint(1) | 状态 | 目标的当前状态（1=进行中, 2=已达成, 3=已归档, 0=已放弃） |

### 2. 目标与账户关联表 (goal_account_relations)

#### 表结构
```sql
CREATE TABLE `goal_account_relations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goal_id` bigint NOT NULL COMMENT '目标ID',
  `account_id` bigint NOT NULL COMMENT '关联的账户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_goal_account` (`goal_id`, `account_id`),
  KEY `idx_account` (`account_id`),
  CONSTRAINT `fk_gar_goal` FOREIGN KEY (`goal_id`) REFERENCES `goals` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_gar_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目标与账户关联表';
```

#### 核心字段说明

| 字段名 | 类型 | 说明 | 核心功能 |
|-------|------|------|---------|
| `goal_id` | bigint | 目标ID | 关联到goals表的主键 |
| `account_id` | bigint | 账户ID | 关联到accounts表的主键 |
| `created_at` | datetime | 创建时间 | 记录关联关系的创建时间 |

## 业务逻辑

### 1. 目标管理
- 目标分为归属家庭ID和创建人ID，确保数据的归属和权限控制
- 可见性范围控制，支持私有和家庭共享两种模式
- 分类体系完善，支持一级分类和二级分类
- 金额管理功能，包括目标金额和当前进度
- 时间规划，支持周期和截止日期
- 状态管理，支持进行中、已达成、已归档和已放弃四种状态

### 2. 目标与账户关联
- 唯一性约束，防止同一个账户被同一个目标关联两次
- 双向查询索引，支持快速查询账户变动影响的目标
- 外键约束，确保数据一致性，防止脏数据
- 级联删除，当目标或账户被删除时，关联关系自动删除

## 相关文件

1. 实体类
   - `Goal.java` - 目标表的实体类
   - `GoalAccountRelation.java` - 目标与账户关联表的实体类

2. 数据访问层
   - `GoalRepository.java` - 目标表的Repository接口
   - `GoalAccountRelationRepository.java` - 目标与账户关联表的Repository接口

3. 文档
   - `goals.md` - 目标模块的设计文档

## 注意事项

1. 数据一致性
   - 外键约束确保了目标与账户之间的关联关系一致性
   - 唯一性约束防止重复关联

2. 查询性能
   - 索引设计优化了常用查询场景
   - 冗余字段`current_amount`提高了列表查询效率

3. 权限控制
   - 家庭ID和可见性范围共同控制数据的访问权限
   - 创建人ID记录了数据的创建者信息

4. 级联操作
   - 级联删除确保了数据的完整性，防止孤立数据
