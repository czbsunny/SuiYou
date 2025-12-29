<script setup>
import { onLaunch } from '@dcloudio/uni-app'
import { useConfigStore } from '@/stores/config.js'

const configStore = useConfigStore()

onLaunch(async () => {
  console.log('App Launch')

  // 全局静默加载配置
  await configStore.initConfigs()
})
</script>

<style lang="scss">

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
}

/* UniApp页面基础样式 */
page {
  width: 100%;
  height: 100%;
  background-color: $cream;
  overflow-x: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 基础样式 */
body {
  margin: 0;
  padding: 0;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>