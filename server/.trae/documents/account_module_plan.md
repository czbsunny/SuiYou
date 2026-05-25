# 账户资产模块功能实现计划

## 需求分析

根据用户需求，当前账户缺少资产模块的概念。需要实现：
1. **资产类型枚举**（AssetType）：如 FIXED_DEPOSIT（定期存款）、FUND（基金）等
2. **账户资产模块**（AccountModule）：关联账户和资产类型，一个账户可以包含多个资产模块
3. **模块管理接口**：通过账户Controller添加或移除账户资产模块

数据结构示意：
```
Account 
   -> AccountModule(assetType = FIXED_DEPOSIT) 
       -> Asset: 3个月定存 
       -> Asset: 1年期定存 

Account 
   -> AccountModule(assetType = FUND) 
       -> Asset: 易方达某基金 
       -> Asset: 广发某基金
```

## 技术方案

### 1. 创建资产类型枚举

文件：`model/enums/AssetType.java`

包含以下资产类型：
- CASH：现金/活期存款
- FIXED_DEPOSIT：定期存款
- FUND：基金
- STOCK：股票
- BOND：债券
- INSURANCE：保险
- REAL_ESTATE：房产

### 2. 创建 AccountModule 实体

文件：`model/AccountModule.java`

字段设计：
- id：主键
- account：关联账户（ManyToOne）
- assetType：资产类型（枚举）
- status：状态（0:归档, 1:活跃）
- createdAt/updatedAt：时间戳

### 3. 创建 Repository

文件：`repository/AccountModuleRepository.java`

### 4. 创建 Service 层

文件：`service/AccountModuleService.java` 和 `service/impl/AccountModuleServiceImpl.java`

### 5. 创建 DTO

文件：`dto/account/AccountModuleDTO.java`
文件：`dto/account/AccountModuleRespDTO.java`

### 6. 更新 AccountController

添加以下接口：
- POST /accounts/{accountId}/modules：添加资产模块
- DELETE /accounts/{accountId}/modules/{moduleId}：移除资产模块
- GET /accounts/{accountId}/modules：获取账户的资产模块列表

## 文件清单

| 文件路径 | 类型 | 说明 |
| :--- | :--- | :--- |
| `model/enums/AssetType.java` | 新增 | 资产类型枚举 |
| `model/AccountModule.java` | 新增 | 账户资产模块实体 |
| `repository/AccountModuleRepository.java` | 新增 | 账户模块数据访问层 |
| `service/AccountModuleService.java` | 新增 | 账户模块服务接口 |
| `service/impl/AccountModuleServiceImpl.java` | 新增 | 账户模块服务实现 |
| `dto/account/AccountModuleDTO.java` | 新增 | 创建/更新模块请求DTO |
| `dto/account/AccountModuleRespDTO.java` | 新增 | 模块响应DTO |
| `controller/AccountController.java` | 修改 | 添加模块管理接口 |

## 数据库表设计

```sql
CREATE TABLE account_module (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    asset_type VARCHAR(20) NOT NULL,
    status TINYINT(1) DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE
);
```

## 接口设计

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| POST | /accounts/{accountId}/modules | 为账户添加资产模块 |
| DELETE | /accounts/{accountId}/modules/{moduleId} | 删除账户的资产模块 |
| GET | /accounts/{accountId}/modules | 获取账户的资产模块列表 |

## 依赖与风险

### 依赖
- 现有的 Account 实体和相关服务
- Spring Data JPA

### 风险
- 确保 AccountModule 与 Account 的级联删除正确处理
- 资产模块删除时需考虑关联的 Asset 数据处理策略（保留但标记为归档）

## 执行步骤

1. 创建资产类型枚举
2. 创建 AccountModule 实体
3. 创建 Repository
4. 创建 Service 接口和实现
5. 创建 DTO 类
6. 更新 AccountController 添加模块管理接口
7. 测试验证