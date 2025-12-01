<script setup>
import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('App Launch')
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