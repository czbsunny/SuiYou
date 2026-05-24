# Tasks

- [x] Task 1: 更新颜色系统变量
  - [x] SubTask 1.1: 在 `variables.scss` 中定义完整的颜色令牌系统（surface、primary、secondary、tertiary、error 等）
  - [x] SubTask 1.2: 添加语义化颜色变量（盈利色、亏损色、背景色、强调色）
  - [x] SubTask 1.3: 创建颜色层级变量（surface-container-lowest 到 highest）

- [x] Task 2: 更新排版系统变量
  - [x] SubTask 2.1: 定义字体族变量（Plus Jakarta Sans、JetBrains Mono）
  - [x] SubTask 2.2: 创建排版规格变量（display-lg、headline-md、title-sm、body-reg、body-sm、num-display、num-data、label-caps）
  - [x] SubTask 2.3: 添加字体大小、字重、行高、字间距变量

- [x] Task 3: 更新间距和布局系统
  - [x] SubTask 3.1: 定义间距变量（container-padding、stack-gap-lg/md/sm、section-margin）
  - [x] SubTask 3.2: 创建安全区域变量（20px 横向边距）
  - [x] SubTask 3.3: 更新 base-8 间距比例系统

- [x] Task 4: 更新圆角系统
  - [x] SubTask 4.1: 定义圆角变量（sm、DEFAULT、md、lg、xl、full）
  - [x] SubTask 4.2: 更新组件圆角以符合极端圆角设计（24px 标准容器）

- [x] Task 5: 更新阴影和深度系统
  - [x] SubTask 5.1: 定义阴影变量（Level 1 卡片阴影、Level 2 活动阴影）
  - [x] SubTask 5.2: 创建环境色调层级混合宏

- [x] Task 6: 创建组件样式混合宏
  - [x] SubTask 6.1: 创建主要按钮混合宏（胶囊形状、深青绿色、无阴影、按下效果）
  - [x] SubTask 6.2: 创建财富卡片混合宏（白色容器、24px 圆角、柔和阴影）
  - [x] SubTask 6.3: 创建幽灵列表混合宏（无分隔线、垂直间距）
  - [x] SubTask 6.4: 创建输入框混合宏（象牙白背景、无边框、聚焦内发光）
  - [x] SubTask 6.5: 创建盈亏徽章混合宏（胶囊形状、半透明背景）

- [x] Task 7: 更新全局样式
  - [x] SubTask 7.1: 更新 `common.scss` 以使用新的颜色和排版变量
  - [x] SubTask 7.2: 更新全局背景色为象牙白 (#FAF9F6)
  - [x] SubTask 7.3: 更新全局字体为 Plus Jakarta Sans

- [x] Task 8: 更新页面组件样式
  - [x] SubTask 8.1: 更新首页样式以符合新设计系统
  - [x] SubTask 8.2: 更新目标页面样式
  - [x] SubTask 8.3: 更新资产页面样式
  - [x] SubTask 8.4: 更新个人中心页面样式

- [x] Task 9: 创建设计令牌文档
  - [x] SubTask 9.1: 创建设计令牌使用指南
  - [x] SubTask 9.2: 创建组件样式示例文档

# Task Dependencies
- [Task 2] depends on [Task 1]
- [Task 3] depends on [Task 1]
- [Task 4] depends on [Task 1]
- [Task 5] depends on [Task 1]
- [Task 6] depends on [Task 1, Task 2, Task 3, Task 4, Task 5]
- [Task 7] depends on [Task 1, Task 2, Task 6]
- [Task 8] depends on [Task 7]
- [Task 9] depends on [Task 1, Task 2, Task 3, Task 4, Task 5, Task 6]
