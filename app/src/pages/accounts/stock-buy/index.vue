<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Top App Bar -->
        <view class="top-bar">
          <view class="top-bar-left">
            <view class="back-btn" @tap="handleBack">
              <text class="icon-arrow">arrow_back</text>
            </view>
            <text class="page-title">股票买入</text>
          </view>
          <view class="top-bar-right">
            <text class="page-subtitle">交易中心</text>
          </view>
        </view>

        <view class="main-grid">
          <!-- Left Column - Order Form -->
          <view class="order-form">
            <!-- Stock Search -->
            <view class="section">
              <text class="section-label">选择证券</text>
              <view class="search-box" @tap="handleSearch">
                <text class="search-icon">search</text>
                <text class="search-placeholder">输入代码/首字母</text>
                <view class="fav-badge">
                  <text class="fav-text">自选</text>
                </view>
              </view>
            </view>

            <!-- Price Input -->
            <view class="section">
              <view class="price-header">
                <text class="section-label">买入价格</text>
                <view class="price-limit">
                  <text class="limit-text limit-up">涨停 {{ limitUp }}</text>
                  <text class="limit-text limit-down">跌停 {{ limitDown }}</text>
                </view>
              </view>
              <view class="price-input-wrap">
                <view class="price-btn" @tap="adjustPrice(-0.01)">
                  <text class="price-icon">remove_circle_outline</text>
                </view>
                <input 
                  class="price-input" 
                  type="digit" 
                  v-model="price"
                />
                <view class="price-btn" @tap="adjustPrice(0.01)">
                  <text class="price-icon">add_circle_outline</text>
                </view>
              </view>
            </view>

            <!-- Quantity Input -->
            <view class="section">
              <text class="section-label">买入数量 (股)</text>
              <input 
                class="qty-input" 
                type="digit" 
                placeholder="请输入100的整数倍"
                v-model="quantity"
              />
              
              <!-- Quick Ratio Buttons -->
              <view class="ratio-buttons">
                <view 
                  v-for="ratio in ratios" 
                  :key="ratio.value"
                  class="ratio-btn"
                  @tap="setQuantity(ratio.value)"
                >
                  <text class="ratio-text">{{ ratio.label }}</text>
                </view>
              </view>
              
              <view class="max-info">
                <text class="max-label">最大可买</text>
                <text class="max-value">{{ maxBuyQuantity }} 股</text>
              </view>
            </view>

            <!-- Buy Button -->
            <view class="buy-btn" @tap="handleBuy">
              <text class="buy-text">立即买入</text>
            </view>
          </view>

          <!-- Right Column - Order Book -->
          <view class="order-book">
            <view class="book-header">
              <text class="book-title">盘口五档</text>
              <text class="book-update">实时更新</text>
            </view>

            <!-- Sell Orders -->
            <view class="book-section sell-section">
              <view 
                v-for="(item, index) in sellOrders" 
                :key="'sell-' + index"
                class="book-item"
                @tap="fillPrice(item.price)"
              >
                <text class="book-label">卖{{ 5 - index }}</text>
                <text class="book-price text-profit">{{ item.price }}</text>
                <text class="book-volume">{{ item.volume }}</text>
              </view>
            </view>

            <view class="book-divider"></view>

            <!-- Buy Orders -->
            <view class="book-section buy-section">
              <view 
                v-for="(item, index) in buyOrders" 
                :key="'buy-' + index"
                class="book-item"
                @tap="fillPrice(item.price)"
              >
                <text class="book-label">买{{ index + 1 }}</text>
                <text class="book-price text-primary">{{ item.price }}</text>
                <text class="book-volume">{{ item.volume }}</text>
              </view>
            </view>

            <!-- Portfolio Card -->
            <view class="portfolio-card">
              <view class="portfolio-item">
                <text class="portfolio-label">可用资金</text>
                <text class="portfolio-value">{{ availableFunds }}</text>
              </view>
              <view class="portfolio-item">
                <text class="portfolio-label">参考市值</text>
                <text class="portfolio-value">{{ marketValue }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Bottom Navigation -->
    <view class="bottom-nav">
      <view class="nav-item active">
        <text class="nav-icon" style="font-variation-settings: 'FILL' 1;">add_chart</text>
        <text class="nav-label">买入</text>
      </view>
      <view class="nav-item" @tap="navigateTo('sell')">
        <text class="nav-icon">sell</text>
        <text class="nav-label">卖出</text>
      </view>
      <view class="nav-item" @tap="navigateTo('transfer')">
        <text class="nav-icon">swap_horiz</text>
        <text class="nav-label">转账</text>
      </view>
      <view class="nav-item" @tap="navigateTo('query')">
        <text class="nav-icon">history_edu</text>
        <text class="nav-label">查询</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const price = ref('22.10')
const quantity = ref('')

const limitUp = '24.32'
const limitDown = '19.88'
const availableFunds = '¥315,420.00'
const marketValue = '¥1,240,850.00'

const ratios = [
  { label: '1/4', value: 0.25 },
  { label: '1/2', value: 0.5 },
  { label: '3/4', value: 0.75 },
  { label: '全仓', value: 1 }
]

const maxBuyQuantity = computed(() => {
  const p = parseFloat(price.value) || 0
  const funds = parseFloat(availableFunds.replace(/[¥,]/g, '')) || 0
  if (p > 0) {
    return Math.floor(funds / p / 100) * 100
  }
  return 14200
})

const sellOrders = ref([
  { price: '22.15', volume: '824' },
  { price: '22.14', volume: '1,202' },
  { price: '22.13', volume: '543' },
  { price: '22.12', volume: '3,491' },
  { price: '22.11', volume: '762' }
])

const buyOrders = ref([
  { price: '22.10', volume: '1,104' },
  { price: '22.09', volume: '882' },
  { price: '22.08', volume: '2,551' },
  { price: '22.07', volume: '430' },
  { price: '22.06', volume: '1,829' }
])

const adjustPrice = (delta) => {
  let current = parseFloat(price.value) || 0
  current = Math.max(0.01, current + delta)
  price.value = current.toFixed(2)
}

const fillPrice = (p) => {
  price.value = p
}

const setQuantity = (ratio) => {
  const max = maxBuyQuantity.value
  quantity.value = Math.floor(max * ratio).toString()
}

const handleBack = () => {
  uni.navigateBack()
}

const handleSearch = () => {
  uni.showToast({ title: '搜索功能开发中', icon: 'none' })
}

const handleBuy = () => {
  if (!quantity.value || parseInt(quantity.value) <= 0) {
    uni.showToast({ title: '请输入有效数量', icon: 'none' })
    return
  }
  uni.showToast({ title: `买入${quantity.value}股`, icon: 'success' })
}

const navigateTo = (page) => {
  const paths = {
    sell: '/pages/accounts/stock-sell/index',
    transfer: '/pages/accounts/transfer/index',
    query: '/pages/accounts/transaction-query/index'
  }
  if (paths[page]) {
    uni.navigateTo({ url: paths[page] })
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $background;
}

.scroll {
  height: 100vh;
}

.content {
  padding-bottom: 200rpx;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: $surface;
  padding: 60rpx $spacing-4 $spacing-4;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: $spacing-4;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $surface-container-low;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-arrow {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $primary;
}

.page-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.top-bar-right {
  display: flex;
  align-items: center;
}

.page-subtitle {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $primary;
}

.main-grid {
  padding: $spacing-4;
  display: flex;
  flex-direction: column;
  gap: $spacing-6;
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

.section-label {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $on-surface-variant;
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

.price-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-limit {
  display: flex;
  gap: $spacing-4;
}

.limit-text {
  font-family: $font-family-mono;
  font-size: $font-size-xs;
  
  &.limit-up {
    color: $secondary;
  }
  &.limit-down {
    color: $primary;
  }
}

.price-input-wrap {
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  padding: 6rpx;
  display: flex;
  align-items: center;
  box-shadow: $shadow-soft;
}

.price-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:active {
    opacity: 0.7;
  }
}

.price-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 48rpx;
  color: $primary;
}

.price-input {
  flex: 1;
  text-align: center;
  font-family: $font-family-mono;
  font-size: $font-size-display-lg;
  font-weight: $font-weight-bold;
  color: $on-surface;
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

.ratio-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-3;
}

.ratio-btn {
  background: $surface-container;
  border-radius: 99rpx;
  padding: $spacing-3;
  text-align: center;
  
  &:active {
    background: $primary-container;
    
    .ratio-text {
      color: $on-primary;
    }
  }
}

.ratio-text {
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  color: $on-surface-variant;
}

.max-info {
  display: flex;
  justify-content: space-between;
}

.max-label {
  font-size: $font-size-xs;
  color: $on-surface-variant;
}

.max-value {
  font-family: $font-family-mono;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.buy-btn {
  background: $secondary;
  color: $on-secondary;
  padding: $spacing-4;
  border-radius: 99rpx;
  text-align: center;
  box-shadow: $shadow-lg;
  
  &:active {
    transform: scale(0.98);
  }
}

.buy-text {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
}

.order-book {
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  padding: $spacing-4;
  box-shadow: $shadow-soft;
}

.book-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: $spacing-3;
  border-bottom: 2rpx solid $surface-container;
}

.book-title {
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  color: $on-surface-variant;
}

.book-update {
  font-family: $font-family-mono;
  font-size: $font-size-xs;
  color: $outline;
}

.book-section {
  display: flex;
  flex-direction: column;
}

.book-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-2;
  border-radius: $rounded-sm;
  
  &:active {
    background: rgba($primary, 0.05);
  }
}

.book-label {
  font-size: $font-size-xs;
  color: $outline;
}

.book-price {
  font-family: $font-family-mono;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  
  &.text-profit {
    color: $secondary;
  }
  &.text-primary {
    color: $primary;
  }
}

.book-volume {
  font-family: $font-family-mono;
  font-size: $font-size-xs;
  color: $on-surface-variant;
}

.book-divider {
  height: 2rpx;
  background: $surface-container-high;
  margin: $spacing-2 0;
}

.portfolio-card {
  background: $surface;
  border-radius: $rounded-lg;
  padding: $spacing-4;
  margin-top: $spacing-4;
  border: 2rpx solid rgba($outline-variant, 0.3);
}

.portfolio-item {
  display: flex;
  justify-content: space-between;
  
  & + & {
    margin-top: $spacing-2;
  }
}

.portfolio-label {
  font-size: $font-size-xs;
  color: $on-surface-variant;
}

.portfolio-value {
  font-family: $font-family-mono;
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 50;
  background: $surface-container-lowest;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.04);
  border-radius: $rounded-lg $rounded-lg 0 0;
  padding: $spacing-3 $spacing-4;
  padding-bottom: calc(#{$spacing-3} + env(safe-area-inset-bottom));
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  color: $on-surface-variant;
  
  &.active {
    color: $primary;
  }
}

.nav-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 44rpx;
}

.nav-label {
  font-family: $font-family-mono;
  font-size: 20rpx;
  font-weight: $font-weight-bold;
}
</style>