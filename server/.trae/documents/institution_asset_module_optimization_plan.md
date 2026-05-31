
# 机构资产类型模块优化计划

## 需求概述
优化机构资产类型模块的配置方式，支持根据机构类型和具体机构配置资产模块，分为必选、默认选、可选三类。

## 分析现状
### 当前实现
- `SysCategoryInstitutionRelation`：机构与分类关联表，仅记录关联关系
- `RelationRuleConfigDTO`：配置类，包含 defaultRules、categoryRules、specialCases
- `sys_relation_rules.json`：配置文件，定义机构与分类的关系

### 当前问题
- 仅记录是否有关联关系，没有区分关联的类型（必选/默认选/可选）
- 前端无法判断哪些模块是必选的，哪些是默认选中的，哪些是可选的

## 优化方案

### 1. 数据模型优化
#### 更新 SysCategoryInstitutionRelation 实体
新增 `selectionType` 字段，表示选择类型：
- `REQUIRED`：必选
- `DEFAULT_SELECTED`：默认选
- `OPTIONAL`：可选（默认值）

### 2. 配置文件优化（sys_relation_rules.json）
重构配置结构，支持按类型配置：

```json
{
  "typeRules": {
    "BANK": {
      "required": ["DEBIT_CARD", "CASH_PLUS"],
      "defaultSelected": ["TIME_DEPOSIT", "BANK_PRODUCT"],
      "optional": ["CREDIT_CARD", "MORTGAGE"]
    },
    "INTERNET_BANK": {
      "required": ["DEBIT_CARD"],
      "defaultSelected": ["TIME_DEPOSIT"],
      "optional": ["BANK_PRODUCT", "CONSUMER_LOAN"]
    }
  },
  "instRules": {
    "ICBC": {
      "required": ["DEBIT_CARD", "CASH_PLUS"],
      "defaultSelected": ["TIME_DEPOSIT", "BANK_PRODUCT"],
      "optional": ["CREDIT_CARD", "MORTGAGE", "HOUSING_FUND"]
    },
    "ALIPAY": {
      "required": ["BALANCE", "YUEBAO"],
      "defaultSelected": ["XIAOHEBAO"],
      "optional": ["FUND", "GOLD_ACCUMULATION"]
    }
  }
}
```

### 3. DTO 更新
#### 更新 RelationRuleConfigDTO
- 重构为新的结构，支持 typeRules 和 instRules
- 新增 SelectionType 枚举（REQUIRED, DEFAULT_SELECTED, OPTIONAL）

#### 更新响应 DTO
- `CategoryInstitutionRelationRespDTO` 新增 selectionType 字段

### 4. 数据访问层更新
#### 更新 Repository
- 新增查询方法，支持按机构代码获取带 selectionType 的分类列表
- 新增查询方法，支持按机构类型和 selectionType 查询

### 5. Service 层更新
#### 更新 SysAssetConfigService
- 更新 `initCategoryInstitutionRelations` 方法，处理新的配置结构
- 新增方法：`getInstitutionCategoriesWithType(String instCode)`，获取机构的所有分类及类型
- 新增方法：`getInstitutionTypeCategoriesWithType(String typeCode)`，获取机构类型的所有分类及类型

### 6. Controller 层更新
#### 新增/更新接口
- `GET /institutions/{instCode}/categories`：获取机构的所有分类及选择类型
- `GET /institution-types/{typeCode}/categories`：获取机构类型的所有分类及选择类型

### 7. 数据加载器更新
#### 更新 RelationDataLoader
- 适配新的配置格式

## 文件变更清单
1. `SysCategoryInstitutionRelation.java`：新增 selectionType 字段
2. `RelationRuleConfigDTO.java`：重构配置结构
3. `CategoryInstitutionRelationRespDTO.java`：新增 selectionType 字段
4. `SysCategoryInstitutionRelationRepository.java`：新增查询方法
5. `SysAssetConfigService.java`：新增接口方法
6. `SysAssetConfigServiceImpl.java`：实现新方法
7. `InstitutionController.java`：新增接口
8. `sys_relation_rules.json`：更新为新格式
9. `RelationDataLoader.java`：适配新配置

## 实施步骤
1. 更新数据模型（实体 + Repository）
2. 更新 DTO 层
3. 更新配置文件
4. 更新 Service 层
5. 更新 Controller 层
6. 更新数据加载器
7. 编译测试
