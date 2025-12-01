import { createApp } from 'vue'
import App from './App.vue'

const app = createApp(App)
app.mount('#app')
import { createSSRApp } from 'vue'
import App from './App.vue'

export function createApp() {
  const app = createSSRApp(App)
  return { app }
}
