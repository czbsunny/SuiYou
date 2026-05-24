# SuiYou App

岁有长赢 - 多平台资产管理应用

## 技术栈

- **框架**: uni-app 3.x + Vue 3
- **构建工具**: Vite
- **状态管理**: Pinia
- **样式**: SCSS/SASS
- **多语言**: vue-i18n

## 支持平台

- ✅ 微信小程序 (mp-weixin)
- ✅ 抖音小程序 (mp-toutiao)
- ✅ Android 应用 (Android App)
- ✅ iOS 应用 (iOS App)
- ✅ H5 Web 应用

## 快速开始

### 安装依赖

```bash
cd app
npm install
```

### 开发模式

```bash
# 微信小程序开发
npm run dev:mp-weixin

# 抖音小程序开发
npm run dev:mp-toutiao

# App 开发
npm run dev:app

# H5 开发
npm run dev:h5
```

### 生产构建

```bash
# 构建微信小程序
npm run build:mp-weixin

# 构建抖音小程序
npm run build:mp-toutiao

# 构建 App (Android/iOS)
npm run build:app

# 构建 H5
npm run build:h5
```

## 项目结构

```
app/
├── src/
│   ├── api/                 # API 请求封装
│   │   ├── request.js      # 请求拦截器
│   │   └── modules/        # 分模块 API
│   ├── components/         # 公共组件
│   │   ├── common/        # 通用组件
│   │   └── platform/      # 平台适配组件
│   ├── configs/           # 配置文件
│   ├── pages/              # 页面文件
│   │   ├── home/          # 首页 Tab
│   │   ├── goals/         # 目标 Tab
│   │   ├── assets/        # 资产 Tab
│   │   └── profile/       # 我的 Tab
│   ├── services/          # 业务服务层
│   ├── static/            # 静态资源
│   ├── stores/            # Pinia 状态管理
│   ├── styles/            # 全局样式
│   │   ├── variables.scss # 样式变量
│   │   ├── mixins.scss    # 混合宏
│   │   └── common.scss    # 公共样式
│   ├── utils/             # 工具函数
│   │   ├── format.js      # 格式化工具
│   │   ├── platform.js    # 平台检测
│   │   └── validation.js  # 验证工具
│   ├── App.vue            # 应用入口
│   ├── main.js           # 主入口文件
│   ├── manifest.json      # 应用配置
│   ├── pages.json         # 页面路由配置
│   └── uni.scss          # uni-app 全局样式
├── package.json          # 项目依赖配置
├── vite.config.js        # Vite 构建配置
└── .gitignore           # Git 忽略文件
```

## 功能特性

- 多平台资产管理
- 收支记录与统计
- 理财目标设定与跟踪
- 资产组合管理
- 基金搜索与分析
- 多平台适配
- 响应式设计

## 平台配置

### 微信小程序

在 `manifest.json` 中配置微信小程序的 appid：

```json
{
  "mp-weixin": {
    "appid": "your-wechat-appid"
  }
}
```

### 抖音小程序

在 `manifest.json` 中配置抖音小程序的 appid：

```json
{
  "mp-toutiao": {
    "appid": "your-douyin-appid"
  }
}
```

### App 应用

App 的构建需要配置应用图标、启动图等资源，并进行签名配置。

## 开发指南

### 平台检测

使用 `usePlatform` 工具检测当前运行平台：

```javascript
import { usePlatform } from '@/utils/platform'

const { isWechat, isDouyin, isAndroid, isIOS } = usePlatform()
```

### 平台适配组件

使用 `PlatformShow` 组件进行条件渲染：

```vue
<PlatformShow :platforms="['wechat', 'douyin']">
  <view>仅在微信和抖音小程序显示</view>
</PlatformShow>
```

### API 请求

使用封装好的请求方法：

```javascript
import { get, post } from '@/api/request'

// GET 请求
const data = await get('/api/assets')

// POST 请求
const result = await post('/api/assets', { name: 'test' })
```

## 注意事项

1. **跨平台兼容性**: 注意各平台的 API 差异，使用条件编译或平台检测
2. **登录方式**: 各平台登录方式不同，需要分别实现
3. **支付能力**: 支付功能需要各平台分别申请权限
4. **权限申请**: 敏感权限需要在 manifest.json 中声明

## License

MIT
