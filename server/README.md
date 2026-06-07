# SuiYou Server (v2)

极简后端服务，第一阶段仅提供鉴权（注册 / 登录 / 微信登录 / JWT / 用户信息），其他业务模块后续逐步迁入。

## 技术栈

- Java 17
- Spring Boot 3.2.0
- Spring Security + JWT (jjwt 0.11.5)
- Spring Data JPA + MySQL
- Lombok / Validation

## 运行

```bash
./build.sh
./start.sh
```

Windows:

```bat
build.bat
start.bat
```

默认端口 8080，context-path `/api`。

## 健康检查

```bash
curl http://localhost:8080/api/health
```

## 鉴权接口

| Method | Path | 说明 | 鉴权 |
|--------|------|------|------|
| POST | /api/auth/register | 手机号注册 | 否 |
| POST | /api/auth/login | 手机号密码登录 | 否 |
| POST | /api/auth/wechat-login | 微信小程序登录 | 否 |
| GET  | /api/auth/me | 当前用户信息 | 是 |
| PUT  | /api/auth/me | 更新昵称 | 是 |

Bearer Token 放在 `Authorization: Bearer <jwt>`。
