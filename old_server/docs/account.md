# 账户模块 (Account Module)

## 1. 模块概述

账户模块用于管理用户的资产和负债账户信息。支持多种账户类型和分类体系，提供灵活的配置选项，帮助用户全面管理个人和家庭财务。

## 2. 数据结构

### 2.1 账户表 (accounts)

| 字段名 | 数据类型 | 约束 | 描述 |
| ------ | -------- | ---- | ---- |
| id | bigint | PRIMARY KEY, AUTO_INCREMENT | 账户ID |
| familyId | bigint | NOT NULL | 当前归属家庭ID |
| ownerId | bigint | NOT NULL | 资产所有者ID |
| visibleScope | varchar(20) | NOT NULL, DEFAULT 'PRIVATE' | 可见范围：PRIVATE/FAMILY |
| groupType | varchar(20) | NOT NULL | 一级分类：ASSET（资产）/ LIABILITY（负债） |
| category | varchar(30) | NOT NULL | 二级分类：LIQUID（流动资产）, INVEST（投资资产）, FIXED（固定资产）, CREDIT（信用负债）, LOAN（贷款负债） |
| subCategory | varchar(30) | NOT NULL | 三级分类：BANK_CARD（银行卡）, E_WALLET（电子钱包）, STOCK（股票）, FUND（基金）等 |
| institution | varchar(50) | DEFAULT NULL | 机构代码：CMB（招商银行）, ALIPAY（支付宝）, WECHAT（微信支付）等（用于显示Logo） |
| name | varchar(50) | NOT NULL | 自定义账户名称 |
| balance | decimal(19,4) | NOT NULL, DEFAULT '0.0000' | 账户余额 |
| currency | varchar(10) | NOT NULL, DEFAULT 'CNY' | 货币类型 |
| includeInNetWorth | tinyint(1) | NOT NULL, DEFAULT 1 | 是否计入净资产 |
| status | tinyint(1) | NOT NULL, DEFAULT 1 | 账户状态：1=启用, 0=停用 |
| attributes | json | DEFAULT NULL | 扩展属性（JSON格式） |
| createdAt | datetime | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updatedAt | datetime | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 2.2 索引设计

- `idx_family_scope`：按家庭和可见范围查询账户
- `idx_owner`：按所有者查询账户
- `idx_category_stat`：按家庭和分类统计账户（用于快速计算各类资产负债）

## 3. API接口

### 3.1 创建账户

**接口地址**：`POST /accounts`

**请求体**：
```json
{
  "familyId": 1,
  "ownerId": 1,
  "visibleScope": "PRIVATE",
  "groupType": "ASSET",
  "category": "LIQUID",
  "subCategory": "BANK_CARD",
  "institution": "CMB",
  "name": "招商银行储蓄卡",
  "balance": 10000.00,
  "currency": "CNY",
  "includeInNetWorth": 1,
  "status": 1,
  "attributes": {"cardNumber": "****1234"}
}
```

**响应**：
```json
{
  "id": 1,
  "familyId": 1,
  "ownerId": 1,
  "visibleScope": "PRIVATE",
  "groupType": "ASSET",
  "category": "LIQUID",
  "subCategory": "BANK_CARD",
  "institution": "CMB",
  "name": "招商银行储蓄卡",
  "balance": 10000.0000,
  "currency": "CNY",
  "includeInNetWorth": 1,
  "status": 1,
  "attributes": {"cardNumber": "****1234"},
  "createdAt": "2025-12-03T11:36:39",
  "updatedAt": "2025-12-03T11:36:39"
}
```

## 4. 业务逻辑

### 4.1 账户分类体系

账户采用三级分类体系：

1. **一级分类（groupType）**：
   - ASSET：资产
   - LIABILITY：负债

2. **二级分类（category）**：
   - 资产类：LIQUID（流动资产）, INVEST（投资资产）, FIXED（固定资产）
   - 负债类：CREDIT（信用负债）, LOAN（贷款负债）

3. **三级分类（subCategory）**：
   - 流动资产：BANK_CARD（银行卡）, E_WALLET（电子钱包）, CASH（现金）等
   - 投资资产：STOCK（股票）, FUND（基金）, REAL_ESTATE（房产投资）等
   - 固定资产：HOUSE（自住房产）, CAR（车辆）等
   - 信用负债：CREDIT_CARD（信用卡）等
   - 贷款负债：MORTGAGE（房贷）, CAR_LOAN（车贷）, PERSONAL_LOAN（个人贷款）等

### 4.2 可见范围控制

- **PRIVATE**：仅账户所有者可见
- **FAMILY**：家庭所有成员可见

### 4.3 机构代码说明

机构代码用于在UI中显示对应的机构Logo，支持以下常见机构：
- CMB：招商银行
- ICBC：工商银行
- ABC：农业银行
- BOC：中国银行
- ALIPAY：支付宝
- WECHAT：微信支付
- JD_PAY：京东支付

## 5. 相关文件

| 文件路径 | 描述 |
| ------ | ---- |
| `src/main/java/com/suiyou/model/Account.java` | 账户实体类 |
| `src/main/java/com/suiyou/repository/AccountRepository.java` | 账户数据访问接口 |
| `src/main/java/com/suiyou/service/AccountService.java` | 账户业务逻辑接口 |
| `src/main/java/com/suiyou/service/impl/AccountServiceImpl.java` | 账户业务逻辑实现 |
| `src/main/java/com/suiyou/controller/AccountController.java` | 账户控制器 |
