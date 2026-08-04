# Pages 代码组织重构计划

## 1. 现状分析

### 当前目录结构
```
src/pages/
├── accounts/
│   ├── detail-alipay/index.vue
│   ├── detail-bank/index.vue
│   ├── detail-securities/index.vue
│   ├── detail-wechat/index.vue
│   ├── stock-buy/index.vue
│   ├── stock-sell/index.vue
│   ├── transaction-query/index.vue
│   ├── transfer/index.vue
│   ├── add-account.vue
│   └── search-institution.vue
├── assets/index.vue
├── goals/index.vue
├── home/index.vue
└── profile/index.vue
```

### 当前 pages.json 配置
- Tabbar 页面: home, goals, assets, profile
- 子页面路径使用 `pages/accounts/...` 格式

## 2. 目标结构

按照领域驱动设计原则，将页面按业务领域重新组织：

```
src/pages/
├── asset/                     # 资产域
│   ├── index.vue              # 资产首页 (Tabbar)
│   ├── account/               # 账户子域
│   │   ├── bank.vue           # 银行账户详情页
│   │   ├── alipay.vue         # 支付宝账户详情页
│   │   ├── wechat.vue         # 微信账户详情页
│   │   └── securities.vue     # 证券账户详情页
│   └── holding/               # 持仓子域
│       ├── money.vue          # 活期/现金详情页
│       └── fund.vue           # 基金详情页
├── trade/                     # 交易域
│   ├── stock.vue              # 股票买卖页 (合并buy/sell)
│   └── transfer.vue           # 银证转账页
├── goal/                      # 目标域
│   └── index.vue              # 目标页
├── home/                      # 首页
│   └── index.vue
└── profile/                   # 个人中心
    └── index.vue
```

## 3. 文件迁移映射表

| 源文件 | 目标文件 | 状态 |
|--------|----------|------|
| `pages/assets/index.vue` | `pages/asset/index.vue` | 重命名迁移 |
| `pages/accounts/detail-bank/index.vue` | `pages/asset/account/bank.vue` | 迁移并简化路径 |
| `pages/accounts/detail-alipay/index.vue` | `pages/asset/account/alipay.vue` | 迁移并简化路径 |
| `pages/accounts/detail-wechat/index.vue` | `pages/asset/account/wechat.vue` | 迁移并简化路径 |
| `pages/accounts/detail-securities/index.vue` | `pages/asset/account/securities.vue` | 迁移并简化路径 |
| `pages/accounts/stock-buy/index.vue` | `pages/trade/stock.vue` | 合并迁移 |
| `pages/accounts/stock-sell/index.vue` | `pages/trade/stock.vue` | 合并迁移 |
| `pages/accounts/transfer/index.vue` | `pages/trade/transfer.vue` | 迁移 |
| `pages/accounts/transaction-query/index.vue` | `pages/trade/query.vue` | 迁移 |
| `pages/goals/index.vue` | `pages/goal/index.vue` | 重命名目录 |
| `pages/accounts/add-account.vue` | `pages/asset/add-account.vue` | 迁移 |
| `pages/accounts/search-institution.vue` | `pages/asset/search-institution.vue` | 迁移 |

## 4. 实施步骤

### 步骤 1: 创建新目录结构
- 创建 `src/pages/asset/`
- 创建 `src/pages/asset/account/`
- 创建 `src/pages/asset/holding/`
- 创建 `src/pages/trade/`

### 步骤 2: 迁移资产域文件
- 重命名 `assets/index.vue` → `asset/index.vue`
- 移动账户详情页到 `asset/account/` 目录
- 移动 add-account.vue 和 search-institution.vue 到 `asset/` 目录

### 步骤 3: 迁移交易域文件
- 合并 `stock-buy/index.vue` 和 `stock-sell/index.vue` → `trade/stock.vue`
- 移动 `transfer/index.vue` → `trade/transfer.vue`
- 移动 `transaction-query/index.vue` → `trade/query.vue`

### 步骤 4: 重命名 goals → goal
- 重命名目录 `goals/` → `goal/`

### 步骤 5: 更新 pages.json
- 更新所有页面路径引用
- 确保 tabBar 配置正确

### 步骤 6: 清理旧目录
- 删除 `accounts/` 目录
- 删除旧的 `assets/` 目录

## 5. 风险与注意事项

### 5.1 风险识别
| 风险 | 描述 | 影响 | 应对措施 |
|------|------|------|----------|
| 路径引用错误 | 页面跳转路径未更新 | 页面无法打开 | 全局搜索并替换路径引用 |
| 静态资源引用 | 组件内静态资源路径引用 | 资源加载失败 | 检查组件模板和样式 |
| tabBar 配置 | 首页路径变化 | Tabbar 无法正常工作 | 重点检查 pages.json |

### 5.2 需要同步更新的文件
- `pages.json` - 所有页面路径配置
- 组件内的 `navigateTo` / `redirectTo` 等跳转路径
- 任何硬编码的页面路径引用

## 6. 验证检查清单

- [ ] 新目录结构创建完成
- [ ] 所有页面文件迁移完成
- [ ] pages.json 更新完成
- [ ] tabBar 配置正确
- [ ] 项目能正常构建
- [ ] 所有页面能正常访问
- [ ] 旧目录已清理