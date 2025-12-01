# SuiYou

本仓库包含：

- `web/`：前端（UniApp 占位项目），目标平台：`mp-weixin` 与 `H5`。
- `server/`：后端（Java 17 最小示例），基于 JDK 自带 `HttpServer`，无需外部依赖。

快速使用：

- 前端：进入 `web/`，推荐使用 `npm init @dcloudio/uni-app` 初始化完整 UniApp 项目，随后运行构建脚本生成 H5 或微信小程序。
- 后端：进入 `server/`，运行 `mvn package` 然后 `java -jar target/server-1.0.0.jar`。

已更新：

- `web/` 现在为 Vite + Vue3 的占位模版，包含 `vite.config.js`、`index.html` 与 `src/`，并演示如何集成 `@dcloudio/vite-plugin-uni`。要运行：在 `web/` 中执行 `npm install` 然后 `npm run dev`。
- `server/` 已切换为 Spring Boot 模板（Java 17），使用 `spring-boot-starter-web`，入口为 `com.suiyou.Application`，包含简单的 `HelloController`（`/` 与 `/health`）。构建与运行：在 `server/` 中运行 `mvn package` 然后 `java -jar target/server-1.0.0.jar`。