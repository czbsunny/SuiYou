# 页面重构计划

## 项目概述
根据设计原型（HTML文件）重构4个一级页面，移除顶部导航栏和底部tab bar（小程序自带）。

## 分析结果

### 原型文件分析
设计原型位于 `e:\Codes\SuiYou\app\designs\` 目录，包含4个页面：
- `home.html` - 首页：资产卡片、快捷操作、交易记录
- `goal.html` - 目标页：储蓄目标、愿望清单、进度条
- `asset.html` - 资产页：资产净值、趋势图表、账户列表
- `profile.html` - 个人中心：用户信息、家庭成员、功能菜单

### 现有页面结构
现有页面位于 `e:\Codes\SuiYou\app\src\pages\` 目录：
- `home/index.vue`
- `goals/index.vue`
- `assets/index.vue`
- `profile/index.vue`

### 需要移除的内容
根据用户要求，需要移除：
1. **顶部导航栏**（`.top-bar`）- 小程序自带导航栏
2. **底部tab bar** - 小程序自带tabBar

## 重构计划

### 1. 首页 (home)
- 移除 `.top-bar` 组件
- 调整滚动区域高度，移除顶部偏移
- 保持资产卡片、快捷操作、交易记录内容

### 2. 目标页 (goals)
- 移除 `.top-bar` 组件
- 调整滚动区域高度
- 保持储蓄汇总卡片、排序标签、目标列表

### 3. 资产页 (assets)
- 移除 `.top-bar` 组件
- 调整滚动区域高度
- 保持资产净值卡片、趋势图表、账户列表

### 4. 个人中心页 (profile)
- 移除 `.top-bar` 组件
- 调整滚动区域高度
- 保持用户头像、家庭成员、功能菜单

## 修改的文件
1. `src/pages/home/index.vue`
2. `src/pages/goals/index.vue`
3. `src/pages/assets/index.vue`
4. `src/pages/profile/index.vue`

## 执行步骤
1. 修改首页 - 移除顶部导航栏，调整样式
2. 修改目标页 - 移除顶部导航栏，调整样式
3. 修改资产页 - 移除顶部导航栏，调整样式
4. 修改个人中心页 - 移除顶部导航栏，调整样式

## 注意事项
- 保持原有设计风格和交互逻辑
- 确保页面在小程序环境中正确显示
- 移除顶部导航栏后，确保内容从页面顶部开始