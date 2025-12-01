import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    uni(),
  ],
  server: {
    port: 3000,
    // 配置代理，解决跨域问题
    proxy: {
      '/api': {
        target: 'https://www.zhitouying.cn',
        changeOrigin: true,
        rewrite: (path) => path
      }
    }
  }
})
