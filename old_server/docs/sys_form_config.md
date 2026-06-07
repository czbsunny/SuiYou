# SysFormConfig 实体类文档

## 1. 概述

`SysFormConfig` 是系统表单配置实体类，用于存储不同业务类型和分类下的表单字段配置信息，支持表单的动态配置和版本管理。

## 2. 类定义

```java
@Data
@Entity
@Table(name = "sys_form_configs")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysFormConfig {
    // 字段定义...
}
```

## 3. 主要注解

- `@Data`: Lombok注解，自动生成getter、setter、toString、equals和hashCode方法
- `@Entity`: JPA注解，标识该类为数据库实体类
- `@Table(name = "sys_form_configs")`: JPA注解，指定对应的数据库表名
- `@JsonIgnoreProperties(ignoreUnknown = true)`: Jackson注解，忽略JSON反序列化时未知的属性

## 4. 字段定义

| 字段名 | 数据类型 | 数据库列名 | 约束 | 描述 |
|-------|---------|-----------|------|------|
| id | Long | id | 主键，自增 | 配置ID |
| bizType | String | biz_type | 非空，长度20 | 业务类型 |
| categoryCode | String | category_code | 非空，长度50 | 分类编码 |
| fieldConfig | String | field_config | 非空，TEXT类型 | 字段配置JSON |
| version | Long | version | 非空 | 版本号 |
| isDeleted | Boolean | is_deleted | 非空，默认0 | 是否删除（0：未删除，1：已删除） |

## 5. 字段详解

### 5.1 id
- **类型**: Long
- **描述**: 主键ID，采用自增策略
- **生成方式**: `@GeneratedValue(strategy = GenerationType.IDENTITY)`

### 5.2 bizType
- **类型**: String
- **长度**: 20
- **约束**: 非空
- **描述**: 业务类型，用于区分不同业务场景下的表单配置
- **示例值**: "asset_account", "portfolio", "financial_goal"

### 5.3 categoryCode
- **类型**: String
- **长度**: 50
- **约束**: 非空
- **描述**: 分类编码，与业务类型组合使用，用于更精细地分类表单配置
- **示例值**: "cash", "fund", "stock"

### 5.4 fieldConfig
- **类型**: String
- **约束**: 非空，TEXT类型
- **描述**: 表单字段配置的JSON字符串，包含字段名称、类型、验证规则等信息
- **示例值**: 
  ```json
  {
    "fields": [
      {
        "name": "accountName",
        "label": "账户名称",
        "type": "text",
        "required": true,
        "maxLength": 50
      },
      {
        "name": "balance",
        "label": "账户余额",
        "type": "number",
        "required": true,
        "min": 0
      }
    ]
  }
  ```

### 5.5 version
- **类型**: Long
- **约束**: 非空
- **描述**: 版本号，用于配置的版本管理，每次更新配置时版本号递增

### 5.6 isDeleted
- **类型**: Boolean
- **约束**: 非空，默认值为false
- **描述**: 软删除标记，0表示未删除，1表示已删除

## 6. 数据库表结构

```sql
CREATE TABLE sys_form_configs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    biz_type VARCHAR(20) NOT NULL,
    category_code VARCHAR(50) NOT NULL,
    field_config TEXT NOT NULL,
    version BIGINT NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
```

## 7. 使用场景

- **动态表单配置**: 根据不同业务类型和分类动态生成表单
- **表单版本管理**: 支持配置的版本控制，方便回溯和切换
- **业务扩展**: 新业务类型或分类可通过添加配置实现，无需修改代码

## 8. 注意事项

1. `fieldConfig` 字段存储JSON格式的配置信息，建议在业务层进行JSON的序列化和反序列化处理
2. 版本号 `version` 在每次更新配置时需手动递增，确保版本的正确性
3. 使用软删除机制，删除配置时仅更新 `is_deleted` 字段，不直接删除数据
4. `bizType` 和 `categoryCode` 组合使用，建议在业务层添加唯一约束以避免重复配置