# 重构计划：移除非 index 页面

## 项目现状分析

### 当前 pages.json 配置
项目目前只配置了4个 tabBar 页面，均为 index 页面：
- `pages/home/index` - 首页
- `pages/goals/index` - 目标页
- `pages/assets/index` - 资产页
- `pages/profile/index` - 个人中心

### 存在的非 index 页面文件
以下页面文件存在于项目中，但未在 pages.json 中注册：
- `pages/home/record.vue` - 记账页面
- `pages/home/transfer.vue` - 转账页面
- `pages/assets/add.vue` - 添加资产页面
- `pages/assets/category.vue` - 资产分类页面
- `pages/goals/add.vue` - 添加目标页面
- `pages/goals/detail.vue` - 目标详情页面
- `pages/profile/edit.vue` - 编辑个人信息页面

### 引用关系分析
- `components/home/QuickActions.vue` 中有引用 `pages/home/record` 的代码
- stores 和 api 中的导航代码只引用了 index 页面
- index 页面中的按钮点击目前仅显示 Toast，无实际页面跳转

## 重构目标
移除所有非 index 页面文件，保持项目简洁，并确保现有功能正常。

## 实施步骤

### 步骤 1：更新 QuickActions 组件
移除或修改 `components/home/QuickActions.vue` 中对 record 页面的跳转，改为与其他按钮一致的行为（显示 Toast）

### 步骤 2：删除非 index 页面文件
删除以下文件：
- `pages/home/record.vue`
- `pages/home/transfer.vue`
- `pages/assets/add.vue`
- `pages/assets/category.vue`
- `pages/goals/add.vue`
- `pages/goals/detail.vue`
- `pages/profile/edit.vue`

### 步骤 3：验证
确认项目结构整洁，所有剩余文件均为 index 页面或必要的支持文件。

## 风险评估
- **低风险**：这些页面目前未在 pages.json 中注册，index 页面也未使用它们
- 唯一的引用在 QuickActions 组件中，会被修改为安全的 Toast 行为

## 文件清单
### 将被删除的文件
```
pages/home/record.vue
pages/home/transfer.vue
pages/assets/add.vue
pages/assets/category.vue
pages/goals/add.vue
pages/goals/detail.vue
pages/profile/edit.vue
```

### 将被修改的文件
```
components/home/QuickActions.vue
```
