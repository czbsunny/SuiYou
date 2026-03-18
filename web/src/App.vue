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
  background-color: $bg-page;
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

view, text, scroll-view, input, button {
  box-sizing: border-box;
}

/* --- 2. 核心原子组件样式 --- */

/* 主品牌按钮（柿子橙渐变） */
.btn-primary {
  width: 100%;
  height: 96rpx;
  @include flex-center;
  background: linear-gradient(to right, $primary, $primary-pressed);
  color: #FFFFFF;
  border-radius: $radius-base;
  font-weight: $fw-semibold;
  font-size: 32rpx;
  box-shadow: $shadow-button;
  transition: all 0.2s ease;

  &::after { border: none; }
  
  &:active {
    transform: translateY(2rpx);
    filter: brightness(0.95);
  }

  /* 禁用态样式 */
  &[disabled], &.btn-disabled {
    background: $btn-disabled-bg !important;
    color: $btn-disabled-text !important;
    box-shadow: none !important;
    transform: none !important;
  }
}

/* 高级感卡片容器 */
.card-warm {
  background-color: $bg-white;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  padding: $spacing-md;
  margin-bottom: $spacing-base;
}

/* --- 3. 金融功能类 --- */

/* 专用数字字体（DIN + 等宽） */
.num-font {
  font-family: $font-family-money;
  @include tabular-nums;
}

/* 涨跌语义色 */
.text-up { color: $color-gain !important; }
.text-down { color: $color-loss !important; }

/* 占位符全局样式 */
.placeholder-style {
  color: $text-placeholder;
}

/* --- 4. 常用布局工具 --- */
.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
}

</style>