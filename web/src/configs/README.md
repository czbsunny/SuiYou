# 配置管理

本目录用于集中管理前端应用的所有配置信息，采用模块化结构，便于维护和扩展。

## 目录结构

```
configs/
├── index.js          # 配置入口文件，统一导出所有配置
├── common.js         # 通用配置（时间格式化、分页、网络等）
├── assets.js         # 资产模块配置
├── portfolio.js      # 组合模块配置
├── goals.js          # 目标模块配置
├── profile.js        # 个人资料模块配置
└── README.md         # 配置说明文档
```

## 使用方法

### 1. 导入单个模块配置

```javascript
// 导入资产模块配置
import { ASSET_TYPES, ASSET_TYPE_DISPLAY } from '@/configs/assets';

// 使用配置
console.log(ASSET_TYPES.CASH); // 'CASH'
console.log(ASSET_TYPE_DISPLAY[ASSET_TYPES.CASH].name); // '现金'
```

### 2. 导入所有配置

```javascript
// 导入所有配置
import * as configs from '@/configs';

// 使用配置
console.log(configs.assets.ASSET_TYPES.CASH); // 'CASH'
console.log(configs.portfolio.RiskLevel.CONSERVATIVE); // 'CONSERVATIVE'
```

## 扩展配置

### 添加新的模块配置

1. 在configs目录下创建新的配置文件，例如 `newModule.js`
2. 在新文件中定义配置常量
3. 在 `index.js` 中导出新模块

### 示例：添加新模块

```javascript
// newModule.js
export const NEW_MODULE_CONFIG = {
  key1: 'value1',
  key2: 'value2'
};

// index.js
import * as common from './common';
import * as assets from './assets';
import * as portfolio from './portfolio';
import * as goals from './goals';
import * as profile from './profile';
import * as newModule from './newModule'; // 导入新模块

export {
  common,
  assets,
  portfolio,
  goals,
  profile,
  newModule // 导出新模块
};
```

## 配置类型

### 1. 常量配置

用于定义固定不变的值，如枚举类型：

```javascript
export const ASSET_TYPES = {
  CASH: 'CASH',
  STOCK: 'STOCK',
  FUND: 'FUND'
};
```

### 2. 显示配置

用于定义UI显示相关的配置：

```javascript
export const ASSET_TYPE_DISPLAY = {
  [ASSET_TYPES.CASH]: {
    name: '现金',
    icon: '/static/icons/cash.png',
    color: '#2a806c'
  }
};
```

### 3. 功能配置

用于定义业务逻辑相关的配置：

```javascript
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 10,
  PAGE_SIZE_OPTIONS: [10, 20, 50, 100]
};
```

## 注意事项

1. 配置文件中只存放静态配置，不包含业务逻辑
2. 配置值应尽量使用常量，避免硬编码
3. 模块配置应与功能模块一一对应
4. 新增配置时请遵循现有命名规范
5. 对于需要动态计算的配置，建议使用 composables

## 向后兼容

为了保持向后兼容，原有的 `constants.js` 文件已更新为从本目录导入配置。建议新代码直接使用 `@/configs` 导入配置。