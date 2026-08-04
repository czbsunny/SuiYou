# 账户详情页实现计划

## 需求分析

根据用户提供的设计文件，需要实现三个不同机构类型的账户详情页面：

| 设计文件 | 页面类型 | 机构类型 |
|---------|---------|---------|
| `designs/_1/code.html` | 银行详情页 | 建设银行风格 |
| `designs/_2/code.html` | 支付宝详情页 | 支付宝风格 |
| `designs/_3/code.html` | 微信钱包详情页 | 微信风格 |

所有页面采用统一的 **Heritage Hearth** 设计规范（Warm Minimalism 风格）。

## 项目架构

项目是基于 UniApp 的 Vue 3 + SCSS 项目：
- `src/pages/` - 页面目录
- `src/components/` - 组件目录  
- `src/styles/` - 样式变量（已包含设计系统变量）
- `src/static/` - 静态资源

## 实现方案

### 1. 创建三个账户详情页面

| 页面 | 路径 | 机构类型 |
|------|------|---------|
| 银行详情页 | `pages/accounts/detail-bank` | 建设银行等银行类 |
| 支付宝详情页 | `pages/accounts/detail-alipay` | 支付宝 |
| 微信钱包详情页 | `pages/accounts/detail-wechat` | 微信支付 |

### 2. 页面结构

每个页面包含：
- 顶部导航栏（TopAppBar）- 自定义，包含返回按钮、机构名称、通知按钮、用户头像
- 资产概览卡片（Wealth Card）- 显示账户总余额、收益等信息
- 快捷操作区域（Quick Actions）- 转账、存款、理财、收支等
- 资产列表（Asset List）- 显示该账户下的各类资产
- 底部导航栏（TabBar）- 使用原生 tabBar

### 3. 设计规范遵循

- **颜色系统**：使用 `styles/variables.scss` 中定义的颜色变量
- **字体系统**：Plus Jakarta Sans（正文）+ JetBrains Mono（数据）
- **圆角系统**：使用 $rounded-md (24rpx), $rounded-lg (32rpx), $rounded-xl (48rpx)
- **阴影系统**：使用 $shadow-soft (0 4px 20px rgba(0,0,0,0.04))

### 4. 数据模拟

每个页面使用独立的模拟数据，模拟对应机构类型的账户信息。

### 5. 跳转逻辑

资产页面 (`pages/assets/index.vue`) 点击账户卡片时，根据机构类型跳转到对应详情页：
- 银行类（BANK）→ `/pages/accounts/detail-bank`
- 支付宝（ALIPAY）→ `/pages/accounts/detail-alipay`
- 微信（WECHAT）→ `/pages/accounts/detail-wechat`
- 通过 URL 参数传递账户 ID 和机构名称

## 任务清单

1. 创建银行详情页面 `pages/accounts/detail-bank/index.vue`
2. 创建支付宝详情页面 `pages/accounts/detail-alipay/index.vue`
3. 创建微信钱包详情页面 `pages/accounts/detail-wechat/index.vue`
4. 更新 `pages.json` 注册新页面
5. 更新资产页面的跳转逻辑

## 风险与依赖

- 现有样式变量已包含设计系统定义，可直接使用
- 需要确保页面路由正确注册
- 使用模拟数据，无需后端依赖

## 预期交付物

- 3 个账户详情页面组件
- 更新的 pages.json 配置
- 更新的资产页面跳转逻辑