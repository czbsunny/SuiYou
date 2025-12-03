# 历史汇率表 (sys_exchange_rates)

## 模块概述
历史汇率表用于存储不同货币间的历史汇率数据，支持多货币间的汇率转换和历史汇率查询功能。

## 数据结构

### 表结构
```sql
CREATE TABLE `sys_exchange_rates` (
  `date` date NOT NULL,
  `source_currency` varchar(10) NOT NULL, -- e.g. USD
  `target_currency` varchar(10) NOT NULL, -- e.g. CNY
  `rate` decimal(19,6) NOT NULL,          -- e.g. 7.235600
  PRIMARY KEY (`date`, `source_currency`, `target_currency`)
);
```

### 核心字段说明

| 字段名 | 数据类型 | 描述 | 示例值 |
|-------|---------|------|-------|
| `date` | date | 汇率日期 | 2023-10-15 |
| `source_currency` | varchar(10) | 源货币代码 | USD |
| `target_currency` | varchar(10) | 目标货币代码 | CNY |
| `rate` | decimal(19,6) | 汇率值 | 7.235600 |

### 复合主键
表使用 `(date, source_currency, target_currency)` 作为复合主键，确保每个日期、每种货币对的汇率唯一。

## 业务逻辑

### 汇率数据存储
- 每个汇率记录代表特定日期下某一货币对的转换汇率
- 汇率值采用 `decimal(19,6)` 类型存储，保留6位小数，确保汇率精度

### 汇率查询
- 支持按日期和货币对查询特定汇率
- 支持按货币对查询历史汇率序列
- 支持按日期查询特定日期的所有汇率

### 应用场景
- 多货币资产估值转换
- 历史资产价值回溯计算
- 跨货币交易记录转换
- 财务报表货币统一

## 相关文件

### 实体类
- `SysExchangeRate.java` - 汇率表实体类，使用复合主键
- `SysExchangeRateId.java` - 复合主键类

### 数据访问层
- `SysExchangeRateRepository.java` - 汇率数据访问接口

## 注意事项

1. **货币代码规范**：使用ISO 4217标准货币代码（如USD、CNY、EUR等）
2. **汇率精度**：汇率值存储6位小数，转换时根据业务需求进行精度调整
3. **汇率方向**：明确记录源货币和目标货币，避免汇率方向混淆
4. **数据同步**：建议定期从可靠的汇率数据源同步汇率数据
5. **历史数据管理**：根据业务需求设置汇率历史数据的保留期限

## 扩展建议

1. **实时汇率支持**：可考虑添加实时汇率字段或单独表存储
2. **汇率来源**：可增加汇率数据来源字段，记录数据获取渠道
3. **汇率类型**：可添加汇率类型字段（如中间价、买入价、卖出价）
4. **时区处理**：考虑添加时区信息，确保汇率日期的准确性
