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
            <text class="page-title">交易中心</text>
          </view>
          <view class="top-bar-right">
            <text class="page-subtitle">HERITAGE</text>
          </view>
        </view>

        <!-- Stock Selector Card -->
        <view class="stock-card">
          <text class="card-label">已持仓股票</text>
          <view class="stock-info" @tap="handleStockSelect">
            <view class="stock-main">
              <text class="stock-name">{{ selectedStock.name }}</text>
              <text class="stock-code">{{ selectedStock.code }}</text>
            </view>
            <view class="stock-price-wrap">
              <text class="stock-price" :class="selectedStock.changeClass">{{ selectedStock.price }}</text>
              <text class="stock-change" :class="selectedStock.changeClass">{{ selectedStock.change }}</text>
            </view>
            <text class="expand-icon">expand_more</text>
          </view>
          <view class="stock-available">
            <text class="available-label">可用持仓</text>
            <text class="available-value">{{ availableQuantity }} <text class="available-unit">股</text></text>
          </view>
        </view>

        <view class="main-grid">
          <!-- Order Form -->
          <view class="order-form">
            <!-- Price Input -->
            <view class="section">
              <view class="section-header">
                <text class="section-label">卖出价格</text>
                <text class="order-type">限价委托</text>
              </view>
              <view class="price-input-wrap">
                <view class="price-btn" @tap="adjustPrice(-0.1)">
                  <text class="price-icon">remove</text>
                </view>
                <input 
                  class="price-input" 
                  type="digit" 
                  v-model="price"
                />
                <view class="price-btn" @tap="adjustPrice(0.1)">
                  <text class="price-icon">add</text>
                </view>
              </view>
            </view>

            <!-- Quantity Input -->
            <view class="section">
              <view class="section-header">
                <text class="section-label">卖出数量</text>
                <text class="max-qty">可卖 {{ availableQuantity }}</text>
              </view>
              <input 
                class="qty-input" 
                type="digit" 
                placeholder="请输入数量"
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
            </view>
          </view>

          <!-- Market Depth -->
          <view class="market-depth">
            <text class="depth-title">Market Depth</text>
            
            <!-- Ask (Sell) Side -->
            <view class="depth-section ask-section">
              <view 
                v-for="(item, index) in askOrders" 
                :key="'ask-' + index"
                class="depth-item"
                @tap="fillPrice(item.price)"
              >
                <view class="depth-bar" :style="{ width: item.percent + '%' }"></view>
                <text class="depth-label">卖{{ 5 - index }}</text>
                <text class="depth-price text-profit">{{ item.price }}</text>
                <text class="depth-volume">{{ item.volume }}</text>
              </view>
            </view>

            <view class="depth-divider"></view>

            <!-- Bid (Buy) Side -->
            <view class="depth-section bid-section">
              <view 
                v-for="(item, index) in bidOrders" 
                :key="'bid-' + index"
                class="depth-item"
                @tap="fillPrice(item.price)"
              >
                <view class="depth-bar bid-bar" :style="{ width: item.percent + '%' }"></view>
                <text class="depth-label">买{{ index + 1 }}</text>
                <text class="depth-price text-primary">{{ item.price }}</text>
                <text class="depth-volume">{{ item.volume }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- Sell Summary -->
        <view class="summary-card">
          <view class="summary-row">
            <text class="summary-label">预计成交金额</text>
            <text class="summary-value">{{ estimatedAmount }}</text>
          </view>
          <view class="summary-tips">
            <view class="tip-item">
              <text class="tip-icon">info</text>
              <text class="tip-text">手续费约: ¥12.50</text>
            </view>
            <view class="tip-item">
              <text class="tip-icon">timer</text>
              <text class="tip-text">T+1 资金到账</text>
            </view>
          </view>
        </view>

        <!-- Action Button -->
        <view class="action-section">
          <view class="sell-btn" @tap="handleSell">
            <text class="sell-text">卖出</text>
            <text class="sell-icon">sell</text>
          </view>
          <text class="risk-text">股票投资有风险，入市需谨慎</text>
        </view>
      </view>
    </scroll-view>

    <!-- Bottom Navigation -->
    <view class="bottom-nav">
      <view class="nav-item" @tap="navigateTo('buy')">
        <text class="nav-icon">add_chart</text>
        <text class="nav-label">买入</text>
      </view>
      <view class="nav-item active">
        <text class="nav-icon" style="font-variation-settings: 'FILL' 1;">sell</text>
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

const price = ref('412.800')
const quantity = ref('')

const selectedStock = ref({
  name: '腾讯控股',
  code: '00700.HK',
  price: '412.800',
  change: '-0.42%',
  changeClass: 'text-loss'
})

const availableQuantity = '1,200'
const totalHoldings = 1200

const ratios = [
  { label: '1/4', value: 0.25 },
  { label: '1/2', value: 0.5 },
  { label: '3/4', value: 0.75 },
  { label: '全仓', value: 1 }
]

const estimatedAmount = computed(() => {
  const p = parseFloat(price.value) || 0
  const q = parseInt(quantity.value) || 0
  return (p * q).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
})

const askOrders = ref([
  { price: '413.200', volume: '2.1k', percent: 15 },
  { price: '413.100', volume: '5.8k', percent: 45 },
  { price: '413.000', volume: '8.4k', percent: 65 },
  { price: '412.900', volume: '3.2k', percent: 30 },
  { price: '412.800', volume: '12.5k', percent: 85 }
])

const bidOrders = ref([
  { price: '412.700', volume: '7.1k', percent: 60 },
  { price: '412.600', volume: '4.3k', percent: 35 },
  { price: '412.500', volume: '18.2k', percent: 90 },
  { price: '412.400', volume: '2.5k', percent: 20 },
  { price: '412.300', volume: '6.7k', percent: 50 }
])

const adjustPrice = (delta) => {
  let current = parseFloat(price.value) || 0
  current = Math.max(0.001, current + delta)
  price.value = current.toFixed(3)
}

const fillPrice = (p) => {
  price.value = p
}

const setQuantity = (percent) => {
  quantity.value = Math.floor(totalHoldings * percent).toString()
}

const handleBack = () => {
  uni.navigateBack()
}

const handleStockSelect = () => {
  uni.showToast({ title: '选择股票', icon: 'none' })
}

const handleSell = () => {
  const q = parseInt(quantity.value)
  if (!quantity.value || q <= 0 || q > totalHoldings) {
    uni.showToast({ title: '请输入有效卖出数量', icon: 'none' })
    return
  }
  uni.showToast({ title: `卖出${quantity.value}股 ${selectedStock.value.name}`, icon: 'success' })
}

const navigateTo = (page) => {
  const paths = {
    buy: '/pages/accounts/stock-buy/index',
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
  color: $primary;
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

.stock-card {
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  margin: $spacing-4;
  padding: $spacing-5;
  box-shadow: $shadow-soft;
}

.card-label {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $on-surface-variant;
  margin-bottom: $spacing-3;
}

.stock-info {
  display: flex;
  align-items: center;
  padding: $spacing-4;
  background: $surface-container;
  border-radius: $rounded-md;
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

.stock-price-wrap {
  text-align: right;
  margin-right: $spacing-3;
}

.stock-price {
  font-family: $font-family-mono;
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  
  &.text-profit {
    color: $secondary;
  }
  &.text-loss {
    color: $primary;
  }
}

.stock-change {
  font-family: $font-family-mono;
  font-size: $font-size-xs;
  
  &.text-profit {
    color: $secondary;
  }
  &.text-loss {
    color: $primary;
  }
}

.expand-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 36rpx;
  color: $outline;
}

.stock-available {
  display: flex;
  justify-content: space-between;
  margin-top: $spacing-4;
}

.available-label {
  font-size: $font-size-xs;
  color: $on-surface-variant;
}

.available-value {
  font-family: $font-family-mono;
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.available-unit {
  font-size: $font-size-xs;
  color: $outline;
  margin-left: $spacing-1;
}

.main-grid {
  padding: 0 $spacing-4;
  display: flex;
  flex-direction: column;
  gap: $spacing-5;
}

.order-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.section {
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  padding: $spacing-4;
  box-shadow: $shadow-soft;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-2;
}

.section-label {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $on-surface-variant;
}

.order-type {
  font-size: $font-size-xs;
  color: $outline-variant;
}

.max-qty {
  font-family: $font-family-mono;
  font-size: $font-size-xs;
  color: $outline;
}

.price-input-wrap {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.price-btn {
  width: 64rpx;
  height: 64rpx;
  background: $surface-container;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:active {
    transform: scale(0.95);
  }
}

.price-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 32rpx;
  color: $on-surface;
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
  width: 100%;
  background: $surface-container;
  border-radius: $rounded-md;
  padding: $spacing-3 $spacing-4;
  font-family: $font-family-mono;
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
  margin-bottom: $spacing-4;
}

.ratio-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-2;
}

.ratio-btn {
  background: $surface-container;
  border-radius: 99rpx;
  padding: $spacing-3;
  text-align: center;
  border: 2rpx solid $outline-variant;
  
  &:active {
    background: $surface-container-high;
  }
}

.ratio-text {
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  color: $on-surface-variant;
}

.market-depth {
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  padding: $spacing-4;
  box-shadow: $shadow-soft;
}

.depth-title {
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  color: $outline-variant;
  margin-bottom: $spacing-3;
}

.depth-section {
  display: flex;
  flex-direction: column;
}

.depth-item {
  position: relative;
  display: flex;
  align-items: center;
  padding: $spacing-1 $spacing-2;
  border-radius: $rounded-sm;
  height: 48rpx;
  overflow: hidden;
  
  &:active {
    background: rgba($primary, 0.05);
  }
}

.depth-bar {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba($secondary, 0.05);
  transition: width 0.2s ease;
}

.bid-bar {
  background: rgba($primary, 0.05);
}

.depth-label {
  font-size: 22rpx;
  color: $outline;
  width: 60rpx;
}

.depth-price {
  flex: 1;
  text-align: right;
  font-family: $font-family-mono;
  font-size: 22rpx;
  font-weight: $font-weight-semibold;
  
  &.text-profit {
    color: $secondary;
  }
  &.text-primary {
    color: $primary;
  }
}

.depth-volume {
  font-family: $font-family-mono;
  font-size: 22rpx;
  color: $on-surface-variant;
  margin-left: $spacing-2;
}

.depth-divider {
  height: 2rpx;
  background: $surface-container-high;
  margin: $spacing-2 0;
}

.summary-card {
  background: $surface-container-low;
  border-radius: $rounded-lg;
  margin: $spacing-4;
  padding: $spacing-5;
  border: 2rpx solid rgba($outline-variant, 0.1);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-label {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
}

.summary-value {
  font-family: $font-family-mono;
  font-size: $font-size-display-lg;
  font-weight: $font-weight-bold;
  color: $primary;
}

.summary-tips {
  display: flex;
  gap: $spacing-4;
  margin-top: $spacing-3;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: $spacing-1;
}

.tip-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 24rpx;
  color: $outline;
}

.tip-text {
  font-size: $font-size-xs;
  color: $outline;
}

.action-section {
  padding: 0 $spacing-4;
  text-align: center;
}

.sell-btn {
  background: $primary;
  color: $on-primary;
  padding: $spacing-4;
  border-radius: 99rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-2;
  box-shadow: $shadow-lg;
  
  &:active {
    transform: scale(0.98);
  }
}

.sell-text {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
}

.sell-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
}

.risk-text {
  font-size: $font-size-xs;
  color: $outline-variant;
  margin-top: $spacing-3;
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