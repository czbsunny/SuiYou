# 应用设计系统规范

## Why
当前 app 项目需要一套统一的设计系统，以确保多平台（微信小程序、抖音小程序、Android、iOS）的一致性体验，并传达"温暖极简主义"的品牌理念，为用户提供安全、清晰、安静奢华的视觉体验。

## What Changes
- 创建统一的设计令牌系统（颜色、排版、间距、形状）
- 实现符合中国金融市场习惯的颜色语义（红色=盈利）
- 建立基于"温暖极简主义"的视觉层级系统
- 定义可复用的组件样式规范
- **BREAKING** 更新现有的样式变量以符合新的设计系统

## Impact
- Affected specs: 所有页面和组件的视觉呈现
- Affected code:
  - `src/styles/variables.scss` - 颜色和排版变量
  - `src/styles/mixins.scss` - 组件混合宏
  - `src/styles/common.scss` - 全局样式
  - 所有页面组件的样式实现

## ADDED Requirements

### Requirement: 颜色系统
系统应提供符合中国金融市场习惯的颜色系统，传达品牌个性。

#### Scenario: 主要操作颜色
- **WHEN** 用户查看主要操作按钮或活动状态
- **THEN** 显示深青绿色 (#006754) 作为主色调

#### Scenario: 盈利显示
- **WHEN** 用户查看盈利数据或买入操作
- **THEN** 使用传承红 (#E63946) 显示，符合中国市场习惯

#### Scenario: 背景色
- **WHEN** 用户浏览应用界面
- **THEN** 使用象牙白 (#FAF9F6) 作为背景，减少视觉疲劳

#### Scenario: 高端标识
- **WHEN** 显示高级功能或财富里程碑
- **THEN** 使用香槟金 (#C5A36A) 作为强调色

### Requirement: 排版系统
系统应提供清晰的排版层级，区分叙事元素和金融数据。

#### Scenario: 文本排版
- **WHEN** 显示标题、正文或 UI 标签
- **THEN** 使用 Plus Jakarta Sans 字体，配合相应的字重和行高

#### Scenario: 数字排版
- **WHEN** 显示货币值、百分比或账号
- **THEN** 使用 JetBrains Mono 等宽字体，确保数字对齐

#### Scenario: 大标题显示
- **WHEN** 显示总资产余额
- **THEN** 使用 display-lg 规格（32px, 粗体700, 行高40px）

### Requirement: 布局与间距
系统应遵循流畅网格模型，优化移动端优先的小程序体验。

#### Scenario: 安全区域
- **WHEN** 用户查看任何屏幕
- **THEN** 保持 20px 横向边距，确保内容不拥挤

#### Scenario: 间距节奏
- **WHEN** 布局功能区块
- **THEN** 使用 base-8 比例，功能区块间使用 24px+ 间距

#### Scenario: 数据分组
- **WHEN** 显示相关的金融指标（如盈亏、总收益）
- **THEN** 在单个卡片内使用 8px 内部间距分组

### Requirement: 高度与深度
系统应使用环境色调层级而非硬边框建立视觉层级。

#### Scenario: 基础层级
- **WHEN** 显示页面背景
- **THEN** 使用象牙白 (#FAF9F6) 作为 Level 0

#### Scenario: 卡片层级
- **WHEN** 显示卡片容器
- **THEN** 使用纯白 (#FFFFFF) 配合柔和阴影（Y: 4, Blur: 20, Opacity: 4%）

#### Scenario: 活动层级
- **WHEN** 显示模态框或活动元素
- **THEN** 使用更明显的阴影（Y: 8, Blur: 32, Opacity: 8%）

### Requirement: 形状系统
系统应使用极端圆角传达有机、舒适的品牌特性。

#### Scenario: 标准容器
- **WHEN** 显示卡片或主要容器
- **THEN** 使用 24px 圆角半径

#### Scenario: 主要按钮
- **WHEN** 显示主要操作按钮
- **THEN** 使用全圆角（胶囊形状）

#### Scenario: 小型元素
- **WHEN** 显示标签或芯片
- **THEN** 使用 12px 圆角

### Requirement: 组件样式
系统应提供符合设计规范的组件样式。

#### Scenario: 主要按钮
- **WHEN** 用户查看主要操作按钮
- **THEN** 显示胶囊形状、深青绿色背景、白色文字、无阴影，按下时使用 10% 深色调

#### Scenario: 财富卡片
- **WHEN** 显示资产组合主要信息
- **THEN** 使用大白色容器、24px 圆角、JetBrains Mono 显示主要余额

#### Scenario: 数据列表
- **WHEN** 显示数据列表
- **THEN** 使用"幽灵列表"样式，无分隔线，使用垂直间距和背景变化分隔行

#### Scenario: 输入框
- **WHEN** 显示输入字段
- **THEN** 使用柔和象牙白背景、无边框，聚焦时显示 1px 主色调内发光

#### Scenario: 盈亏徽章
- **WHEN** 显示盈利或亏损状态
- **THEN** 使用胶囊形状、10% 不透明度背景、100% 不透明度文字

## MODIFIED Requirements

### Requirement: 样式变量系统
原有的样式变量系统需要更新以符合新的设计系统规范。

**原系统**:
- 主色调: #7dc6a2
- 成功色: #52c41a
- 错误色: #f5222d
- 背景色: #f9f8f4

**新系统**:
- 主色调: #006754 (Deep Cyan Green)
- 盈利色: #E63946 (Heritage Red)
- 亏损色: 使用主色调的降低饱和度版本或中性灰
- 背景色: #FAF9F6 (Ivory White)

### Requirement: 圆角系统
原有的圆角系统需要更新以符合极端圆角设计。

**原系统**:
- sm: 8rpx
- base: 12rpx
- lg: 16rpx

**新系统**:
- sm: 12px (小型元素)
- DEFAULT: 16px (标准元素)
- md: 24px (中等容器)
- lg: 32px (大型容器)
- xl: 48px (超大容器)
- full: 9999px (胶囊形状)

## REMOVED Requirements

无移除的需求。所有现有功能保持，仅更新视觉样式。
