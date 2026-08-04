# 资产持仓页面复刻计划

## 项目概述
根据设计文件 `d:\code\SuiYou\app\designs\06161717` 中的6个页面设计，需要在 `d:\code\SuiYou\app\src\pages\asset\holding` 目录下创建对应的 UniApp 页面。

## 设计页面分析

| 设计文件 | 页面名称 | 功能描述 | 目标文件 |
|---------|---------|---------|---------|
| `_1/code.html` | 基金持仓 | 展示基金总资产、持有收益、持仓明细列表 | `fund.vue` |
| `_2/code.html` | 股票持仓 | 展示股票总市值、盈亏信息、持仓列表 | `stock.vue` |
| `_3/code.html` | 添加基金 | 手动输入基金信息、待添加列表管理 | `add-fund.vue` |
| `_4/code.html` | 搜索页面 | 基金/股票搜索、历史记录、类型切换 | `search.vue` |
| `_5/code.html` | 搜索结果 | 展示搜索结果列表、支持筛选 | `search-result.vue` |
| `_6/code.html` | 录入持仓 | 手动录入股票持仓信息 | `add-stock.vue` |

## 技术方案

### 框架与语言
- **框架**: UniApp (Vue 3)
- **语言**: TypeScript
- **样式**: SCSS (使用项目现有变量)

### 设计规范
遵循 Heritage Hearth 设计系统：
- 主色调: `$primary: #006754` (深青色)
- 盈利色: `$profit: #E02020` (红色，中国市场标准)
- 亏损色: `$loss: #009688` (绿色)
- 圆角: 大圆角设计 (`$rounded-lg: 32rpx`)
- 字体: Plus Jakarta Sans (正文) + JetBrains Mono (数据)

### 页面结构
```
src/pages/asset/holding/
├── fund.vue          # 基金持仓页面
├── stock.vue         # 股票持仓页面
├── add-fund.vue      # 添加基金页面
├── search.vue        # 搜索页面
├── search-result.vue # 搜索结果页面
└── add-stock.vue     # 录入股票持仓页面
```

## 实现步骤

### 步骤 1: 创建目录结构
创建 `holding` 目录及6个页面文件。

### 步骤 2: 实现基金持仓页面 (`fund.vue`)
- TopAppBar 导航栏
- Portfolio Hero Card (总资产展示)
- 持有明细列表
- 底部导航栏

### 步骤 3: 实现股票持仓页面 (`stock.vue`)
- TopAppBar 导航栏
- Header Portfolio Card (总市值)
- 持仓列表（现价、成本、仓位）
- 底部导航栏

### 步骤 4: 实现添加基金页面 (`add-fund.vue`)
- 手动输入表单（基金名称、持有金额、收益）
- 图片识别快捷入口
- 待添加列表管理
- 确认添加按钮

### 步骤 5: 实现搜索页面 (`search.vue`)
- 搜索输入框
- 资产类型切换器（基金/股票/港股/美股）
- 历史记录展示

### 步骤 6: 实现搜索结果页面 (`search-result.vue`)
- 筛选标签
- 结果列表（基金名称、代码、类型、涨跌）
- 加载更多动画

### 步骤 7: 实现录入持仓页面 (`add-stock.vue`)
- 股票搜索触发
- 成本价、持仓股数输入
- 待添加列表管理
- 确认录入按钮

## 依赖与资源
- 使用项目现有样式变量 (`@/styles/variables.scss`)
- 使用 Material Symbols 图标（通过 font-family 引入）
- 复用现有组件模式

## 风险评估
- **低风险**: 页面结构清晰，无复杂交互逻辑
- **注意点**: 需要保持与现有页面风格一致，使用统一的设计变量

## 交付标准
- 6个页面均使用 UniApp 规范编写
- 样式与设计文件保持一致
- 数据使用模拟数据
- 基本交互逻辑完整（点击、跳转等）

## 后续工作
- 完成后需要更新 `pages.json` 配置路由
- 可根据需求添加页面间的跳转逻辑