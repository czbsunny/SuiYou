import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni(), vue()],
  server: {
    port: 5173
  }
})
