# server（Java 17 最小示例服务）

这是一个使用 Java 17 + Spring Boot 的示例服务，包含微信小程序登录/注册与手机号登录（仅登陆，不提供手机号注册）。

构建与运行：

```bash
cd server
# 使用 Maven 构建
mvn package
# 运行
java -jar target/server-1.0.0.jar
```

接口说明（示例）：

- `POST /auth/wx-login`：微信小程序登录与注册接口（若 openid 不存在则自动注册）。请求体示例：

```json
{ "code": "小程序登录时获取的 code", "nickname":"昵称", "avatar":"头像url" }
```

- `POST /auth/phone-login`：手机号登录（只允许已存在的手机号登录，不支持注册）。请求体示例：

```json
{ "phone": "13800000000" }
```

说明与配置：

- `server/src/main/resources/application.properties` 包含 `wechat.appid` 与 `wechat.secret` 配置；如果未配置或 `wechat.mock=true`（默认），服务器会返回一个模拟 `openid`，方便本地开发与微信公众平台审核。若要对接真是微信服务，请填入 `appid` 与 `secret` 并将 `wechat.mock` 设为 `false`。

数据存储：示例使用内存仓库（仅用于演示）。生产环境应替换为数据库实现并加入鉴权（JWT/Redis 会话等）、验证码、HTTPS、日志和安全校验。
