
# 机构配置重构计划

## 概述
重构机构配置系统，引入机构类型表，并提供给前端访问接口。

## 分析现状

### 当前实现
- **SysInstitution**: 包含 instType 字段作为字符串，但没有关联表
- **sys_institution_init.json**: 包含完整的机构数据
- **SysAssetConfigService**: 提供机构数据管理
- **现有问题**: 机构类型没有标准化管理

### 数据结构分析
从 sys_institution_init.json 中，我们可以看到机构类型有：
- BANK: 银行
- INTERNET_BANK: 互联网银行
- FOREIGN_BANK: 外资银行
- SECURITY: 证券公司
- INSURANCE: 保险公司
- PAYMENT: 支付平台
- FINTECH: 金融科技
- FUND_PLATFORM: 基金平台

### 业务场景
前端需要：
1. 展示机构类型的 Tab（如：银行、证券、保险等）
2. 点击不同的 Tab 可以获取对应的机构列表
3. 选择具体的机构来创建账户

## 重构方案

### 1. 新增机构类型表
- **表名**: sys_institution_type
- **实体**: SysInstitutionType
- **字段**:
  - id: Long (主键)
  - typeCode: String (类型代码，如 BANK)
  - typeName: String (类型名称，如 银行)
  - description: String (描述)
  - sortOrder: Integer (排序)
  - iconUrl: String (图标URL)
  - themeColor: String (主题色)

### 2. 调整机构表
- **实体**: SysInstitution (保留现有)
- **新增关联**:
  - 与 SysInstitutionType 的 @ManyToOne 关联
  - 保留 instType 字段（向后兼容）

### 3. 新增 DTO
- InstitutionTypeInitDTO: 机构类型初始化数据
- InstitutionTypeRespDTO: 机构类型响应
- 扩展 InstitutionRespDTO: 添加机构类型信息

### 4. 更新 Repository
- SysInstitutionTypeRepository: 机构类型数据访问
- 更新 SysInstitutionRepository: 添加按类型查询等方法

### 5. 更新 Service
- 更新 SysAssetConfigService: 添加机构类型管理方法
- 创建 SysInstitutionTypeService

### 6. 更新 Controller
- 创建 InstitutionTypeController: 机构类型API
- 创建 InstitutionController: 机构API（独立）

### 7. 更新数据加载器
- 创建 InstitutionTypeDataLoader: 加载机构类型数据
- 更新 InstitutionDataLoader: 加载机构时关联机构类型

### 8. 创建初始化数据文件
- sys_institution_type_init.json: 机构类型初始化数据

## 实施步骤

### 第一阶段: 数据模型
1. 创建 SysInstitutionType 实体
2. 更新 SysInstitution 实体，添加机构类型关联

### 第二阶段: 数据访问层
1. 创建 SysInstitutionTypeRepository
2. 更新 SysInstitutionRepository

### 第三阶段: DTO层
1. 创建相关的 DTO 类
2. 更新现有 DTO 类

### 第四阶段: Service层
1. 创建机构类型 Service
2. 更新现有 Service
3. 添加前端需要的业务逻辑

### 第五阶段: Controller层
1. 创建新的 API 接口

### 第六阶段: 数据加载层
1. 创建机构类型数据加载器
2. 更新机构数据加载器
3. 创建初始化数据 JSON 文件

### 第七阶段: 测试验证
1. 编译测试
2. 功能测试
3. 前后端联调

## 前端接口设计

### 机构类型相关
- GET /api/institution-types: 获取所有机构类型（用于 Tab 展示）
- GET /api/institution-types/{code}: 获取单个机构类型详情

### 机构相关
- GET /api/institutions: 获取所有机构
- GET /api/institutions/type/{typeCode}: 按类型获取机构列表（用于 Tab 切换）
- GET /api/institutions/hot: 获取热门机构
- GET /api/institutions/{code}: 获取单个机构详情

## 数据结构示例

### 机构类型数据
```json
[
  {
    "typeCode": "BANK",
    "typeName": "银行",
    "description": "各类银行机构",
    "sortOrder": 1,
    "iconUrl": "/icons/bank.png",
    "themeColor": "#1890FF"
  }
]
```

## 风险与注意事项
1. 向后兼容：保留现有数据结构和API
2. 数据迁移：现有数据需要正确关联到新的机构类型
3. 性能：大数据量情况下的查询优化

## 预期成果
- 标准化的机构类型管理
- 完整的前后端接口支持 Tab 切换
- 可扩展的架构
- 向后兼容性
