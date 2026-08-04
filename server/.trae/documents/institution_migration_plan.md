# 机构和机构类型迁移计划

## 一、需求分析

根据用户需求，需要完成以下迁移任务：

### 1.1 InstType 枚举重构
- 移除 `iconUrl` 字段
- **合并分类**：
  - 银行（BANK）+ 互联网银行（INTERNET_BANK）+ 外资银行（FOREIGN_BANK）→ 合并为 **银行**
  - 金融科技（FINTECH）+ 支付平台（PAYMENT）+ 基金平台（FUND_PLATFORM）→ 合并为 **互金平台**
- **新增分类**：独立账户（INDIVIDUAL）

### 1.2 控制器迁移
- 创建 `InstitutionController.java`，合并机构和机构类型接口，统一对外提供API

### 1.3 初始化数据迁移
- 迁移 `sys_institution_init.json`，按照新的机构类型重新排列
- 初始化方式改为脚本执行，而非服务启动时执行

## 二、文件清单

### 需要创建/修改的文件

| 文件路径 | 操作 | 说明 |
| :--- | :--- | :--- |
| `server/src/main/java/com/suiyou/enums/InstType.java` | 修改 | 重构枚举，移除 iconUrl，合并分类 |
| `server/src/main/java/com/suiyou/model/SysInstitution.java` | 创建 | 机构实体 |
| `server/src/main/java/com/suiyou/model/SysInstitutionType.java` | 创建 | 机构类型实体 |
| `server/src/main/java/com/suiyou/repository/SysInstitutionRepository.java` | 创建 | 机构数据访问层 |
| `server/src/main/java/com/suiyou/repository/SysInstitutionTypeRepository.java` | 创建 | 机构类型数据访问层 |
| `server/src/main/java/com/suiyou/service/SysInstitutionService.java` | 创建 | 机构服务接口 |
| `server/src/main/java/com/suiyou/service/SysInstitutionTypeService.java` | 创建 | 机构类型服务接口 |
| `server/src/main/java/com/suiyou/service/impl/SysInstitutionServiceImpl.java` | 创建 | 机构服务实现 |
| `server/src/main/java/com/suiyou/service/impl/SysInstitutionTypeServiceImpl.java` | 创建 | 机构类型服务实现 |
| `server/src/main/java/com/suiyou/controller/InstitutionController.java` | 创建 | 机构控制器（合并机构类型接口） |
| `server/src/main/java/com/suiyou/dto/account/InstitutionRespDTO.java` | 创建 | 机构响应DTO |
| `server/src/main/java/com/suiyou/dto/account/InstitutionTypeRespDTO.java` | 创建 | 机构类型响应DTO |
| `server/src/main/java/com/suiyou/dto/account/InstitutionInitDTO.java` | 创建 | 机构初始化DTO |
| `server/src/main/java/com/suiyou/dto/account/InstitutionTypeInitDTO.java` | 创建 | 机构类型初始化DTO |
| `server/src/main/java/com/suiyou/dto/account/InstitutionModuleRespDTO.java` | 创建 | 机构模块响应DTO |
| `server/src/main/resources/sys_institution_init.json` | 创建 | 机构初始化数据（按新类型排列） |
| `server/src/main/resources/sys_institution_type_init.json` | 创建 | 机构类型初始化数据 |
| `server/src/main/resources/schema.sql` | 创建 | 数据库初始化脚本 |

## 三、新机构类型定义

| 类型代码 | 类型名称 | 描述 | 排序 | 原类型映射 |
| :--- | :--- | :--- | :--- | :--- |
| BANK | 银行 | 各类银行机构 | 1 | BANK, INTERNET_BANK, FOREIGN_BANK |
| SECURITY | 证券 | 证券公司 | 2 | SECURITY |
| INSURANCE | 保险 | 保险公司 | 3 | INSURANCE |
| FINTECH | 互金平台 | 金融科技、支付、基金平台 | 4 | FINTECH, PAYMENT, FUND_PLATFORM |
| INDIVIDUAL | 独立账户 | 独立账户分类 | 5 | 新增 |

## 四、迁移步骤

### 4.1 第一步：重构 InstType 枚举
- 移除 iconUrl 字段
- 合并银行相关类型
- 合并互金平台相关类型
- 新增独立账户类型

### 4.2 第二步：创建实体类
- 创建 SysInstitution 实体
- 创建 SysInstitutionType 实体（移除 iconUrl 字段）

### 4.3 第三步：创建 Repository
- 创建 SysInstitutionRepository
- 创建 SysInstitutionTypeRepository

### 4.4 第四步：创建 DTO 类
- 创建 InstitutionRespDTO
- 创建 InstitutionTypeRespDTO（移除 iconUrl 字段）
- 创建 InstitutionInitDTO
- 创建 InstitutionTypeInitDTO
- 创建 InstitutionModuleRespDTO

### 4.5 第五步：创建 Service 接口和实现
- 创建 SysInstitutionService 和实现
- 创建 SysInstitutionTypeService 和实现

### 4.6 第六步：创建控制器
- 创建 InstitutionController（合并机构类型接口）

### 4.7 第七步：准备初始化数据
- 创建 sys_institution_init.json（按新类型重新映射）
- 创建 sys_institution_type_init.json
- 创建数据库初始化脚本 schema.sql

### 4.8 第八步：移除启动时初始化逻辑
确保数据初始化通过 SQL 脚本执行，而非服务启动时执行

## 五、风险评估

| 风险点 | 风险等级 | 应对措施 |
| :--- | :--- | :--- |
| 机构类型映射错误 | 高 | 仔细核对每个机构的类型映射 |
| 数据迁移遗漏 | 高 | 对照原JSON文件逐一检查 |
| 接口兼容性问题 | 中 | 保持原有API接口不变 |
| 数据库脚本执行顺序 | 中 | 在脚本中明确依赖顺序 |

## 六、依赖关系

```
控制器 (Controller)
    ↓
服务层 (Service)
    ↓
数据访问层 (Repository)
    ↓
实体类 (Model)
    ↓
数据库表 (Database)
```

---

**计划版本**: v1.0  
**创建时间**: 2026-06-09