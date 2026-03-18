<template>
  <view class="asset-summary-wrapper">
    <!-- 核心资产卡片 -->
    <view class="summary-card card-warm">
      <view class="asset-profit-container">
        <!-- 资产和收益部分 -->
        <view class="asset-stats-container">
          <!-- 左侧：总资产 -->
          <view class="left-section">
            <view class="label-group" @click="toggleAssetVisibility" hover-class="hover-opacity">
              <text class="label-text">总资产 (元)</text>
              <view class="icon-wrapper">
                <image 
                  :src="isAssetHidden ? '/static/images/eye-close.png' : '/static/images/eye-open.png'" 
                  mode="aspectFit" 
                  class="eye-icon" 
                  :class="{ 'is-hidden': isAssetHidden }"
                ></image>
              </view>
            </view>
            
            <view class="value-group">
              <text class="assets-value num-font" :class="{ 'is-hidden': isAssetHidden }">
                {{ isAssetHidden ? '******' : formatCurrency(asset || 0) }}
              </text>
            </view>
          </view>
          
          <!-- 右侧 预估收益 -->
          <view class="right-section">
            <view class="label-group" @click="toggleReturnDisplay" hover-class="hover-opacity">
              <view class="profit-toggle">
                <image 
                  src="/static/images/switch.png" 
                  mode="aspectFit" 
                  class="switch-icon"
                ></image>
              </view>
              <text class="label-text">预估{{ showReturnRate ? '收益率' : '总收益' }}</text>
            </view>
            
            <view class="value-group">
              <text 
                class="profit-value num-font" 
                :class="[getProfitClass]"
              >
                {{ getProfitPrefix }}{{ showReturnRate ? formatPercent(dailyReturnRate) : formatCurrency(dailyReturn || 0) }}
              </text>
            </view>
          </view>
        </view>
        
      </view>
    </view>
  </view>
</template>

<script>
import { formatCurrency, formatPercentage } from '@/utils/formatUtil';

export default {
  name: 'AssetSummary',
  props: {
    asset: { type: Number, default: 0 },
    dailyReturn: { type: Number, default: 0 },
    dailyReturnRate: { type: Number, default: 0 }
  },
  data() {
    return {
      showReturnRate: false,
      isAssetHidden: false
    }
  },
  computed: {
    getProfitClass() {
      const val = this.showReturnRate ? this.dailyReturnRate : this.dailyReturn;
      if (val > 0) return 'text-up';
      if (val < 0) return 'text-down';
      return 'text-main';
    },
    getProfitPrefix() {
      const val = this.showReturnRate ? this.dailyReturnRate : this.dailyReturn;
      return val > 0 ? '+' : '';
    }
  },
  methods: {
    formatCurrency(value) {
      return formatCurrency(value, '');
    },
    formatPercent(value) {
      return formatPercentage(value);
    },
    toggleReturnDisplay() {
      this.showReturnRate = !this.showReturnRate;
      uni.vibrateShort(); // 触感反馈
    },
    toggleAssetVisibility() {
      this.isAssetHidden = !this.isAssetHidden;
      this.$emit('assetVisibilityChanged', this.isAssetHidden);
      uni.vibrateShort();
    }
  }
}
</script>

<style lang="scss" scoped>
.asset-summary-wrapper {
  padding: $spacing-xs $spacing-xs 0; 
  background-color: transparent;
}

@keyframes scroll {
  0% {
    transform: translateX(100%);
  }
  100% {
    transform: translateX(-100%);
  }
}

.summary-card {
  /* 使用 App.vue 定义的 .card-warm */
  padding: 20rpx 40rpx 50rpx; // 减少顶部内
  position: relative;
  overflow: hidden;
  
  // 品牌色装饰光晕
  &::before {
    content: '';
    position: absolute;
    top: -60rpx;
    right: -60rpx;
    width: 240rpx;
    height: 240rpx;
    background: radial-gradient(circle, rgba(#2a806c, 0.06) 0%, transparent 70%);
    pointer-events: none;
  }
}

.asset-profit-container {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.asset-stats-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.left-section, .right-section {
  display: flex;
  flex-direction: column;
}

.right-section {
  align-items: flex-end;
}

.label-group {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  padding: 4rpx; // 增加内边距，扩大点击区域
  border-radius: 8rpx; // 添加圆角，使点击区域更明显
  
  &:active { 
    opacity: 0.7;
    .switch-icon {
      transform: rotate(180deg);
    }
  }
}

.profit-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8rpx; // 增加内边距，扩大点击区域
  margin: -8rpx 8rpx -8rpx -8rpx; // 负边距抵消内边距对布局的影响
}

.switch-icon {
  width: 32rpx; // 增大图标尺寸
  height: 32rpx; // 增大图标尺寸

  transition: transform 0.3s ease;
}

.hover-opacity {
  transition: opacity 0.2s ease;
  
  &:hover {
    opacity: 0.7;
  }
}

// 响应式调整
.label-text {
  font-size: 28rpx;
  color: $text-placeholder;
  font-weight: $fw-regular;
}

.icon-wrapper {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 8rpx; // 增加内边距，扩大点击区域
  margin: -8rpx 8rpx -8rpx 16rpx; // 负边距抵消内边距对布局的影响，同时保留与文字的间距
  
  .eye-icon {
    width: 32rpx;
    height: 32rpx;
    opacity: 0.5;
    transition: all 0.3s ease;
    
    &.is-hidden {
      opacity: 0.2;
      transform: scale(0.9) rotate(360deg);
    }
  }
}

.value-group {
  min-height: 64rpx;
  display: flex;
  align-items: baseline;
}

.assets-value {
  font-size: 52rpx; // 略微放大字号，增强冲击力
  font-weight: $fw-semibold;
  color: $text-main;
  
  &.is-hidden {
    letter-spacing: 4rpx;
    font-size: 36rpx;
    color: $text-inverse;
  }
}

.profit-value {
  font-size: 44rpx; 
  font-weight: $fw-medium;
  
  .unit {
    font-size: 24rpx;
    margin-left: 4rpx;
  }
}

/* 响应式微调 */
@media (max-width: 350px) {
  .assets-value { font-size: 44rpx; }
  .profit-value { font-size: 36rpx; }
}
</style>