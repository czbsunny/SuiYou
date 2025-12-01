<script>
export default {
  globalData: {},
  onLaunch: function() {
    console.log('App Launch')
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  }
}
</script>

<style lang="scss" scope>
/* ========================================== */
/* 全局滚动条隐藏 (H5/App/小程序)             */
/* ========================================== */

/* Webkit内核 (Chrome, Safari, 新版小程序, H5) */
::-webkit-scrollbar {
  width: 0;
  height: 0;
  background: transparent;
  display: none; /* 强制隐藏 */
}

/* ========================================== */
/* 全局基础样式                              */
/* ========================================== */

/* 注意：在 UniApp 中使用 'page' 选择器来控制全局背景和高度 */
page {
  width: 100%;
  height: 100%;
  background-color: $bg-page; /* 这里对应你之前的 bg-page 颜色 */
  overflow-x: hidden;
  
  /* 字体优化 (可选) */
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* Vue 路由过渡动画 (如果你的页面组件中有用到 transition) */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>