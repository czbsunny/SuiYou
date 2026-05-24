# 设计令牌使用指南

## 1. 概述

### 1.1 设计系统简介

Heritage Hearth Design System 是 SuiYou 应用的核心设计系统，旨在为用户提供温暖、专业、值得信赖的金融体验。该设计系统定义了一套完整的设计令牌（Design Tokens），确保整个应用的视觉一致性和品牌识别度。

设计令牌是设计系统的最小构建单元，它们以变量的形式存储颜色、字体、间距、圆角、阴影等视觉属性。通过使用设计令牌，我们可以：

- **保持一致性**：确保整个应用的视觉风格统一
- **提高效率**：快速引用预定义的样式值
- **便于维护**：修改一处即可全局生效
- **支持主题**：为未来的主题切换提供基础

### 1.2 "温暖极简主义"设计理念

SuiYou 的设计遵循"温暖极简主义"（Warm Minimalism）理念，这一理念体现在：

| 特征 | 说明 | 设计体现 |
|------|------|----------|
| **温暖** | 传递关怀与信任感 | 象牙白背景、柔和阴影、圆润边角 |
| **极简** | 减少视觉噪音，突出核心信息 | 清晰的层级、克制的色彩、充足的留白 |
| **专业** | 建立金融产品的可信度 | 等宽数字字体、精确的排版、规范的数据展示 |
| **人文** | 贴近用户情感，而非冰冷机器 | 暖色调中性色、舒适的交互反馈 |

### 1.3 符合中国金融市场习惯的颜色语义

在中国金融市场，颜色语义与国际标准有所不同：

| 语义 | 中国市场 | 国际市场 |
|------|----------|----------|
| **盈利/上涨** | 红色 `#F44336` | 绿色 |
| **亏损/下跌** | 绿色 `#4CAF50` | 红色 |

> ⚠️ **重要提示**：在设计金融相关功能时，务必使用正确的盈亏颜色语义，避免用户误解。

---

## 2. 颜色系统

### 2.1 品牌色

品牌色是应用的核心识别色，用于主要按钮、强调元素和品牌标识。

```scss
// 品牌主色
$primary: #006754;           // 深绿色 - 主品牌色
$primary-dark: #004D3D;      // 深色变体 - 用于点击态
$primary-light: #008066;     // 浅色变体 - 用于悬浮态
```

**使用场景**：
- 主要操作按钮
- 导航栏选中状态
- 品牌标识
- 重要信息强调

### 2.2 Surface 颜色（表面颜色）

Surface 颜色用于界面背景和容器，构建视觉层级。

```scss
// 基础背景
$background: #FAF9F6;                    // 象牙白 - 页面主背景
$surface: #FAF9F6;                       // 表面色
$surface-dim: #DBDADA;                   // 暗淡表面

// 容器层级
$surface-container-lowest: #FFFFFF;      // 最高层级容器 - 纯白
$surface-container-low: #F5F5F5;         // 低层级容器 - 浅灰

// 背景色变体
$background-white: #FFFFFF;              // 纯白背景
$background-gray: #F5F5F5;               // 灰色背景
```

**层级关系图**：

```
┌─────────────────────────────────────┐
│  $background: #FAF9F6 (页面背景)     │
│  ┌───────────────────────────────┐  │
│  │ $surface-container-low: #F5F5F5 │
│  │  ┌─────────────────────────┐  │  │
│  │  │ $surface-container-     │  │  │
│  │  │ lowest: #FFFFFF (卡片)  │  │  │
│  │  └─────────────────────────┘  │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

### 2.3 文字颜色

```scss
$text-color: #1A1A1A;                    // 主文字 - 标题、正文
$text-color-secondary: #666666;         // 次要文字 - 副标题、说明
$text-color-placeholder: #999999;       // 占位符文字
$text-color-disabled: #CCCCCC;          // 禁用状态文字
```

**使用示例**：

```scss
.page-title {
  color: $text-color;                   // 主标题
}

.section-desc {
  color: $text-color-secondary;         // 章节描述
}

.input-field::placeholder {
  color: $text-color-placeholder;       // 输入框占位符
}

.disabled-text {
  color: $text-color-disabled;          // 禁用文字
}
```

### 2.4 语义化颜色

#### 金融语义色

```scss
// 盈亏颜色（符合中国市场习惯）
$profit: #4CAF50;                       // 盈利/下跌 - 绿色
$loss: #F44336;                         // 亏损/上涨 - 红色
```

#### 功能状态色

```scss
$success-color: #4CAF50;                // 成功状态
$error-color: #F44336;                  // 错误状态
$warning-color: #FF9800;                // 警告状态
$info-color: #2196F3;                   // 信息状态
```

**使用示例**：

```scss
// 盈亏显示
.profit-value {
  color: $profit;
  &::before { content: '+'; }
}

.loss-value {
  color: $loss;
  &::before { content: '-'; }
}

// 状态提示
.success-message {
  color: $success-color;
  background: rgba($success-color, 0.1);
}

.error-message {
  color: $error-color;
  background: rgba($error-color, 0.1);
}
```

### 2.5 边框颜色

```scss
$border-color: #E5E5E5;                 // 标准边框
$border-light: #F0F0F0;                 // 浅色边框
$border-dark: #CCCCCC;                  // 深色边框
```

---

## 3. 排版系统

### 3.1 字体族

```scss
// 主字体 - 用于界面文字
$font-family-primary: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;

// 等宽字体 - 用于数字、代码
$font-family-mono: 'JetBrains Mono', 'SF Mono', Monaco, 'Cascadia Code', monospace;
```

**字体选择说明**：

| 字体 | 用途 | 特点 |
|------|------|------|
| Plus Jakarta Sans | 界面文字 | 现代、温暖、易读性好 |
| JetBrains Mono | 数字、代码 | 等宽、数字对齐、适合金融数据 |

### 3.2 字体大小

```scss
$font-size-xs: 24rpx;                   // 极小 - 辅助信息
$font-size-sm: 26rpx;                   // 小 - 标签、说明
$font-size-base: 28rpx;                 // 基础 - 正文
$font-size-lg: 32rpx;                   // 大 - 副标题
$font-size-xl: 36rpx;                   // 较大 - 标题
$font-size-2xl: 40rpx;                  // 特大 - 主标题
```

> 📝 **注意**：字体大小使用 `rpx` 单位，可根据屏幕宽度自适应。`rpx` 是小程序的响应式单位，750rpx = 屏幕宽度。

### 3.3 字重

```scss
$font-weight-regular: 400;              // 常规 - 正文
$font-weight-medium: 500;               // 中等 - 副标题
$font-weight-semibold: 600;             // 半粗 - 标题
$font-weight-bold: 700;                 // 粗体 - 强调
```

### 3.4 行高

```scss
$line-height-tight: 1.25;               // 紧凑 - 标题
$line-height-base: 1.5;                 // 基础 - 正文
$line-height-relaxed: 1.75;             // 宽松 - 长文本
```

### 3.5 排版规格对照表

| 规格 | 字号 | 字重 | 行高 | 使用场景 |
|------|------|------|------|----------|
| display-lg | 40rpx | 700 | 1.25 | 页面主标题 |
| headline-md | 36rpx | 600 | 1.25 | 区块标题 |
| title-sm | 32rpx | 600 | 1.25 | 卡片标题 |
| body-reg | 28rpx | 400 | 1.5 | 正文内容 |
| body-sm | 26rpx | 400 | 1.5 | 辅助说明 |
| num-display | 40rpx | 600 | 1.25 | 大数字展示 |
| num-data | 28rpx | 500 | 1.5 | 数据展示 |
| label-caps | 24rpx | 500 | 1.5 | 标签文字 |

### 3.6 使用示例

```scss
// 页面标题
.page-title {
  font-family: $font-family-primary;
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  line-height: $line-height-tight;
  color: $text-color;
}

// 数字展示（使用等宽字体）
.amount-display {
  font-family: $font-family-mono;
  font-size: $font-size-2xl;
  font-weight: $font-weight-semibold;
  color: $text-color;
}

// 正文内容
.body-text {
  font-family: $font-family-primary;
  font-size: $font-size-base;
  font-weight: $font-weight-regular;
  line-height: $line-height-base;
  color: $text-color-secondary;
}
```

---

## 4. 间距系统

### 4.1 Base-8 间距比例

设计系统采用 Base-8 间距比例，所有间距值都是 4 的倍数，确保视觉节奏的一致性。

```scss
$spacing-1: 4px;                        // 极小间距
$spacing-2: 8px;                        // 小间距
$spacing-3: 12px;                       // 较小间距
$spacing-4: 16px;                       // 基础间距
$spacing-5: 20px;                       // 中等间距
$spacing-6: 24px;                       // 较大间距
$spacing-7: 28px;                       // 大间距
$spacing-8: 32px;                       // 区块间距
$spacing-10: 40px;                      // 大区块间距
$spacing-12: 48px;                      // 章节间距
```

**间距比例图**：

```
spacing-1  spacing-2  spacing-4  spacing-6  spacing-8
    │          │          │          │          │
    ▼          ▼          ▼          ▼          ▼
    ├─┤        ├──┤       ├───┤      ├────┤     ├─────┤
    4px        8px        16px       24px       32px
```

### 4.2 容器和组件间距

```scss
// 容器内边距
$container-padding: 20px;               // 页面容器内边距

// 组件间距
$stack-gap-lg: 24px;                    // 大间距 - 区块之间
$stack-gap-md: 16px;                    // 中间距 - 元素之间
$stack-gap-sm: 8px;                     // 小间距 - 紧凑元素

// 章节间距
$section-margin: 32px;                  // 章节之间的间距
```

### 4.3 安全区域

```scss
$safe-zone-horizontal: 20px;            // 水平安全区域
```

### 4.4 使用示例

```scss
// 页面容器
.page-container {
  padding: $container-padding;
}

// 列表项
.list-item {
  padding: $spacing-4;
  margin-bottom: $stack-gap-sm;
}

// 卡片
.card {
  padding: $spacing-6;
  margin-bottom: $stack-gap-md;
}

// 章节
.section {
  margin-bottom: $section-margin;
}

// 紧凑布局
.compact-layout {
  gap: $spacing-2;
}
```

---

## 5. 圆角系统

### 5.1 圆角变量

```scss
$rounded-default: 8px;                  // 默认圆角
$rounded-sm: 4px;                       // 小圆角
$rounded-md: 24px;                      // 中圆角（卡片）
$rounded-full: 9999px;                  // 全圆（胶囊）
```

### 5.2 极端圆角设计理念

"极端圆角"（Extreme Roundness）是设计系统的重要特征，体现在：

| 圆角值 | 使用场景 | 视觉效果 |
|--------|----------|----------|
| 4px | 小按钮、标签 | 精致、现代 |
| 8px | 输入框、小卡片 | 友好、舒适 |
| 24px | 大卡片、容器 | 温暖、柔和 |
| 9999px | 胶囊按钮、徽章 | 活泼、亲和 |

### 5.3 使用示例

```scss
// 小按钮
.btn-small {
  border-radius: $rounded-sm;
}

// 输入框
.input-field {
  border-radius: $rounded-default;
}

// 卡片
.card {
  border-radius: $rounded-md;
}

// 胶囊按钮
.btn-capsule {
  border-radius: $rounded-full;
}

// 徽章
.badge {
  border-radius: $rounded-full;
}
```

---

## 6. 阴影和深度

### 6.1 阴影层级

```scss
$shadow-sm: 0 1px 4px rgba(0, 0, 0, 0.06);       // 微阴影
$shadow-base: 0 2px 8px rgba(0, 0, 0, 0.08);     // 基础阴影
$shadow-lg: 0 4px 16px rgba(0, 0, 0, 0.12);      // 大阴影
```

### 6.2 阴影使用场景

| 阴影 | 使用场景 | 层级感 |
|------|----------|--------|
| shadow-sm | 输入框、小卡片 | 轻微浮起 |
| shadow-base | 标准卡片、弹窗 | 明显浮起 |
| shadow-lg | 模态框、重要卡片 | 强调浮起 |

### 6.3 环境色调层级

通过 Surface 颜色和阴影的组合，构建视觉层级：

```
层级 4 (最高)  ┌─────────────────┐  $surface-container-lowest + $shadow-lg
               │   模态框/弹窗    │
               └─────────────────┘

层级 3         ┌─────────────────┐  $surface-container-lowest + $shadow-base
               │     卡片        │
               └─────────────────┘

层级 2         ┌─────────────────┐  $surface-container-low
               │   次级容器      │
               └─────────────────┘

层级 1 (最低)  ┌─────────────────┐  $background
               │    页面背景     │
               └─────────────────┘
```

### 6.4 使用示例

```scss
// 标准卡片
.card {
  background: $surface-container-lowest;
  border-radius: $rounded-md;
  box-shadow: $shadow-base;
  padding: $spacing-6;
}

// 悬浮卡片
.card-elevated {
  background: $surface-container-lowest;
  border-radius: $rounded-md;
  box-shadow: $shadow-lg;
  padding: $spacing-6;
}

// 输入框
.input-field {
  background: $surface-container-low;
  border-radius: $rounded-default;
  box-shadow: $shadow-sm;
  padding: $spacing-4;
}
```

---

## 7. 最佳实践

### 7.1 如何使用变量

#### ✅ 正确做法

```scss
// 使用变量
.my-component {
  color: $text-color;
  background: $background-white;
  padding: $spacing-4;
  border-radius: $rounded-default;
}
```

#### ❌ 错误做法

```scss
// 不要硬编码值
.my-component {
  color: #1A1A1A;              // ❌ 应使用 $text-color
  background: #FFFFFF;          // ❌ 应使用 $background-white
  padding: 16px;                // ❌ 应使用 $spacing-4
  border-radius: 8px;           // ❌ 应使用 $rounded-default
}
```

### 7.2 如何使用混合宏

设计系统提供了多个实用的混合宏（Mixins），可以快速实现常见样式。

#### 布局混合宏

```scss
// 居中布局
.centered-box {
  @include flex-center;
  width: 100px;
  height: 100px;
}

// 两端对齐
.header {
  @include flex-between;
  padding: $spacing-4;
}

// 垂直布局
.sidebar {
  @include flex-column;
  gap: $spacing-4;
}
```

#### 文本混合宏

```scss
// 单行省略
.title {
  @include text-ellipsis;
  max-width: 200px;
}

// 多行省略
.description {
  @include multi-line-ellipsis(3);  // 最多显示3行
}
```

#### 安全区域混合宏

```scss
// 底部安全区域
.bottom-bar {
  @include safe-area-bottom;
  position: fixed;
  bottom: 0;
}

// 顶部安全区域
.top-bar {
  @include safe-area-top;
  position: fixed;
  top: 0;
}
```

#### 组件混合宏

```scss
// 卡片样式
.my-card {
  @include card;
}

// 主按钮
.submit-btn {
  @include button-primary;
  width: 100%;
}

// 默认按钮
.cancel-btn {
  @include button-default;
}

// 财富卡片
.wealth-display {
  @include wealth-card;
}

// 盈利徽章
.profit-badge {
  @include profit-loss-badge('profit');
}

// 亏损徽章
.loss-badge {
  @include profit-loss-badge('loss');
}
```

### 7.3 常见问题和解决方案

#### Q1: 如何处理盈亏颜色？

```scss
// ✅ 正确：使用语义变量
.profit { color: $profit; }
.loss { color: $loss; }

// ❌ 错误：直接使用颜色值
.profit { color: #4CAF50; }
.loss { color: #F44336; }
```

#### Q2: 如何创建卡片？

```scss
// 方式1：使用混合宏
.card {
  @include card;
}

// 方式2：手动组合变量
.card {
  background: $surface-container-lowest;
  border-radius: $rounded-md;
  box-shadow: $shadow-base;
  padding: $spacing-6;
}
```

#### Q3: 如何处理数字显示？

```scss
// 金融数字应使用等宽字体
.amount {
  font-family: $font-family-mono;
  font-weight: $font-weight-semibold;
}

// 配合盈亏颜色
.amount.positive {
  color: $loss;  // 注意：中国市场红色表示上涨
}

.amount.negative {
  color: $profit;  // 注意：中国市场绿色表示下跌
}
```

#### Q4: 如何处理间距？

```scss
// 使用语义化的间距变量
.page {
  padding: $container-padding;         // 页面内边距
}

.section {
  margin-bottom: $section-margin;       // 章节间距
}

.list {
  gap: $stack-gap-md;                   // 列表项间距
}
```

### 7.4 变量速查表

| 类别 | 变量名 | 值 | 用途 |
|------|--------|-----|------|
| 品牌色 | `$primary` | #006754 | 主品牌色 |
| 盈利色 | `$profit` | #4CAF50 | 盈利/下跌 |
| 亏损色 | `$loss` | #F44336 | 亏损/上涨 |
| 背景色 | `$background` | #FAF9F6 | 页面背景 |
| 主文字 | `$text-color` | #1A1A1A | 标题/正文 |
| 次文字 | `$text-color-secondary` | #666666 | 副标题/说明 |
| 基础间距 | `$spacing-4` | 16px | 元素间距 |
| 大间距 | `$spacing-6` | 24px | 区块间距 |
| 默认圆角 | `$rounded-default` | 8px | 输入框/小卡片 |
| 中圆角 | `$rounded-md` | 24px | 大卡片 |
| 基础阴影 | `$shadow-base` | 0 2px 8px rgba(0,0,0,0.08) | 卡片阴影 |

---

## 附录：文件位置

设计令牌定义在以下文件中：

| 文件 | 路径 | 内容 |
|------|------|------|
| variables.scss | `src/styles/variables.scss` | 所有设计变量 |
| mixins.scss | `src/styles/mixins.scss` | 混合宏定义 |
| common.scss | `src/styles/common.scss` | 全局样式 |

在使用设计令牌前，请确保已正确导入：

```scss
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';
```
