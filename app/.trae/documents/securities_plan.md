# 证券账户页面实现计划

## 一、需求分析

根据设计文件 `d:\code\SuiYou\app\designs\06151423` 中的原型，需要实现以下6个页面：

| 设计文件 | 页面功能 | 页面路径 |
|---------|---------|---------|
| _1/code.html | 证券账户详情页 | `pages/accounts/detail-securities/index.vue` |
| _2/code.html | 交易查询页（当日成交/历史成交/流水查询） | `pages/accounts/transaction-query/index.vue` |
| _3/code.html | 银证转账页（转入证券） | `pages/accounts/transfer/index.vue` |
| _4/code.html | 银证转账页（转出证券） | 与转入共用一个页面，通过切换实现 |
| _5/code.html | 股票卖出页 | `pages/accounts/stock-sell/index.vue` |
| _6/code.html | 股票买入页 | `pages/accounts/stock-buy/index.vue` |

## 二、设计规范

### 2.1 颜色规范（中国市场习惯）
- **盈利色**: 红色 `#E02020` / `#b7102a`
- **亏损色**: 绿色 `#009688` / `#006754`
- **主色调**: 绿色 `#006754`
- **次色调**: 红色 `#b7102a`

### 2.2 组件风格
保持与现有账户详情页面（`detail-bank`、`detail-alipay`、`detail-wechat`）一致的设计风格：
- 使用 Material Symbols Outlined 图标
- 使用 JetBrains Mono 字体显示数字
- 使用 Plus Jakarta Sans 字体显示文本
- 卡片圆角使用 `$rounded-lg` (32rpx)
- 阴影使用 `$shadow-soft`

## 三、页面结构设计

### 3.1 证券账户详情页 (`detail-securities/index.vue`)
参考 `detail-bank/index.vue` 的结构：
- TopAppBar（返回按钮 + 标题 + 通知 + 用户头像）
- Wealth Hero Card（总资产、今日盈亏、累计盈亏、可用余额）
- Quick Actions（买入、卖出、转账、查询）
- 持仓明细列表（股票、基金分类）

### 3.2 交易查询页 (`transaction-query/index.vue`)
- TopAppBar（返回按钮 + 标题）
- Tab导航（当日成交、历史成交、流水查询）
- 交易列表（买入/卖出/撤单状态，包含价格、成交量、佣金）

### 3.3 银证转账页 (`transfer/index.vue`)
- TopAppBar（返回按钮 + 标题 + 交易记录入口）
- 分段选择器（转入证券 / 转出证券）
- 资金流向可视化（银行账户 ↔ 证券账户）
- 银行卡选择
- 金额输入
- 确认按钮

### 3.4 股票买入页 (`stock-buy/index.vue`)
- TopAppBar（返回按钮 + 标题）
- 股票搜索/选择
- 买入价格输入（带涨跌停提示）
- 买入数量输入（带快捷比例按钮）
- 五档盘口（卖盘/买盘）
- 可用资金显示
- 立即买入按钮

### 3.5 股票卖出页 (`stock-sell/index.vue`)
- TopAppBar（返回按钮 + 标题）
- 已持仓股票选择
- 卖出价格输入
- 卖出数量输入（带快捷比例按钮）
- 五档盘口（卖盘/买盘）
- 预计成交金额
- 卖出按钮

## 四、文件结构

```
src/
└── pages/
    └── accounts/
        ├── detail-securities/
        │   └── index.vue      # 证券账户详情页
        ├── transaction-query/
        │   └── index.vue      # 交易查询页
        ├── transfer/
        │   └── index.vue      # 银证转账页
        ├── stock-buy/
        │   └── index.vue      # 股票买入页
        └── stock-sell/
            └── index.vue      # 股票卖出页
```

## 五、页面路由配置

需要在 `pages.json` 中添加以下路由：

| 路径 | 导航样式 | 标题 |
|-----|---------|-----|
| `pages/accounts/detail-securities/index` | custom | 证券账户 |
| `pages/accounts/transaction-query/index` | custom | 交易查询 |
| `pages/accounts/transfer/index` | custom | 转账 |
| `pages/accounts/stock-buy/index` | custom | 股票买入 |
| `pages/accounts/stock-sell/index` | custom | 交易中心 |

## 六、技术实现要点

### 6.1 数据模拟
- 参考现有账户详情页的数据结构
- 使用 `ref` 定义响应式数据
- 提供模拟数据用于展示

### 6.2 交互功能
- 金额可见性切换
- Tab切换（交易查询）
- 转入/转出切换（转账）
- 价格/数量调整
- 快捷比例选择（1/4, 1/2, 3/4, 全仓）
- 盘口数据点击填充价格

### 6.3 样式实现
- 使用 SCSS 变量 `@import '@/styles/variables.scss'`
- 使用 Material Icons 字体图标
- 使用 JetBrains Mono 字体显示金融数据

## 七、实现步骤

1. **创建证券账户详情页** - `pages/accounts/detail-securities/index.vue`
2. **创建交易查询页** - `pages/accounts/transaction-query/index.vue`
3. **创建银证转账页** - `pages/accounts/transfer/index.vue`
4. **创建股票买入页** - `pages/accounts/stock-buy/index.vue`
5. **创建股票卖出页** - `pages/accounts/stock-sell/index.vue`
6. **更新 pages.json 添加路由配置**

## 八、风险与注意事项

1. **中国市场颜色习惯**: 盈利为红色，亏损为绿色，与国际习惯相反
2. **数字格式化**: 使用 `toLocaleString('zh-CN')` 格式化金额
3. **字体引入**: 确保 Material Symbols 和 JetBrains Mono 字体正确加载
4. **响应式设计**: 确保在不同屏幕尺寸下正常显示