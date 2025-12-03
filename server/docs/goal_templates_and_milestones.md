# 目标模板与里程碑模块设计文档

## 模块概述
目标模板与里程碑模块用于管理目标的预设模板和进度里程碑，帮助用户更便捷地创建目标并追踪进度。

## 数据结构

### 1. 目标模板配置表 (sys_goal_templates)

#### 表结构
```sql
CREATE TABLE `sys_goal_templates` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  -- 【Level 1: 大类】
  -- 对应 Java Enum: WISH, BIG_ITEM, SECURITY, GROWTH, WEALTH
  `category` varchar(20) NOT NULL COMMENT '一级分类',
  
  -- 【Level 2: 子项代码】
  -- 用于代码逻辑判断 (如: DEBT_CLEARANCE 需要触发关联账户逻辑)
  -- 比如: TRAVEL, HOUSING_DOWN_PAYMENT, DEBT_CLEARANCE
  `sub_category` varchar(50) NOT NULL COMMENT '二级分类代码',
  
  -- 【UI 展示】
  `name` varchar(50) NOT NULL COMMENT '显示名称 (如: 一次旅行, 还清债务)',
  `icon` varchar(50) NOT NULL COMMENT '图标/Emoji',
  `color_hex` varchar(20) DEFAULT NULL COMMENT '卡片背景色',
  
  -- 【预设值】
  `default_horizon` varchar(20) DEFAULT 'SHORT' COMMENT '默认周期: SHORT/MEDIUM/LONG',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sub_cat` (`sub_category`), -- 子分类代码唯一
  KEY `idx_category` (`category`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目标模板配置表';
```

#### 核心字段说明

| 字段名 | 类型 | 说明 | 核心功能 |
|-------|------|------|---------|
| `category` | varchar(20) | 一级分类 | 对应Java Enum: WISH(心愿), BIG_ITEM(大项), SECURITY(保障), GROWTH(成长), WEALTH(财富) |
| `sub_category` | varchar(50) | 二级分类代码 | 用于代码逻辑判断 (如: DEBT_CLEARANCE 需要触发关联账户逻辑) |
| `name` | varchar(50) | 显示名称 | 用户界面展示的目标名称 (如: 一次旅行, 还清债务) |
| `icon` | varchar(50) | 图标/Emoji | 目标卡片显示的图标 |
| `color_hex` | varchar(20) | 卡片背景色 | 目标卡片的背景颜色 |
| `default_horizon` | varchar(20) | 默认周期 | 预设的目标周期: SHORT(短期), MEDIUM(中期), LONG(长期) |
| `sort_order` | int | 排序 | 目标模板在UI中的显示顺序 |

### 2. 目标里程碑表 (goal_milestones)

#### 表结构
```sql
CREATE TABLE `goal_milestones` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goal_id` bigint NOT NULL COMMENT '归属目标',
  
  `name` varchar(50) NOT NULL COMMENT '里程碑名称 (如: 存够机票钱)',
  `target_amount` decimal(19,4) NOT NULL COMMENT '该阶段金额节点',
  
  `is_reached` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否达成',
  `reached_at` datetime DEFAULT NULL,
  
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '顺序',
  
  PRIMARY KEY (`id`),
  KEY `idx_goal` (`goal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目标里程碑表';
```

#### 核心字段说明

| 字段名 | 类型 | 说明 | 核心功能 |
|-------|------|------|---------|
| `goal_id` | bigint | 归属目标ID | 关联到goals表的主键 |
| `name` | varchar(50) | 里程碑名称 | 里程碑的显示名称 (如: 存够机票钱) |
| `target_amount` | decimal(19,4) | 该阶段金额节点 | 里程碑对应的目标金额 |
| `is_reached` | tinyint(1) | 是否达成 | 标记里程碑是否已达成 |
| `reached_at` | datetime | 达成时间 | 记录里程碑达成的时间 |
| `sort_order` | int | 顺序 | 里程碑在UI中的显示顺序 |

## 业务逻辑

### 1. 目标模板管理
- 系统预设目标模板，用户可以选择模板快速创建目标
- 模板分类体系完善，支持一级分类和二级分类
- 模板包含UI展示信息（名称、图标、颜色）和预设值（默认周期）
- 子分类代码用于代码逻辑判断，如DEBT_CLEARANCE需要触发关联账户逻辑
- 模板按分类和排序字段在UI中展示

### 2. 目标里程碑管理
- 里程碑是目标的阶段性进度节点
- 每个里程碑包含名称和目标金额
- 里程碑支持排序，按顺序展示进度
- 里程碑达成状态自动更新
- 当里程碑达成时，记录达成时间
- 里程碑随目标删除而自动删除

## 相关文件

1. 实体类
   - `SysGoalTemplate.java` - 目标模板配置表的实体类
   - `GoalMilestone.java` - 目标里程碑表的实体类

2. 数据访问层
   - `SysGoalTemplateRepository.java` - 目标模板配置表的Repository接口
   - `GoalMilestoneRepository.java` - 目标里程碑表的Repository接口

3. 文档
   - `goal_templates_and_milestones.md` - 目标模板与里程碑模块的设计文档

## 注意事项

1. 数据一致性
   - 目标模板的子分类代码是唯一的，确保代码逻辑判断的准确性
   - 里程碑与目标的外键关系确保数据一致性

2. 查询性能
   - 索引设计优化了常用查询场景
   - 分类和排序字段的组合索引提高了UI展示的查询效率

3. 扩展性
   - 模板系统支持灵活扩展新的目标类型
   - 里程碑系统支持任意数量的进度节点

4. 级联操作
   - 当目标被删除时，关联的里程碑自动删除
