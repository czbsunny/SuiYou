# 目标创建流程迁移计划

## 一、项目调研结论

### 1.1 Web 项目源文件分析

| 文件              | 路径                                       | 功能说明                      |
| --------------- | ---------------------------------------- | ------------------------- |
| `guide.vue`     | `web/src/pages/goals/guide.vue`          | 引导页面，展示分类和模板选择流程          |
| `add.vue`       | `web/src/pages/goals/add.vue`            | 表单页面，填写目标详情（金额、名称、图标、日期等） |
| `GoalGuide.vue` | `web/src/components/goals/GoalGuide.vue` | 核心组件，两步滑动选择器（分类→模板）       |

### 1.2 App 项目现状分析

| 文件             | 路径                              | 状态                                 |
| -------------- | ------------------------------- | ---------------------------------- |
| `index.vue`    | `app/src/pages/goal/index.vue`  | 目标列表页，已有 `handleCreate` 方法但跳转路径不存在 |
| `detail.vue`   | `app/src/pages/goal/detail.vue` | 目标详情页                              |
| `saving.vue`   | `app/src/pages/goal/saving.vue` | 存钱计划页                              |
| `useGoalStore` | `app/src/stores/goal.js`        | Pinia 状态管理，包含分类和模板数据获取方法           |
| `goal API`     | `app/src/api/modules/goal.js`   | 后端 API 接口                          |
| `pages.json`   | `app/src/pages.json`            | 页面路由配置                             |

### 1.3 关键差异点

| 对比项      | Web 项目                      | App 项目                     |
| -------- | --------------------------- | -------------------------- |
| 状态管理     | `useConfigStore`            | `useGoalStore`             |
| API 调用方式 | `goalService`               | Pinia action               |
| 样式变量     | `$spacing-sm`, `$primary` 等 | `$spacing-1`, `$primary` 等 |
| 路由路径     | `/pages/goals/guide`        | `/pages/goal/guide`        |

***

## 二、实施步骤

### 2.1 创建 GoalGuide 组件

**文件路径**: `app/src/components/goals/GoalGuide.vue`

**功能**: 实现分类选择→模板选择的两步滑动交互流程

**关键修改**:

* 将 `useConfigStore` 替换为 `useGoalStore`

* 适配 App 项目的样式变量（`$spacing-sm` → `$spacing-3`）

* 移除 web 项目特有的依赖（如 `uni-icons` 组件需检查是否存在）

### 2.2 创建引导页面

**文件路径**: `app/src/pages/goal/guide.vue`

**功能**: 承载 GoalGuide 组件，作为创建流程的第一步

**关键修改**:

* 使用 `useGoalStore` 获取分类和模板数据

* 选择模板后跳转至 `/pages/goal/add?tpl=xxx`

### 2.3 创建表单页面

**文件路径**: `app/src/pages/goal/add.vue`

**功能**: 目标详情填写表单

**关键修改**:

* 将 `goalService.createGoal` 替换为 `useGoalStore().createGoal`

* 适配 App 项目的样式变量

* 修改跳转路径（`/pages/goals/index` → `/pages/goal/index`）

* 移除 `uni-icons` 组件，改用纯 CSS 实现箭头

### 2.4 更新页面路由配置

**文件路径**: `app/src/pages.json`

**修改内容**:

* 在 `pages` 数组中添加 `/pages/goal/guide` 路由

* 在 `pages` 数组中添加 `/pages/goal/add` 路由

### 2.5 更新目标列表页

**文件路径**: `app/src/pages/goal/index.vue`

**修改内容**:

* 更新 `handleCreate` 方法，跳转路径从 `/pages/goal/create` 改为 `/pages/goal/guide`

***

## 三、文件清单

| 操作类型   | 文件路径                                     | 说明        |
| ------ | ---------------------------------------- | --------- |
| **新建** | `app/src/components/goals/GoalGuide.vue` | 两步滑动选择器组件 |
| **新建** | `app/src/pages/goal/guide.vue`           | 引导选择页面    |
| **新建** | `app/src/pages/goal/add.vue`             | 表单填写页面    |
| **修改** | `app/src/pages.json`                     | 添加新路由     |
| **修改** | `app/src/pages/goal/index.vue`           | 更新创建按钮跳转  |

***

## 四、依赖与风险

### 4.1 依赖检查

* [x] `uni-icons` 组件：需确认 App 项目是否安装

* [x] `useGoalStore`：已存在，包含 `fetchCategories` 和 `fetchTemplates` 方法

* [x] `createGoal` API：已存在于 `useGoalStore` 中

* [x] 样式变量：`variables.scss` 已定义，需适配命名差异

### 4.2 风险点

| 风险           | 描述                                         | 应对措施                        |
| ------------ | ------------------------------------------ | --------------------------- |
| 样式变量不一致      | Web 项目使用 `$spacing-sm`，App 使用 `$spacing-3` | 统一使用 App 项目变量               |
| uni-icons 缺失 | Web 项目使用 `uni-icons`，App 项目可能未安装           | 使用纯 CSS 箭头替代                |
| API 响应结构差异   | Web 和 App 的 API 响应格式可能不同                   | 使用 App 项目已有的 `useGoalStore` |
| 图标资源路径       | Web 的图标路径可能与 App 不同                        | 使用 App 项目的静态资源路径            |

***

## 五、验证方案

### 5.1 功能验证

1. 点击目标列表页的「创建」按钮，跳转到引导页面
2. 引导页面显示分类列表，点击分类进入模板选择
3. 选择模板后跳转至表单页面，表单预填模板数据
4. 填写表单后点击「确认开启目标」，创建成功后返回列表页

### 5.2 代码验证

* 运行 `npm run dev:h5` 启动开发服务器

* 检查页面是否正常渲染

* 检查 API 调用是否正常

***

## 六、完成标准

* [ ] `GoalGuide.vue` 组件创建完成

* [ ] `guide.vue` 页面创建完成

* [ ] `add.vue` 页面创建完成

* [ ] `pages.json` 路由配置更新完成

* [ ] `index.vue` 的 `handleCreate` 方法更新完成

* [ ] 开发服务器可正常启动

* [ ] 创建流程可完整执行

