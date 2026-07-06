<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="order-form">
          <view v-if="isBuy" class="section">
            <text class="section-label">选择证券</text>
            <view class="search-box" @tap="handleSearch">
              <text class="search-icon">搜索</text>
              <text class="search-placeholder">输入代码/首字母</text>
              <view class="fav-badge">
                <text class="fav-text">自选</text>
              </view>
            </view>
          </view>

          <view v-else class="section">
            <text class="section-label">已持仓股票</text>
            <view class="stock-info" @tap="handleStockSelect">
              <view class="stock-main">
                <text class="stock-name">{{ selectedStock.name }}</text>
                <text class="stock-code">{{ selectedStock.code }}</text>
              </view>
              <text class="expand-icon">expand_more</text>
            </view>
          </view>

          <view class="section">
            <text class="section-label">{{ isBuy ? '买入价格' : '卖出价格' }}</text>
            <input 
              class="price-input" 
              type="digit" 
              v-model="price"
              :placeholder="isBuy ? '请输入买入价格' : '请输入卖出价格'"
            />
          </view>

          <view class="section">
            <view class="section-header">
              <text class="section-label">{{ isBuy ? '买入数量 (股)' : '卖出数量' }}</text>
              <text v-if="!isBuy" class="max-qty">可卖 {{ availableQuantity }}</text>
            </view>
            <input 
              class="qty-input" 
              type="digit" 
              :placeholder="isBuy ? '请输入数量' : '请输入数量'"
              v-model="quantity"
            />
          </view>

          <view class="action-btn" :class="isBuy ? 'buy-btn' : 'sell-btn'" @tap="handleAction">
            <text class="action-text">{{ isBuy ? '立即买入' : '卖出' }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const isBuy = ref(true)
const price = ref('')
const quantity = ref('')

const availableQuantity = '1,200'
const totalHoldings = 1200

const selectedStock = ref({
  name: '腾讯控股',
  code: '00700.HK'
})

onLoad((options) => {
  if (options?.type === 'sell') {
    isBuy.value = false
  }
})

const handleSearch = () => {
  uni.showToast({ title: '搜索功能开发中', icon: 'none' })
}

const handleStockSelect = () => {
  uni.showToast({ title: '选择股票', icon: 'none' })
}

const handleAction = () => {
  if (!quantity.value || parseInt(quantity.value) <= 0) {
    uni.showToast({ title: isBuy.value ? '请输入有效数量' : '请输入有效卖出数量', icon: 'none' })
    return
  }
  if (!isBuy.value && parseInt(quantity.value) > totalHoldings) {
    uni.showToast({ title: '卖出数量超过持仓', icon: 'none' })
    return
  }
  const actionText = isBuy.value ? `买入${quantity.value}股` : `卖出${quantity.value}股 ${selectedStock.value.name}`
  uni.showToast({ title: actionText, icon: 'success' })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

.scroll {
  height: 100vh;
}

.content {
  padding: $spacing-4;
}

.order-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-6;
}

.section {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-label {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $on-surface-variant;
}

.max-qty {
  font-family: $font-family-mono;
  font-size: $font-size-xs;
  color: $outline;
}

.search-box {
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  padding: $spacing-4;
  display: flex;
  align-items: center;
  box-shadow: $shadow-soft;
}

.search-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $outline;
  margin-right: $spacing-3;
}

.search-placeholder {
  flex: 1;
  font-size: $font-size-body-reg;
  color: $on-surface-variant;
}

.fav-badge {
  background: $surface-container;
  padding: $spacing-1 $spacing-3;
  border-radius: 99rpx;
}

.fav-text {
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  color: $primary;
}

.stock-info {
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  padding: $spacing-4;
  display: flex;
  align-items: center;
  box-shadow: $shadow-soft;
}

.stock-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.stock-name {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.stock-code {
  font-family: $font-family-mono;
  font-size: $font-size-xs;
  color: $outline;
}

.expand-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 36rpx;
  color: $outline;
}

.price-input {
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  padding: $spacing-3 $spacing-4;
  font-family: $font-family-mono;
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
  box-shadow: $shadow-soft;
}

.qty-input {
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  padding: $spacing-3 $spacing-4;
  font-family: $font-family-mono;
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
  box-shadow: $shadow-soft;
}

.action-btn {
  padding: $spacing-4;
  border-radius: 99rpx;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-2;
  box-shadow: $shadow-lg;
  
  &:active {
    transform: scale(0.98);
  }
  
  &.buy-btn {
    background: $secondary;
    color: $on-secondary;
  }
  
  &.sell-btn {
    background: $primary;
    color: $on-primary;
  }
}

.action-text {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
}
</style>
