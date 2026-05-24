# SuiYou App 快速入门指南

## 🎯 快速开始

### 第一步：安装依赖

```bash
cd e:\Codes\SuiYou\app
npm install
```

### 第二步：配置平台 AppID

打开 `src/manifest.json`，替换以下占位符：

```json
{
  "mp-weixin": {
    "appid": "替换为你的微信小程序 AppID"
  },
  "mp-toutiao": {
    "appid": "替换为你的抖音小程序 AppID"
  }
}
```

### 第三步：启动开发服务器

**微信小程序：**
```bash
npm run dev:mp-weixin
```

**抖音小程序：**
```bash
npm run dev:mp-toutiao
```

**App（Android/iOS）：**
```bash
npm run dev:app
```

**H5：**
```bash
npm run dev:h5
```

## 📁 目录结构说明

```
app/
├── src/
│   ├── api/          # API 接口定义
│   ├── components/   # Vue 组件
│   ├── configs/     # 配置文件
│   ├── pages/       # 页面文件
│   ├── static/      # 静态资源
│   ├── stores/      # 状态管理
│   ├── styles/      # 样式文件
│   ├── utils/       # 工具函数
│   ├── App.vue      # 应用入口
│   ├── main.js      # 主入口
│   ├── manifest.json # 应用配置
│   ├── pages.json   # 页面路由
│   └── uni.scss    # 全局样式变量
```

## 🛠️ 开发指南

### 创建新页面

1. 在 `src/pages/` 下创建页面文件夹
2. 在 `pages.json` 中注册页面路由
3. 编写页面组件

**示例：创建 "关于" 页面**

```bash
# 1. 创建页面文件
# src/pages/about/index.vue

<template>
  <view class="about-page">
    <text>关于我们</text>
  </view>
</template>

<script setup>
// 页面逻辑
</script>

<style lang="scss" scoped>
.about-page {
  padding: 40rpx;
}
</style>
```

```json
// 2. 在 pages.json 中注册
{
  "pages": [
    // ... 其他页面
    {
      "path": "pages/about/index",
      "style": {
        "navigationBarTitleText": "关于我们"
      }
    }
  ]
}
```

### 创建新组件

在 `src/components/` 下创建组件：

```bash
# src/components/common/MyComponent.vue

<template>
  <view class="my-component">
    <slot></slot>
  </view>
</template>

<script setup>
defineProps({
  title: String
})
</script>

<style lang="scss" scoped>
.my-component {
  padding: 20rpx;
}
</style>
```

使用组件：

```vue
<template>
  <MyComponent title="示例">
    <text>组件内容</text>
  </MyComponent>
</template>

<script setup>
import MyComponent from '@/components/common/MyComponent.vue'
</script>
```

### 调用 API

```javascript
import { getAssetList, createAsset } from '@/api/modules/asset'

// 获取资产列表
const assets = await getAssetList({ page: 1, pageSize: 10 })

// 创建资产
await createAsset({
  name: '测试资产',
  amount: 1000
})
```

### 使用状态管理

```javascript
import { useAssetStore } from '@/stores/asset'

const assetStore = useAssetStore()

// 获取状态
const assets = assetStore.assetList

// 调用 actions
await assetStore.fetchAssetList()
```

### 平台检测

```javascript
import { usePlatform } from '@/utils/platform'

const { isWechat, isDouyin, isAndroid, isIOS } = usePlatform()

if (isWechat) {
  // 微信小程序特定逻辑
  uni.login({
    success: (res) => {
      console.log('微信登录成功', res.code)
    }
  })
}
```

### 平台条件渲染

```vue
<template>
  <view>
    <!-- 所有平台都显示 -->
    <AllPlatformContent />

    <!-- 仅微信和抖音显示 -->
    <PlatformShow :platforms="['wechat', 'douyin']">
      <WechatAndDouyinContent />
    </PlatformShow>

    <!-- 仅 App 显示 -->
    <PlatformShow :platforms="['android', 'ios']">
      <AppOnlyContent />
    </PlatformShow>
  </view>
</template>

<script setup>
import PlatformShow from '@/components/platform/PlatformShow.vue'
</script>
```

## 🎨 样式指南

### 使用样式变量

```scss
@import '@/styles/variables.scss';

.my-component {
  color: $text-color;
  background: $background-color;
  padding: $spacing-base;
  border-radius: $border-radius-base;
}
```

### 使用混合宏

```scss
@import '@/styles/mixins.scss';

.my-component {
  @include flex-center;
  @include card;
}

.another-component {
  @include flex-between;

  &:active {
    opacity: 0.8;
  }
}
```

## 📱 页面导航

### 跳转到 TabBar 页面
```javascript
uni.switchTab({ url: '/pages/home/index' })
```

### 跳转到普通页面
```javascript
uni.navigateTo({ url: '/pages/about/index' })
```

### 返回上一页
```javascript
uni.navigateBack()
```

### 重定向到页面
```javascript
uni.redirectTo({ url: '/pages/login/index' })
```

## 🔧 工具函数

### 格式化金额
```javascript
import { formatNumber, formatCurrency } from '@/utils/format'

formatNumber(12345.67)      // "12345.67"
formatCurrency(12345.67)    // "¥12,345.67"
```

### 格式化日期
```javascript
import { formatDate, formatTime } from '@/utils/format'

formatDate(new Date())                      // "2026-05-24"
formatDate(new Date(), 'YYYY年MM月DD日')   // "2026年05月24日"
formatTime(Date.now() - 3600000)           // "1小时前"
```

### 表单验证
```javascript
import { validatePhone, validateEmail } from '@/utils/validation'

validatePhone('13800138000')  // true
validateEmail('test@test.com') // true
```

## 🐛 调试技巧

### 开启 Vue DevTools
在 `manifest.json` 中配置：
```json
{
  "h5": {
    "devServer": {
      "https": false
    }
  }
}
```

### 查看平台信息
```javascript
uni.getSystemInfo({
  success: (info) => {
    console.log('平台信息:', info)
  }
})
```

## 📦 构建发布

### 微信小程序
```bash
npm run build:mp-weixin
# 输出目录: dist/build/mp-weixin
```

### 抖音小程序
```bash
npm run build:mp-toutiao
# 输出目录: dist/build/mp-toutiao
```

### App（Android/iOS）
```bash
npm run build:app
# 输出目录: dist/build/app
```

## ❓ 常见问题

### 1. 安装依赖失败
```bash
# 清理缓存后重试
rm -rf node_modules package-lock.json
npm install
```

### 2. 构建失败
- 检查 `manifest.json` 配置
- 确保 AppID 正确
- 查看终端错误信息

### 3. 页面不显示
- 检查 `pages.json` 路由配置
- 确保页面文件路径正确
- 检查控制台错误信息

## 📚 相关资源

- [uni-app 官方文档](https://uniapp.dcloud.net.cn/)
- [Vue 3 文档](https://vuejs.org/)
- [Pinia 状态管理](https://pinia.vuejs.org/)
- [SCSS 文档](https://sass-lang.com/)

---

**祝开发愉快！ 🚀**
