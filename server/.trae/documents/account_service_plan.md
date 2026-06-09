# Account服务实现计划

## 需求分析

根据用户请求，需要实现account服务，包含以下功能：

1. **获取账户列表** - 获取当前用户的账户列表
2. **创建账户** - 创建账户，同时创建account和accountModule两个表的数据

创建账户请求示例：

```json
{
    "instCode": "ALIPAY",
    "accountNo": "3342",
    "accountType": "FINTECH",
    "accountName": "工资卡",
    "includeInNetWorth": true,
    "modules": [
        {"moduleType": "CURRENT", "moduleName": "余额"},
        {"moduleType": "CURRENT_PLUS", "moduleName": "余额宝"},
        {"moduleType": "FUND", "moduleName": "基金"},
        {"moduleType": "GOLD", "moduleName": "黄金"}
    ]
}
```

## 现有代码结构分析

### 核心模型

* **Account** (`model/Account.java`): 账户主表，包含familyId, ownerId, instCode, accountNo, accountType, accountName, includeInNetWorth等字段

* **AccountModule** (`model/AccountModule.java`): 账户模块表，包含accountId, moduleType, moduleName, iconUrl等字段

### Repository接口

* **AccountRepository**: 已存在基本CRUD方法

* **AccountModuleRepository**: 已存在基本CRUD方法

### 现有服务模式

参考`SysInstitutionService`和`SysInstitutionServiceImpl`的模式实现

## 实现计划

### 1. 创建DTO类

**CreateAccountDTO** - 创建账户请求DTO

* instCode: String (必填)

* accountNo: String (必填)

* accountType: String (必填)

* accountName: String (可选)

* includeInNetWorth: Boolean (可选，默认true)

* modules: List<AccountModuleDTO> (可选)

**AccountModuleDTO** - 模块请求DTO

* moduleType: String (必填)

* moduleName: String (必填)

**AccountRespDTO** - 账户响应DTO

* id: Long

* instCode: String

* accountNo: String

* accountType: String

* accountName: String

* includeInNetWorth: Boolean

* modules: List<AccountModuleRespDTO>

* createdAt: LocalDateTime

**AccountModuleRespDTO** - 模块响应DTO

* id: String

* moduleType: String

* moduleName: String

* iconUrl: String

* canPay: Integer

* sortOrder: Integer

### 2. 创建Service接口

**AccountService** (`service/AccountService.java`)

* `List<AccountRespDTO> getAccountsByOwnerId(Long ownerId)` - 获取用户账户列表

* `AccountRespDTO createAccount(CreateAccountDTO dto)` - 创建账户

### 3. 创建Service实现类

**AccountServiceImpl** (`service/impl/AccountServiceImpl.java`)

* 注入AccountRepository和AccountModuleRepository

* 实现getAccountsByOwnerId方法

* 实现createAccount方法（事务管理，同时保存Account和AccountModule）

### 4. 创建Controller

**AccountController** (`controller/AccountController.java`)

* GET `/api/accounts` - 获取当前用户账户列表

* POST `/api/accounts` - 创建账户

## 文件清单

| 文件路径                                    | 类型  | 操作 |
| :-------------------------------------- | :-- | :- |
| `dto/account/CreateAccountDTO.java`     | DTO | 新建 |
| `dto/account/AccountModuleDTO.java`     | DTO | 新建 |
| `dto/account/AccountRespDTO.java`       | DTO | 新建 |
| `dto/account/AccountModuleRespDTO.java` | DTO | 新建 |
| `service/AccountService.java`           | 接口  | 新建 |
| `service/impl/AccountServiceImpl.java`  | 实现  | 新建 |
| `controller/AccountController.java`     | 控制器 | 新建 |

## 注意事项

1. **事务管理**: 创建账户时需要同时保存Account和AccountModule，需使用@Transactional注解
2. **用户认证**: 使用SecurityUtils.getCurrentUserId()获取当前登录用户ID
3. **模块图标**: 根据ModuleType枚举获取默认图标URL
4. **ID生成**: AccountModule的id需要手动生成UUID

## 依赖检查

* Spring Data JPA已配置

* Lombok已配置

* SecurityUtils已存在用于获取当前用户

***

## 执行步骤

1. 创建DTO类（4个文件）
2. 创建AccountService接口
3. 创建AccountServiceImpl实现类
4. 创建AccountController控制器
5. 验证编译通过

