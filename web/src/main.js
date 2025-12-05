import { createSSRApp } from 'vue'
import App from './App.vue'
import configService from './services/configService'

export function createApp() {
  const app = createSSRApp(App)
  
  // 初始化配置服务
  configService.getConfig().then(() => {
    console.log('配置初始化完成')
  }).catch(error => {
    console.error('配置初始化失败:', error)
  })
  
  // 将配置服务挂载到全局，方便组件使用
  app.config.globalProperties.$configService = configService
  
  return {
    app
  }
}