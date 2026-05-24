# 组件样式示例文档

## 概述

### 组件样式系统简介

Heritage Hearth 设计系统提供了一套完整的 SCSS 混合宏（Mixins），用于快速构建一致性的 UI 组件。这些混合宏封装了常用的样式模式，确保整个应用的视觉风格统一。

### 混合宏的优势

1. **代码复用**：避免重复编写相同的样式代码
2. **一致性保证**：所有组件使用相同的设计规范
3. **易于维护**：修改混合宏即可全局更新所有使用该宏的组件
4. **语义化命名**：混合宏名称直观反映其用途，提高代码可读性

---

## 主要按钮

### 混合宏名称

`button-primary`

### 样式特性

- 主色调背景（#006754）
- 白色文字
- 无边框
- 全圆角设计
- 居中对齐
- 按下状态变深
- 禁用状态半透明

### 使用示例代码

```scss
@import '@/styles/mixins.scss';

.submit-button {
  @include button-primary;
  width: 100%;
  height: 88rpx;
  font-size: $font-size-lg;
  font-weight: $font-weight-medium;
}

.login-btn {
  @include button-primary;
  padding: $spacing-3 $spacing-6;
  min-width: 200rpx;
}
```

```vue
<template>
  <button class="submit-button" @click="handleSubmit">
    提交
  </button>
  
  <button class="login-btn" :disabled="isLoading">
    登录
  </button>
</template>
```

### 效果说明

- **默认状态**：深绿色背景，白色文字，圆角胶囊形状
- **按下状态**：背景色加深 10%，提供触觉反馈
- **禁用状态**：透明度降至 50%，鼠标变为禁用图标

### 使用场景

- 主要操作按钮（提交、确认、保存）
- 登录/注册按钮
- 页面主 CTA（Call to Action）按钮

---

## 财富卡片

### 混合宏名称

`wealth-card`

### 样式特性

- 白色背景
- 24px 大圆角
- 柔和阴影
- 内置数字等宽字体样式
- 24px 内边距

### 使用示例代码

```scss
@import '@/styles/mixins.scss';

.asset-card {
  @include wealth-card;
  margin-bottom: $stack-gap-md;
  
  &__title {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    margin-bottom: $spacing-2;
  }
  
  &__number {
    font-family: $font-family-mono;
    font-size: $font-size-2xl;
    font-weight: $font-weight-bold;
    color: $text-color;
  }
  
  &__label {
    font-size: $font-size-xs;
    color: $text-color-placeholder;
  }
}
```

```vue
<template>
  <view class="asset-card">
    <text class="asset-card__title">总资产</text>
    <text class="asset-card__number">¥ 128,456.78</text>
    <text class="asset-card__label">更新于 2024-01-15</text>
  </view>
</template>
```

### 效果说明

- 卡片呈现白色背景，与页面背景形成层次感
- 大圆角营造柔和、现代的视觉效果
- 阴影提供适度的立体感
- 数字使用等宽字体，确保数字对齐美观

### 使用场景

- 资产总览卡片
- 收益统计卡片
- 账户余额展示
- 数据面板容器

---

## 幽灵列表

### 混合宏名称

`ghost-list`

### 样式特性

- 无列表样式标记
- 零内外边距
- 无分隔线设计
- 偶数行背景变化
- 行间垂直间距分隔

### 使用示例代码

```scss
@import '@/styles/mixins.scss';

.transaction-list {
  @include ghost-list;
  
  &__item {
    @include flex-between;
    padding: $stack-gap-md 0;
    border: none;
    
    &:nth-child(even) {
      background: $surface-container-low;
      border-radius: $rounded-default;
      padding: $stack-gap-md $spacing-4;
    }
  }
  
  &__left {
    @include flex-column;
    gap: $spacing-1;
  }
  
  &__title {
    font-size: $font-size-base;
    color: $text-color;
  }
  
  &__time {
    font-size: $font-size-xs;
    color: $text-color-secondary;
  }
  
  &__amount {
    font-family: $font-family-mono;
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
  }
}
```

```vue
<template>
  <view class="transaction-list">
    <view class="transaction-list__item" v-for="item in list" :key="item.id">
      <view class="transaction-list__left">
        <text class="transaction-list__title">{{ item.title }}</text>
        <text class="transaction-list__time">{{ item.time }}</text>
      </view>
      <text class="transaction-list__amount">{{ item.amount }}</text>
    </view>
  </view>
</template>
```

### 效果说明

- 列表项之间无实体分隔线，视觉更轻盈
- 偶数行使用浅灰背景，便于区分不同行
- 垂直间距提供舒适的阅读节奏
- 整体风格简洁、现代

### 使用场景

- 交易记录列表
- 消息列表
- 设置选项列表
- 数据记录展示

---

## 输入框

### 混合宏名称

`input-field`

### 样式特性

- 象牙白背景（#F5F5F5）
- 无边框设计
- 8px 圆角
- 聚焦时主色调内发光
- 平滑过渡动画
- 占位符文字颜色优化

### 使用示例代码

```scss
@import '@/styles/mixins.scss';

.form-input {
  @include input-field;
  width: 100%;
  height: 88rpx;
  font-size: $font-size-base;
  color: $text-color;
  
  &--large {
    height: 100rpx;
    font-size: $font-size-lg;
  }
  
  &--error {
    &:focus {
      box-shadow: inset 0 0 0 1px $error-color;
    }
  }
}

.search-input {
  @include input-field;
  padding-left: $spacing-8;
  background: $surface-container-low url('@/static/icons/search.svg') no-repeat 16px center;
  background-size: 20px;
}
```

```vue
<template>
  <view class="form-group">
    <input 
      class="form-input" 
      type="text" 
      placeholder="请输入用户名"
      v-model="username"
    />
    
    <input 
      class="form-input form-input--large" 
      type="password" 
      placeholder="请输入密码"
      v-model="password"
    />
    
    <input 
      class="form-input form-input--error" 
      type="text" 
      placeholder="输入有误"
      v-model="errorField"
    />
  </view>
</template>
```

### 效果说明

- **默认状态**：浅灰背景，无边框，视觉柔和
- **聚焦状态**：内边框显示主色调绿色，引导用户注意力
- **输入状态**：文字清晰可读，占位符颜色较淡
- **错误状态**：聚焦时显示红色内边框，提示输入错误

### 使用场景

- 登录/注册表单
- 搜索输入框
- 评论输入
- 信息编辑表单

---

## 盈亏徽章

### 混合宏名称

`profit-loss-badge($type: 'profit')`

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `$type` | String | `'profit'` | 徽章类型，可选 `'profit'`（盈利）或 `'loss'`（亏损） |

### 使用示例代码

```scss
@import '@/styles/mixins.scss';

.profit-badge {
  @include profit-loss-badge('profit');
}

.loss-badge {
  @include profit-loss-badge('loss');
}

.change-badge {
  @include profit-loss-badge($type: 'profit');
  font-size: $font-size-xs;
  padding: $spacing-1 $spacing-2;
}

.large-badge {
  @include profit-loss-badge('profit');
  font-size: $font-size-base;
  padding: $spacing-2 $spacing-4;
}
```

```vue
<template>
  <view class="asset-item">
    <text class="asset-name">股票A</text>
    <view class="asset-change">
      <text class="profit-badge">+12.5%</text>
    </view>
  </view>
  
  <view class="asset-item">
    <text class="asset-name">股票B</text>
    <view class="asset-change">
      <text class="loss-badge">-3.2%</text>
    </view>
  </view>
  
  <view class="portfolio-summary">
    <text class="change-badge">+5.8%</text>
    <text class="change-label">今日收益</text>
  </view>
</template>
```

### 效果说明

- **盈利徽章**：浅绿色背景（10% 不透明度），深绿色文字
- **亏损徽章**：浅红色背景（10% 不透明度），深红色文字
- 胶囊形状，圆角饱满
- 文字居中，字重适中

### 使用场景

- 股票/基金涨跌幅展示
- 收益率显示
- 数据变化趋势标识
- 状态标签

---

## 组合使用

### 如何组合多个混合宏

混合宏可以灵活组合使用，构建复杂的组件样式。以下是组合使用的最佳实践：

```scss
@import '@/styles/mixins.scss';

.complex-card {
  @include wealth-card;
  
  &__header {
    @include flex-between;
    margin-bottom: $spacing-4;
  }
  
  &__list {
    @include ghost-list;
    margin-top: $spacing-4;
  }
  
  &__input {
    @include input-field;
    margin-top: $spacing-4;
  }
  
  &__button {
    @include button-primary;
    margin-top: $spacing-4;
    width: 100%;
  }
  
  &__badge {
    @include profit-loss-badge('profit');
  }
}
```

### 复杂组件示例

```vue
<template>
  <view class="investment-card">
    <view class="investment-card__header">
      <text class="investment-card__title">我的投资组合</text>
      <text class="investment-card__badge">+8.5%</text>
    </view>
    
    <view class="investment-card__list">
      <view class="investment-card__item" v-for="item in investments" :key="item.id">
        <text class="investment-card__name">{{ item.name }}</text>
        <text class="investment-card__value">{{ item.value }}</text>
      </view>
    </view>
    
    <input 
      class="investment-card__input" 
      placeholder="添加新投资..."
      v-model="newInvestment"
    />
    
    <button class="investment-card__button" @click="addInvestment">
      添加投资
    </button>
  </view>
</template>

<style lang="scss">
.investment-card {
  @include wealth-card;
  
  &__header {
    @include flex-between;
    margin-bottom: $spacing-4;
  }
  
  &__title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $text-color;
  }
  
  &__badge {
    @include profit-loss-badge('profit');
  }
  
  &__list {
    @include ghost-list;
    
    .investment-card__item {
      @include flex-between;
      padding: $stack-gap-md 0;
      
      &:not(:last-child) {
        border-bottom: 1px solid $border-light;
      }
    }
  }
  
  &__name {
    font-size: $font-size-base;
    color: $text-color;
  }
  
  &__value {
    font-family: $font-family-mono;
    font-size: $font-size-base;
    color: $text-color;
  }
  
  &__input {
    @include input-field;
    margin-top: $spacing-4;
    width: 100%;
    height: 80rpx;
  }
  
  &__button {
    @include button-primary;
    margin-top: $spacing-4;
    width: 100%;
    height: 88rpx;
  }
}
</style>
```

---

## 自定义扩展

### 如何基于现有混合宏扩展

可以在现有混合宏基础上进行扩展，创建符合特定需求的组件样式：

```scss
@import '@/styles/mixins.scss';

@mixin button-primary-large {
  @include button-primary;
  height: 100rpx;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
}

@mixin wealth-card-highlight {
  @include wealth-card;
  border: 2px solid $primary;
  position: relative;
  
  &::before {
    content: '推荐';
    position: absolute;
    top: 0;
    right: $spacing-4;
    background: $primary;
    color: #ffffff;
    font-size: $font-size-xs;
    padding: $spacing-1 $spacing-2;
    border-radius: 0 0 $rounded-sm $rounded-sm;
  }
}

@mixin input-field-with-icon($icon-position: 'left') {
  @include input-field;
  
  @if $icon-position == 'left' {
    padding-left: $spacing-8;
  } @else {
    padding-right: $spacing-8;
  }
}

@mixin profit-loss-badge-large($type: 'profit') {
  @include profit-loss-badge($type);
  font-size: $font-size-lg;
  padding: $spacing-2 $spacing-4;
}
```

### 自定义组件示例

```scss
@import '@/styles/mixins.scss';

.premium-card {
  @include wealth-card-highlight;
  
  &__title {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $text-color;
    margin-bottom: $spacing-2;
  }
  
  &__value {
    font-family: $font-family-mono;
    font-size: 48rpx;
    font-weight: $font-weight-bold;
    color: $primary;
  }
}

.search-field {
  @include input-field-with-icon('left');
  background-image: url('@/static/icons/search.svg');
  background-repeat: no-repeat;
  background-position: 16px center;
  background-size: 20px;
}

.action-button {
  @include button-primary-large;
  
  &--full {
    width: 100%;
  }
  
  &--outline {
    background: transparent;
    border: 2px solid $primary;
    color: $primary;
    
    &:active {
      background: rgba($primary, 0.1);
    }
  }
}
```

```vue
<template>
  <view class="dashboard">
    <view class="premium-card">
      <text class="premium-card__title">年度收益</text>
      <text class="premium-card__value">¥ 56,789.00</text>
    </view>
    
    <view class="search-section">
      <input 
        class="search-field" 
        placeholder="搜索投资产品..."
        v-model="searchQuery"
      />
    </view>
    
    <view class="action-section">
      <button class="action-button action-button--full">
        立即投资
      </button>
      <button class="action-button action-button--outline">
        了解更多
      </button>
    </view>
  </view>
</template>
```

---

## 附录：变量参考

### 颜色变量

| 变量名 | 值 | 用途 |
|--------|-----|------|
| `$primary` | #006754 | 主色调 |
| `$profit` | #4CAF50 | 盈利色 |
| `$loss` | #F44336 | 亏损色 |
| `$surface-container-lowest` | #FFFFFF | 最高表面色 |
| `$surface-container-low` | #F5F5F5 | 较低表面色 |

### 间距变量

| 变量名 | 值 | 用途 |
|--------|-----|------|
| `$spacing-1` | 4px | 最小间距 |
| `$spacing-2` | 8px | 小间距 |
| `$spacing-3` | 12px | 中小间距 |
| `$spacing-4` | 16px | 基础间距 |
| `$spacing-6` | 24px | 大间距 |

### 圆角变量

| 变量名 | 值 | 用途 |
|--------|-----|------|
| `$rounded-sm` | 4px | 小圆角 |
| `$rounded-default` | 8px | 默认圆角 |
| `$rounded-md` | 24px | 大圆角 |
| `$rounded-full` | 9999px | 全圆角 |

### 字体变量

| 变量名 | 值 | 用途 |
|--------|-----|------|
| `$font-family-primary` | Plus Jakarta Sans | 主要字体 |
| `$font-family-mono` | JetBrains Mono | 等宽字体 |
| `$font-size-xs` | 24rpx | 超小字号 |
| `$font-size-sm` | 26rpx | 小字号 |
| `$font-size-base` | 28rpx | 基础字号 |
| `$font-size-lg` | 32rpx | 大字号 |
