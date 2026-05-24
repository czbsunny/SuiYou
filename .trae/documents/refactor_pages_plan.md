
# 一级页面重构计划

## 一、需求分析

### 1.1 需求概述
根据用户需求，需要基于 `e:\Codes\SuiYou\app\designs` 目录中的设计文件，重构四个一级页面：
- **首页** (`home.html`)
- **目标页** (`goal.html`)
- **资产页** (`asset.html`)
- **个人中心** (`profile.html`)

所有数据需要实现与后端接口的交互，可使用测试数据先完成前端开发。

### 1.2 设计标准分析

基于设计文件分析，设计系统包含以下关键要素：

| 设计要素 | 规范说明 |
|---------|---------|
| **颜色系统** | Material Design 风格，主色 `#006754`，次色 `#b7102a`，第三色 `#705624` |
| **字体** | Plus Jakarta Sans (正文) + JetBrains Mono (数字) |
| **间距系统** | Base-8 系统，`container-padding: 16px`，`section-margin: 32px` |
| **圆角系统** | `rounded-md: 24rpx`，`rounded-lg: 32rpx`，`rounded-xl: 48rpx` |
| **阴影** | 软阴影 `0 4px 20px rgba(0,0,0,0.04)` |
| **导航风格** | 微信小程序风格，顶部 AppBar 高度 44px，底部 TabBar |

### 1.3 页面结构对比

| 页面 | 现有实现 | 设计标准要求 |
|-----|---------|-------------|
| **首页** | 简单资产卡片 + 记录列表 | Hero 资产卡片 + 快捷操作 + 近期交易列表 |
| **目标页** | 简单目标列表 | 储蓄汇总卡片 + 排序筛选 + 目标卡片列表 |
| **资产页** | 资产分类 + 账户列表 | 趋势图资产卡片 + 账户列表 |
| **个人中心** | 简单用户信息 + 菜单 | 用户信息 + 家庭成员管理 + 功能菜单 |

---

## 二、技术方案

### 2.1 技术栈
- **框架**: UniApp (Vue 3)
- **状态管理**: Pinia
- **样式**: SCSS
- **构建工具**: Vite

### 2.2 目录结构
```
app/src/
├── components/          # 公共组件
│   ├── common/         # 通用组件
│   │   └── ...
│   └── home/           # 首页组件
│       ├── WealthCard.vue      # 资产净值卡片
│       ├── QuickActions.vue    # 快捷操作
│       └── TransactionItem.vue # 交易项
├── pages/              # 页面
│   ├── home/index.vue  # 首页
│   ├── goals/index.vue # 目标页
│   ├── assets/index.vue # 资产页
│   └── profile/index.vue # 个人中心
├── stores/             # 状态管理
├── api/                # API 接口
└── styles/             # 样式变量
```

### 2.3 API 接口规划

| 页面 | 所需接口 | 状态 |
|-----|---------|------|
| **首页** | `/api/assets/summary` - 资产汇总 | ✅ 已存在 |
| | `/api/transactions` - 近期交易 | ⚠️ 需确认 |
| **目标页** | `/api/goals` - 目标列表 | ✅ 已存在 |
| **资产页** | `/api/assets` - 资产列表 | ✅ 已存在 |
| | `/api/assets/accounts` - 账户列表 | ✅ 已存在 |
| **个人中心** | `/api/user/profile` - 用户信息 | ✅ 已存在 |
| | `/api/family/members` - 家庭成员 | ⚠️ 需确认 |

---

## 三、重构计划

### 3.1 任务分解

| 序号 | 任务名称 | 描述 | 依赖 | 优先级 |
|:---:|---------|------|------|:-----:|
| 1 | 更新样式变量 | 确保 `variables.scss` 与设计标准一致 | - | 高 |
| 2 | 创建首页组件 | 创建 `WealthCard`、`QuickActions`、`TransactionItem` 组件 | 1 | 高 |
| 3 | 重构首页 | 基于设计文件重构首页，实现接口交互 | 2 | 高 |
| 4 | 创建目标页组件 | 创建目标卡片组件 | 1 | 高 |
| 5 | 重构目标页 | 实现储蓄汇总和目标列表 | 4 | 高 |
| 6 | 创建资产页组件 | 创建趋势图组件 | 1 | 高 |
| 7 | 重构资产页 | 实现趋势图和账户列表 | 6 | 高 |
| 8 | 创建个人中心组件 | 创建家庭成员组件 | 1 | 高 |
| 9 | 重构个人中心 | 实现用户信息和菜单列表 | 8 | 高 |
| 10 | 测试验证 | 验证页面功能和样式 | 3,5,7,9 | 高 |

### 3.2 详细任务说明

#### 任务 1: 更新样式变量
- 确保 `src/styles/variables.scss` 中的颜色、字体、间距与设计文件一致
- 添加缺失的设计变量

#### 任务 2: 创建首页组件
- `WealthCard.vue`: Hero 资产净值卡片，包含金额显示和本月增长
- `QuickActions.vue`: 快捷操作按钮组（预算、记账、存钱、报告）
- `TransactionItem.vue`: 交易列表项组件

#### 任务 3: 重构首页
- 移除旧的简单布局
- 实现新的顶部 AppBar（头像 + 问候语 + 通知按钮）
- 集成 WealthCard 组件显示资产净值
- 集成 QuickActions 组件
- 实现近期交易列表
- 接入 `/api/assets/summary` 和 `/api/transactions` 接口

#### 任务 4: 创建目标页组件
- `GoalCard.vue`: 目标卡片，包含进度条、时间信息

#### 任务 5: 重构目标页
- 实现储蓄汇总卡片
- 实现排序筛选功能（目标金额、截止时间、完成度）
- 集成 GoalCard 组件列表
- 接入 `/api/goals` 接口

#### 任务 6: 创建资产页组件
- `TrendChart.vue`: 资产趋势图组件（SVG 实现）

#### 任务 7: 重构资产页
- 实现带趋势图的 Hero 资产卡片
- 实现账户列表（银行、证券、微信支付等）
- 接入 `/api/assets` 和 `/api/assets/accounts` 接口

#### 任务 8: 创建个人中心组件
- `FamilyMember.vue`: 家庭成员头像组件

#### 任务 9: 重构个人中心
- 实现用户头像和信息展示
- 实现家庭成员管理模块
- 实现功能菜单列表（账户设置、个人测评、账单导出等）
- 接入 `/api/user/profile` 和 `/api/family/members` 接口

#### 任务 10: 测试验证
- 验证各页面样式与设计文件一致
- 验证接口数据正确渲染
- 验证交互功能正常

---

## 四、数据模型

### 4.1 测试数据结构

#### 资产汇总 (`/api/assets/summary`)
```json
{
  "total": 12482930.00,
  "monthChange": 42105.20,
  "monthChangePercent": 3.2,
  "trendData": [
    {"month": "2024-01", "value": 11800000},
    {"month": "2024-02", "value": 11950000},
    {"month": "2024-03", "value": 12100000},
    {"month": "2024-04", "value": 12350000},
    {"month": "2024-05", "value": 12482930}
  ]
}
```

#### 近期交易 (`/api/transactions`)
```json
{
  "list": [
    {
      "id": 1,
      "title": "精品百货",
      "category": "购物",
      "amount": -1280.00,
      "account": "招商银行卡",
      "time": "2024-05-24 14:20"
    },
    {
      "id": 2,
      "title": "股票红利发放",
      "category": "投资",
      "amount": 24500.00,
      "account": "证券账户",
      "time": "2024-05-23"
    }
  ]
}
```

#### 目标列表 (`/api/goals`)
```json
{
  "list": [
    {
      "id": 1,
      "name": "购车计划",
      "description": "家庭 SUV 升级",
      "targetAmount": 600000,
      "currentAmount": 450000,
      "progress": 75,
      "startDate": "2023-01-01",
      "deadline": "2024-12-31",
      "daysLeft": 45
    }
  ],
  "totalSaved": 1248500,
  "overallProgress": 64
}
```

#### 账户列表 (`/api/assets/accounts`)
```json
{
  "list": [
    {
      "id": 1,
      "name": "招商银行 (私行)",
      "type": "储蓄账户",
      "institution": "招商银行",
      "balance": 2450000.00,
      "change": -12400.00
    },
    {
      "id": 2,
      "name": "中信证券",
      "type": "美股/港股投资",
      "institution": "中信证券",
      "balance": 4120500.00,
      "change": -29700.50
    }
  ]
}
```

#### 用户信息 (`/api/user/profile`)
```json
{
  "id": 1,
  "name": "陈伟",
  "role": "主管理员",
  "joinDate": "2012-01-15",
  "familyMembers": [
    {"id": 2, "name": "SARAH", "relation": "配偶"},
    {"id": 3, "name": "KEVIN", "relation": "儿子"},
    {"id": 4, "name": "MAYA", "relation": "女儿"}
  ]
}
```

---

## 五、风险评估

| 风险点 | 风险等级 | 应对措施 |
|-------|:-------:|---------|
| 设计文件与现有代码差异较大 | 高 | 按设计文件重新实现，保留可复用的基础组件 |
| 接口未就绪 | 中 | 使用测试数据完成前端，预留接口调用逻辑 |
| 样式兼容性问题 | 中 | 使用 SCSS 变量统一管理，确保跨平台兼容 |
| 数据格式不一致 | 中 | 定义统一的数据模型，使用适配器转换 |

---

## 六、进度预估

| 阶段 | 任务 | 预估时间 |
|-----|------|---------|
| 第一阶段 | 样式变量更新 + 首页重构 | 2 天 |
| 第二阶段 | 目标页重构 | 1.5 天 |
| 第三阶段 | 资产页重构 | 1.5 天 |
| 第四阶段 | 个人中心重构 | 1.5 天 |
| 第五阶段 | 测试验证 | 0.5 天 |
| **总计** | | **7 天** |

---

## 七、输出物

完成后将交付以下文件：

| 文件路径 | 说明 |
|---------|------|
| `src/pages/home/index.vue` | 重构后的首页 |
| `src/pages/goals/index.vue` | 重构后的目标页 |
| `src/pages/assets/index.vue` | 重构后的资产页 |
| `src/pages/profile/index.vue` | 重构后的个人中心 |
| `src/components/home/WealthCard.vue` | 资产净值卡片组件 |
| `src/components/home/QuickActions.vue` | 快捷操作组件 |
| `src/components/home/TransactionItem.vue` | 交易项组件 |
| `src/components/goals/GoalCard.vue` | 目标卡片组件 |
| `src/components/assets/TrendChart.vue` | 趋势图组件 |
| `src/components/profile/FamilyMember.vue` | 家庭成员组件 |

---

**文档版本**: v1.0  
**创建日期**: 2024-05-24  
**适用项目**: SuiYou 岁有应用
