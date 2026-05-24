# 小程序编译错误修复计划

## 一、问题分析

### 1.1 编译错误
```
[plugin:vite:vue] src/pages/goals/index.vue (32:13): Element is missing end tag.
```

### 1.2 问题原因
在所有重构的页面和组件中，使用了大量 HTML5 标签：
- `<section>`, `<header>`, `<main>` - 结构标签
- `<div>`, `<span>` - 通用容器
- `<h1>`, `<h2>`, `<h3>` - 标题标签
- `<p>` - 段落标签

微信小程序编译时对这些标签的处理存在问题，导致编译失败。

### 1.3 受影响的文件

| 文件路径 | 问题标签 |
|---------|---------|
| `src/pages/home/index.vue` | `<header>`, `<section>`, `<main>`, `<h2>`, `<span>` |
| `src/pages/goals/index.vue` | `<header>`, `<section>`, `<main>`, `<div>`, `<span>`, `<h1>`, `<h2>`, `<p>` |
| `src/pages/assets/index.vue` | `<header>`, `<section>`, `<main>`, `<div>`, `<h1>`, `<h2>`, `<p>` |
| `src/pages/profile/index.vue` | `<header>`, `<section>`, `<main>`, `<div>`, `<h1>`, `<h2>`, `<p>` |
| `src/components/home/WealthCard.vue` | `<p>`, `<div>`, `<span>`, `<h1>` |
| `src/components/home/TransactionItem.vue` | `<p>`, `<span>` |
| `src/components/goals/GoalCard.vue` | `<p>`, `<div>`, `<span>` |

---

## 二、修复方案

### 2.1 标签替换规则

| 原标签 | 替换为 | 说明 |
|-------|--------|------|
| `<div>` | `<view>` | 通用容器 |
| `<span>` | `<text>` | 行内文本 |
| `<p>` | `<text>` | 段落 |
| `<h1>` | `<text>` | 一级标题 |
| `<h2>` | `<text>` | 二级标题 |
| `<h3>` | `<text>` | 三级标题 |
| `<section>` | `<view>` | 区块 |
| `<header>` | `<view>` | 头部 |
| `<main>` | `<view>` | 主体 |

### 2.2 修复步骤

#### 步骤 1: 修复目标页 `pages/goals/index.vue`
- 替换所有 `<section>` 为 `<view>`
- 替换所有 `<header>` 为 `<view>`
- 替换所有 `<main>` 为 `<view>`
- 替换所有 `<div>` 为 `<view>`
- 替换所有 `<span>` 为 `<text>`
- 替换所有 `<h1>` 为 `<text>`
- 替换所有 `<h2>` 为 `<text>`
- 替换所有 `<p>` 为 `<text>`

#### 步骤 2: 修复首页 `pages/home/index.vue`
- 替换所有 `<section>` 为 `<view>`
- 替换所有 `<header>` 为 `<view>`
- 替换所有 `<main>` 为 `<view>`
- 替换所有 `<h2>` 为 `<text>`
- 替换所有 `<span>` 为 `<text>`

#### 步骤 3: 修复资产页 `pages/assets/index.vue`
- 替换所有 `<section>` 为 `<view>`
- 替换所有 `<header>` 为 `<view>`
- 替换所有 `<main>` 为 `<view>`
- 替换所有 `<div>` 为 `<view>`
- 替换所有 `<h1>` 为 `<text>`
- 替换所有 `<h2>` 为 `<text>`
- 替换所有 `<p>` 为 `<text>`

#### 步骤 4: 修复个人中心 `pages/profile/index.vue`
- 替换所有 `<section>` 为 `<view>`
- 替换所有 `<header>` 为 `<view>`
- 替换所有 `<main>` 为 `<view>`
- 替换所有 `<div>` 为 `<view>`
- 替换所有 `<h1>` 为 `<text>`
- 替换所有 `<h2>` 为 `<text>`
- 替换所有 `<p>` 为 `<text>`

#### 步骤 5: 修复组件文件
- `WealthCard.vue`: 替换 `<p>`, `<div>`, `<span>`, `<h1>` 为 `<text>` 或 `<view>`
- `TransactionItem.vue`: 替换 `<p>`, `<span>` 为 `<text>`
- `GoalCard.vue`: 替换 `<p>`, `<div>`, `<span>` 为 `<text>` 或 `<view>`

#### 步骤 6: 验证编译
运行 `npm run dev:mp-weixin` 验证编译成功

---

## 三、注意事项

### 3.1 样式调整
- 替换标签后，部分样式可能需要调整
- 确保 flex 布局在 `<view>` 标签中正常工作
- 检查 `text-transform` 等样式在 `<text>` 标签中的表现

### 3.2 功能保持
- 所有交互功能（点击事件、动态绑定等）保持不变
- 组件 Props 和 Events 接口保持不变
- 数据绑定逻辑保持不变

---

## 四、输出物

修复后，所有文件中的 HTML 标签将被替换为 UniApp/微信小程序兼容的标签，确保编译成功。
