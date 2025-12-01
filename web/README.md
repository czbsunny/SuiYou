# web（Vite + uni 占位项目）

这是一个基于 Vite + Vue3 的占位模版，并包含 `@dcloudio/vite-plugin-uni` 的示例配置，便于后续构建 `H5` 与 `mp-weixin` 平台。

快速开始（需 Node.js 与 npm/pnpm/yarn）：

1. 安装依赖：

```bash
cd web
npm install
```

2. 启动开发服务器（Vite）：

```bash
npm run dev
```

3. 构建 H5：

```bash
npm run build:h5
```

注意：构建 `mp-weixin` 通常需要 uni 的官方工具链（如 HBuilderX 或 `@dcloudio/uni-cli`），此仓库内的脚本为占位，实际构建小程序请使用 uni 官方流程。

文件说明：
- `index.html`：Vite 入口
- `src/`：Vue + 页面文件
- `vite.config.js`：Vite 配置，包含 `vite-plugin-uni` 占位
- `manifest.json`：uni 平台配置（占位）
