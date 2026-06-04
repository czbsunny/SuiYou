# Token 鉴权和 API 调用完善计划

## 一、需求分析

参考 web 端的 token 鉴权和 API 调用实现，完善 app 端（小程序）的相关功能：

1. **web 端已实现的功能：**
   - 统一的 API 请求封装（apiService.js）
   - Pinia 状态管理（auth.js）
   - 登录锁机制（防止并发登录）
   - 401 自动静默登录重试
   - 微信登录全链路流程

2. **app 端缺失/需要修改的内容：**
   - 缺少 `stores/auth.js` 文件
   - 微信登录接口路径不一致
   - 请求拦截器调用了不存在的方法

## 二、修改方案

### 2.1 创建 stores/auth.js

参考 web 端实现，创建完整的 auth store：
- 状态：token、userInfo、isLoggingIn、loginPromise
- Getters：isLoggedIn、displayUserInfo
- Actions：login（微信静默登录）、silentLogin、setAuthData、updateUserInfo、logout

### 2.2 更新 api/modules/auth.js

修改微信登录接口路径，保持与 web 端一致：
- 将 `/api/auth/wechat` 修改为 `/api/auth/wechat-login`

### 2.3 更新 api/request.js

完善请求拦截逻辑：
- 确保登录锁等待机制正确
- 修改 `authStore.clearAuth()` 为 `authStore.logout()`

## 三、文件修改清单

| 文件路径 | 修改类型 | 说明 |
| :--- | :--- | :--- |
| `src/stores/auth.js` | 新建 | 创建完整的 auth store |
| `src/api/modules/auth.js` | 修改 | 修改微信登录接口路径 |
| `src/api/request.js` | 修改 | 修复 authStore 方法调用 |

## 四、风险与注意事项

1. **小程序环境**：确保使用 `uni.login({ provider: 'weixin' })` 正确获取微信 code
2. **登录状态持久化**：使用 `uni.setStorageSync` 保存 token 和用户信息
3. **并发登录控制**：通过 `isLoggingIn` 和 `loginPromise` 防止重复登录请求
4. **401 重试机制**：token 失效时自动尝试静默登录，失败后清空本地存储