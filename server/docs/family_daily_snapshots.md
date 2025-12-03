# 家庭每日资产快照模块设计文档

## 模块概述
家庭每日资产快照模块用于记录家庭每日的资产和负债情况，提供净值计算和历史趋势分析的基础数据。

## 数据结构

### 1. 家庭每日资产快照表 (family_daily_snapshots)

#### 表结构
```sql
CREATE TABLE `family_daily_snapshots` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL COMMENT '归属家庭',
  `date` date NOT NULL COMMENT '快照日期',
  
  -- 【基准信息】
  `base_currency` varchar(10) NOT NULL DEFAULT 'CNY' COMMENT '本位币',

  -- 【资产端 (Assets) - 存正数】
  `asset_liquid` decimal(19,4) NOT NULL DEFAULT 0 COMMENT '活钱',
  `asset_invest` decimal(19,4) NOT NULL DEFAULT 0 COMMENT '投资',
  `asset_fixed`  decimal(19,4) NOT NULL DEFAULT 0 COMMENT '固产',
  `asset_other`  decimal(19,4) NOT NULL DEFAULT 0 COMMENT '其他',

  -- 【负债端 (Liabilities) - 建议存负数】
  -- 存负数的好处：计算净值直接 SUM 所有字段，不用做减法，代码逻辑统一
  `liabilities`    decimal(19,4) NOT NULL DEFAULT 0 COMMENT '负债',
    
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_date` (`family_id`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭每日资产快照表';
```

#### 核心字段说明

| 字段名 | 类型 | 说明 | 核心功能 |
|-------|------|------|---------|
| `family_id` | bigint | 归属家庭ID | 确定快照所属的家庭范围 |
| `date` | date | 快照日期 | 记录快照的日期，用于时间维度查询 |
| `base_currency` | varchar(10) | 本位币 | 快照的货币单位，默认为CNY |
| `asset_liquid` | decimal(19,4) | 活钱 | 流动资产，如现金、活期存款等 |
| `asset_invest` | decimal(19,4) | 投资 | 投资资产，如股票、基金、债券等 |
| `asset_fixed` | decimal(19,4) | 固产 | 固定资产，如房产、车辆等 |
| `asset_other` | decimal(19,4) | 其他 | 其他资产，如收藏品、知识产权等 |
| `liabilities` | decimal(19,4) | 负债 | 家庭负债，建议存负数，方便净值计算 |

## 业务逻辑

### 1. 快照生成逻辑
- 每日定时生成家庭资产快照
- 确保每个家庭每天只有一条快照记录（通过唯一约束保证）
- 快照数据来源：家庭下所有账户的资产负债统计

### 2. 资产分类
- **活钱**：流动性强，可随时使用的资金
- **投资**：用于投资增值的资产
- **固产**：价值相对稳定的固定资产
- **其他**：不属于上述三类的其他资产

### 3. 净值计算
- 净值 = 资产总和 + 负债（因为负债存负数）
- 资产总和 = 活钱 + 投资 + 固产 + 其他
- 代码实现：`getNetWorth()` 方法直接 SUM 所有字段

### 4. 查询逻辑
- 支持按家庭ID和日期查询特定快照
- 支持按日期范围查询快照历史
- 支持计算净值变化趋势

## 相关文件

1. 实体类
   - `FamilyDailySnapshot.java` - 家庭每日资产快照表的实体类

2. 数据访问层
   - `FamilyDailySnapshotRepository.java` - 家庭每日资产快照表的Repository接口

3. 文档
   - `family_daily_snapshots.md` - 家庭每日资产快照模块的设计文档

## 注意事项

1. 数据一致性
   - 唯一约束确保每个家庭每天只有一条快照
   - 定时任务生成快照时需处理并发情况

2. 性能优化
   - 按家庭ID和日期的联合索引提高查询效率
   - 只存储必要的统计数据，避免冗余

3. 计算逻辑
   - 负债存负数，简化净值计算逻辑
   - 提供计算字段，方便业务层使用

4. 扩展性
   - 支持多种本位币，便于国际化
   - 资产分类可根据业务需求扩展
