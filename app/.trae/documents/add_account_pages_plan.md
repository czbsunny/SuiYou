
# 添加账户页面实施计划

## 需求概述
实现两个添加账户的页面：
1. 搜索机构页面：用于选择金融机构
2. 添加账户页面：填写账户信息并配置资产模块

## 设计文件
- `search_inst.html`：机构搜索页面原型
- `add_account.html`：添加账户页面原型
- `search_inst.png`：搜索页面设计图
- `add_account.png`：添加账户页面设计图

## 现有结构分析
- 使用 Vue 3 + uni-app 框架
- 页面配置在 `pages.json`
- 使用 Scss 样式，变量定义在 `@/styles/variables.scss`
- 组件库风格：Material Design 3
- API 模块在 `api/modules/` 目录
- 组件按功能分类在 `components/` 目录

## 目录组织优化
将相关文件组织在同一个功能模块下：
- 页面：`pages/accounts/`
- 组件：`components/accounts/`
- API：`api/modules/asset.js`（已有资产模块，扩展）

## 实施步骤

### 1. 创建目录结构
- `pages/accounts/`：放置账户相关页面
- `components/accounts/`：放置账户相关组件

### 2. 创建页面组件
创建两个新页面：
- `pages/accounts/search-institution.vue`：机构搜索页面
- `pages/accounts/add-account.vue`：添加账户页面

### 3. 更新 pages.json 配置
在 pages.json 中添加两个新页面的配置

### 4. 创建组件（可选但推荐）
在 `components/accounts/` 目录下创建：
- `InstitutionCard.vue`：机构卡片组件
- `AssetModuleItem.vue`：资产模块项组件

### 5. 扩展 API 模块
在 `api/modules/asset.js` 中添加新的 API 接口
- 获取机构类型列表
- 获取机构列表
- 获取机构资产模块配置

### 6. 创建搜索机构页面（search-institution）
功能需求：
- 搜索框：搜索机构或平台
- 机构类型 Tab 栏：热门、银行、支付、证券、基金平台、保险、其他
- 机构列表：展示常用金融机构，支持点击选择
- 手动添加选项：未在列表中的机构

API 接口：
- `GET /institution-types`：获取机构类型列表
- `GET /institutions`：获取所有机构
- `GET /institutions/type/{typeCode}`：按类型获取机构
- `GET /institutions/hot`：获取热门机构

### 7. 创建添加账户页面（add-account）
功能需求：
- 机构信息展示：显示当前选择的机构，支持修改
- 账户信息填写：账户标识、账户名称、账户用途、可见范围、计入净值
- 资产模块配置：
  - 必选模块（不可取消）
  - 默认模块（默认选中）
  - 可选模块（用户选择）
- 确认添加按钮

API 接口：
- `GET /institutions/{instCode}/categories`：获取机构的资产模块配置

### 8. 修改资产页面（assets/index.vue）
- 更新 + 按钮，点击跳转到搜索机构页面

### 9. 实现样式
- 按照设计文件实现 UI
- 使用现有的变量和风格

## 文件清单
1. `pages.json`：添加新页面配置
2. `pages/accounts/search-institution.vue`：机构搜索页面
3. `pages/accounts/add-account.vue`：添加账户页面
4. `pages/assets/index.vue`：修改 + 按钮跳转
5. `components/accounts/InstitutionCard.vue`：机构卡片组件
6. `components/accounts/AssetModuleItem.vue`：资产模块项组件
7. `api/modules/asset.js`：扩展 API 接口

## 技术要点
- 使用 uni-app 路由跳转
- 实现 Tab 切换效果
- 处理 API 请求和状态管理
- 表单验证（账户名称等必填项）
- 资产模块的选择状态管理
