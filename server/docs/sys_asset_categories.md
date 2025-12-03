# 资产分类配置表

## 模块概述
资产分类配置表（sys_asset_categories）用于定义系统中所有资产和负债的分类体系，为账户管理提供标准化的分类支持。该表采用代码（code）作为核心锚点，支持层级结构，并预设了系统默认分类。

## 数据结构

### 表结构
```sql
CREATE TABLE `sys_asset_categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '分类代码 (如: LIQUID, BANK_CARD)',
  `parent_code` varchar(50) DEFAULT NULL COMMENT '父级代码 (一级分类此项为 NULL)',
  `name` varchar(50) NOT NULL COMMENT '显示名称 (如: 银行卡)',
  `icon_url` varchar(255) DEFAULT NULL COMMENT '图标链接/Token',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序权重',
  `color` varchar(20) DEFAULT NULL COMMENT '展示颜色 (Hex)',
  `group_type` varchar(20) NOT NULL COMMENT 'ASSET/LIABILITY (冗余字段，方便一级查询)',
  `is_system` tinyint(1) DEFAULT '1' COMMENT '是否系统预设 (1=是, 0=用户自定义)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent` (`parent_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产分类配置表';
```

### 核心字段说明

| 字段名 | 类型 | 描述 | 用途 |
| --- | --- | --- | --- |
| `code` | varchar(50) | 分类代码 | 核心锚点，对应Java Enum的.name()，用于代码中的switch case判断 |
| `parent_code` | varchar(50) | 父级代码 | 构建层级结构，比parent_id更具可读性，便于数据迁移 |
| `group_type` | varchar(20) | 分组类型 | 区分资产(ASSET)和负债(LIABILITY)，便于快速查询 |
| `is_system` | tinyint(1) | 是否系统预设 | 标记系统默认分类，1表示系统预设，0表示用户自定义 |

## 业务逻辑

### 分类体系
- **一级分类**：如流动资产(LIQUID)、负债(DEBT)等，parent_code为NULL
- **二级分类**：如银行卡(BANK_CARD)、信用卡(CREDIT_CARD)等，parent_code指向一级分类

### 代码实现
- 使用`AssetCategoryEnum`枚举类定义所有分类代码，确保代码一致性
- 系统预设分类的is_system字段为1，不允许用户修改
- 支持通过code快速查找分类信息，便于账户处理

### 与账户的关系
账户表(accounts)中的`category`和`subCategory`字段应对应资产分类配置表中的`code`字段，实现标准化分类管理。

## 相关文件

1. **实体类**：`com.suiyou.model.SysAssetCategories`
   - 对应sys_asset_categories表的实体类
   - 使用JPA注解定义表结构和字段映射

2. **数据访问层**：`com.suiyou.repository.SysAssetCategoriesRepository`
   - 提供分类数据的CRUD操作
   - 支持按code、parent_code、group_type查询

3. **枚举类**：`com.suiyou.model.AssetCategoryEnum`
   - 定义所有资产分类代码
   - 包含LIQUID(流动资产)、BANK_CARD(银行卡)、DEBT(负债)等枚举值

4. **业务服务**：
   - 可在`AccountService`等服务中使用资产分类配置表进行账户分类管理

## 注意事项

1. 目前暂不提供Controller接口查询资产分类，仅用于后端账户处理
2. 分类代码(code)是核心字段，需确保与Java Enum的name()保持一致
3. 系统预设分类(is_system=1)不允许删除或修改，避免影响现有账户数据
4. 新增分类时，需同时更新AssetCategoryEnum枚举类
