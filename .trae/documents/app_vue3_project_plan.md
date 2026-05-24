# SuiYou App 多平台前端项目实施计划

## 项目概述

在 `e:\Codes\SuiYou` 项目中创建一个名为 `app` 的新前端项目，使用 uni-app + Vue 3 技术栈，支持编译为以下四个平台：

1. **微信小程序** (mp-weixin)
2. **抖音小程序** (mp-toutiao)
3. **安卓应用** (Android App)
4. **iOS 应用** (iOS App)

## 技术选型

- **框架**: uni-app 3.x + Vue 3
- **构建工具**: Vite
- **状态管理**: Pinia
- **多语言**: vue-i18n
- **图表库**: uCharts (已有 qiun-data-charts)
- **样式预处理器**: SCSS/SASS

## 实施步骤

### 1. 项目目录结构搭建

```
app/
├── src/
│   ├── api/                    # API 请求封装
│   │   ├── request.js          # 请求拦截器
│   │   ├── response.js         # 响应拦截器
│   │   └── modules/            # 分模块 API
│   │       ├── auth.js
│   │       ├── user.js
│   │       ├── asset.js
│   │       ├── goal.js
│   │       └── portfolio.js
│   ├── components/            # 公共组件
│   │   ├── common/            # 通用组件
│   │   ├── form/               # 表单组件
│   │   └── business/          # 业务组件
│   ├── composables/           # 组合式函数
│   │   ├── useAuth.js
│   │   ├── usePlatform.js     # 平台检测
│   │   └── useStorage.js
│   ├── configs/               # 配置文件
│   │   ├── index.js
│   │   ├── api.js             # API 配置
│   │   └── constants.js       # 常量配置
│   ├── pages/                 # 页面文件
│   │   ├── home/              # 首页 Tab
│   │   ├── goals/             # 目标 Tab
│   │   ├── assets/            # 资产 Tab
│   │   └── profile/           # 我的 Tab
│   ├── services/              # 业务服务层
│   │   ├── authService.js
│   │   ├── assetService.js
│   │   ├── goalService.js
│   │   └── portfolioService.js
│   ├── static/                # 静态资源
│   │   ├── images/
│   │   ├── icons/
│   │   └── tabs/
│   ├── stores/                # Pinia 状态管理
│   │   ├── auth.js
│   │   ├── user.js
│   │   ├── asset.js
│   │   └── goal.js
│   ├── styles/               # 全局样式
│   │   ├── variables.scss     # 样式变量
│   │   ├── mixins.scss       # 混合宏
│   │   └── common.scss       # 公共样式
│   ├── utils/                 # 工具函数
│   │   ├── format.js          # 格式化工具
│   │   ├── validation.js     # 验证工具
│   │   └── platform.js       # 平台工具
│   ├── App.vue                # 应用入口
│   ├── main.js               # 主入口文件
│   ├── manifest.json          # 应用配置
│   ├── pages.json             # 页面路由配置
│   └── uni.scss              # uni-app 全局样式
├── package.json              # 项目依赖配置
├── vite.config.js            # Vite 构建配置
├── .gitignore                # Git 忽略文件
└── README.md                 # 项目说明文档
```

### 2. 配置文件设置

#### 2.1 package.json 配置

创建支持多平台的构建脚本：

```json
{
  "scripts": {
    "dev:mp-weixin": "uni -p mp-weixin",
    "dev:mp-toutiao": "uni -p mp-toutiao",
    "dev:app": "uni -p app",
    "dev:h5": "uni",
    "build:mp-weixin": "uni build -p mp-weixin",
    "build:mp-toutiao": "uni build -p mp-toutiao",
    "build:app": "uni build -p app",
    "build:h5": "uni build",
    "type-check": "vue-tsc --noEmit"
  }
}
```

#### 2.2 vite.config.js 配置

配置多平台构建和代理设置：

```javascript
import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'

export default defineConfig({
  plugins: [uni()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8000',
        changeOrigin: true,
        rewrite: (path) => path
      }
    }
  }
})
```

#### 2.3 manifest.json 配置

配置四个平台的特定设置：

- **微信小程序**: 配置 appid、权限、域名校验等
- **抖音小程序**: 配置 appid、权限等
- **Android**: 配置应用图标、权限、签名等
- **iOS**: 配置 Bundle ID、图标、权限描述等

### 3. 核心功能模块实现

#### 3.1 平台检测工具

创建 `src/utils/platform.js` 用于检测当前运行平台：

```javascript
export const usePlatform = () => {
  const platform = uni.getSystemInfoSync().platform

  const isWechat = platform === 'wxwork' || platform === 'weixin'
  const isDouyin = platform === 'ttworker' || platform === 'toutiao'
  const isAndroid = platform === 'android'
  const isIOS = platform === 'ios'
  const isH5 = platform === 'h5' || !platform

  return {
    platform,
    isWechat,
    isDouyin,
    isAndroid,
    isIOS,
    isH5,
    isApp: isAndroid || isIOS
  }
}
```

#### 3.2 API 请求封装

创建统一的请求处理，支持各平台的差异处理：

```javascript
// src/api/request.js
const BASE_URL = 'https://api.suiyou.com'

export const request = (options) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...getAuthHeader(), // 获取对应平台的认证信息
        ...options.header
      },
      success: (res) => {
        handleResponse(res, resolve, reject)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}
```

#### 3.3 平台适配组件

创建条件渲染组件，处理平台差异：

```vue
<!-- src/components/platform/PlatformShow.vue -->
<template>
  <view v-if="shouldShow">
    <slot />
  </view>
  <view v-else-if="$slots.fallback">
    <slot name="fallback" />
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { usePlatform } from '@/utils/platform'

const props = defineProps({
  platforms: {
    type: Array,
    default: () => ['wechat', 'douyin', 'android', 'ios', 'h5']
  }
})

const platform = usePlatform()

const shouldShow = computed(() => {
  if (props.platforms.includes('wechat') && platform.isWechat) return true
  if (props.platforms.includes('douyin') && platform.isDouyin) return true
  if (props.platforms.includes('android') && platform.isAndroid) return true
  if (props.platforms.includes('ios') && platform.isIOS) return true
  if (props.platforms.includes('h5') && platform.isH5) return true
  return false
})
</script>
```

### 4. 构建配置

#### 4.1 微信小程序配置

在 `manifest.json` 的 `mp-weixin` 部分：

```json
{
  "mp-weixin": {
    "appid": "wxYOUR_APPID",
    "setting": {
      "urlCheck": false,
      "ignoreDevUnusedFiles": false,
      "ignoreUploadUnusedFiles": false,
      "es6": true,
      "postcss": true,
      "minified": true
    },
    "usingComponents": true,
    "permission": {
      "scope.userLocation": {
        "desc": "获取您的位置信息用于记账定位"
      }
    },
    "requiredPrivateInfos": ["getLocation"]
  }
}
```

#### 4.2 抖音小程序配置

在 `manifest.json` 的 `mp-toutiao` 部分：

```json
{
  "mp-toutiao": {
    "appid": "ttYOUR_APPID",
    "setting": {
      "urlCheck": false,
      "es6": true,
      "minified": true
    },
    "usingComponents": true
  }
}
```

#### 4.3 Android 应用配置

在 `manifest.json` 的 `app-plus` 部分的 `distribute.android`：

```json
{
  "app-plus": {
    "distribute": {
      "android": {
        "permissions": [
          "<uses-permission android:name=\"android.permission.INTERNET\"/>",
          "<uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\"/>",
          "<uses-permission android:name=\"android.permission.ACCESS_WIFI_STATE\"/>",
          "<uses-permission android:name=\"android.permission.CAMERA\"/>",
          "<uses-permission android:name=\"android.permission.READ_PHONE_STATE\"/>",
          "<uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\"/>",
          "<uses-permission android:name=\"android.permission.READ_EXTERNAL_STORAGE\"/>"
        ],
        "abiFilters": ["armeabi-v7a", "arm64-v8a"],
        "minSdkVersion": 21,
        "targetSdkVersion": 33
      }
    }
  }
}
```

#### 4.4 iOS 应用配置

在 `manifest.json` 的 `app-plus` 部分的 `distribute.ios`：

```json
{
  "app-plus": {
    "distribute": {
      "ios": {
        "dSYMs": false,
        "idfa": false,
        "privacyDescription": {
          "NSPhotoLibraryUsageDescription": "需要访问您的相册以便上传图片",
          "NSCameraUsageDescription": "需要使用相机进行拍照",
          "NSLocationWhenInUseUsageDescription": "需要获取您的位置信息用于记账定位"
        }
      }
    }
  }
}
```

### 5. 构建命令

创建平台特定的构建脚本：

```bash
# 开发模式
npm run dev:mp-weixin    # 微信小程序开发
npm run dev:mp-toutiao   # 抖音小程序开发
npm run dev:app          # App 开发

# 生产构建
npm run build:mp-weixin  # 构建微信小程序
npm run build:mp-toutiao # 构建抖音小程序
npm run build:app        # 构建 App (Android/iOS)
```

### 6. 依赖安装

创建项目后需要安装的核心依赖：

```json
{
  "dependencies": {
    "@dcloudio/uni-app": "3.0.0-xxx",
    "@dcloudio/uni-app-plus": "3.0.0-xxx",
    "@dcloudio/uni-components": "3.0.0-xxx",
    "@dcloudio/uni-h5": "3.0.0-xxx",
    "@dcloudio/uni-mp-weixin": "3.0.0-xxx",
    "@dcloudio/uni-mp-toutiao": "3.0.0-xxx",
    "pinia": "^2.1.0",
    "vue": "^3.4.0",
    "vue-i18n": "^9.0.0",
    "dayjs": "^1.11.0"
  },
  "devDependencies": {
    "@dcloudio/types": "^3.4.0",
    "@dcloudio/vite-plugin-uni": "3.0.0-xxx",
    "sass": "^1.70.0",
    "vite": "^5.0.0"
  }
}
```

### 7. 注意事项

#### 7.1 平台差异处理

- **登录方式**: 微信使用 `uni.login()`，抖音使用 `tt.login()`，App 使用手机号/第三方登录
- **分享能力**: 各平台分享 API 不同，需要封装统一接口
- **支付能力**: 微信支付、抖音支付、支付宝/微信支付（App）
- **用户信息**: 各平台获取用户信息的方式不同

#### 7.2 样式兼容

- 使用 `uni.scss` 定义全局样式变量
- 注意各平台的单位转换（px、rpx、upx）
- 使用 flex 布局确保跨平台兼容性

#### 7.3 权限申请

- 小程序需要在 `manifest.json` 配置权限说明
- App 需要在 `manifest.json` 配置 `privacyDescription`
- 敏感权限需要在代码中动态申请

## 实施检查清单

- [ ] 创建项目目录结构
- [ ] 配置 package.json 和构建脚本
- [ ] 配置 vite.config.js
- [ ] 配置 manifest.json 多平台设置
- [ ] 创建 pages.json 路由配置
- [ ] 实现平台检测工具
- [ ] 实现 API 请求封装
- [ ] 创建公共组件库
- [ ] 实现基础页面框架
- [ ] 配置各平台构建命令
- [ ] 测试各平台编译和运行

## 下一步行动

1. 在 `e:\Codes\SuiYou\app` 目录下初始化项目结构
2. 配置 package.json 和必要配置文件
3. 创建核心工具和组件
4. 实现基础页面框架
5. 配置并测试各平台构建
