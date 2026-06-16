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
            <text class="page-title">{{ isBuy ? '股票买入' : '股票卖出' }}</text>
          </view>
          <view class="top-bar-right">
            <text class="page-subtitle">交易中心</text>
          </view>
        </view>

        <!-- Stock Selector Card (Sell Mode) -->
        <view v-if="!isBuy" class="stock-card">
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
          <!-- Left Column - Order Form -->
          <view class="order-form">
            <!-- Stock Search (Buy Mode) -->
            <view v-if="isBuy" class="section">
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
                <text class="section-label">{{ isBuy ? '买入价格' : '卖出价格' }}</text>
                <template v-if="isBuy">
                  <view class="price-limit">
                    <text class="limit-text limit-up">涨停 {{ limitUp }}</text>
                    <text class="limit-text limit-down">跌停 {{ limitDown }}</text>
                  </view>
                </template>
                <text v-else class="order-type">限价委托</text>
              </view>
              <view class="price-input-wrap">
                <view class="price-btn" @tap="adjustPrice(isBuy ? -0.01 : -0.1)">
                  <text class="price-icon">remove_circle_outline</text>
                </view>
                <input 
                  class="price-input" 
                  type="digit" 
                  v-model="price"
                />
                <view class="price-btn" @tap="adjustPrice(isBuy ? 0.01 : 0.1)">
                  <text class="price-icon">add_circle_outline</text>
                </view>
              </view>
            </view>

            <!-- Quantity Input -->
            <view class="section">
              <view class="section-header">
                <text class="section-label">{{ isBuy ? '买入数量 (股)' : '卖出数量' }}</text>
                <text v-if="!isBuy" class="max-qty">可卖 {{ availableQuantity }}</text>
              </view>
              <input 
                class="qty-input" 
                type="digit" 
                :placeholder="isBuy ? '请输入100的整数倍' : '请输入数量'"
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
              
              <view v-if="isBuy" class="max-info">
                <text class="max-label">最大可买</text>
                <text class="max-value">{{ maxBuyQuantity }} 股</text>
              </view>
            </view>

            <!-- Sell Summary (Sell Mode) -->
            <view v-if="!isBuy" class="summary-card">
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
            <view class="action-btn" :class="isBuy ? 'buy-btn' : 'sell-btn'" @tap="handleAction">
              <text class="action-text">{{ isBuy ? '立即买入' : '卖出' }}</text>
              <text v-if="!isBuy" class="action-icon">sell</text>
            </view>
          </view>

          <!-- Right Column - Order Book / Market Depth -->
          <view class="order-book">
            <view class="book-header">
              <text class="book-title">{{ isBuy ? '盘口五档' : 'Market Depth' }}</text>
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
                <view v-if="!isBuy" class="depth-bar" :style="{ width: item.percent + '%' }"></view>
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
                <view v-if="!isBuy" class="depth-bar bid-bar" :style="{ width: item.percent + '%' }"></view>
                <text class="book-label">买{{ index + 1 }}</text>
                <text class="book-price text-primary">{{ item.price }}</text>
                <text class="book-volume">{{ item.volume }}</text>
              </view>
            </view>

            <!-- Portfolio Card (Buy Mode) -->
            <view v-if="isBuy" class="portfolio-card">
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
      <view class="nav-item" :class="{ active: isBuy }" @tap="switchMode(true)">
        <text class="nav-icon" :style="{ fontVariationSettings: isBuy ? '\'FILL\' 1' : '' }">add_chart</text>
        <text class="nav-label">买入</text>
      </view>
      <view class="nav-item" :class="{ active: !isBuy }" @tap="switchMode(false)">
        <text class="nav-icon" :style="{ fontVariationSettings: !isBuy ? '\'FILL\' 1' : '' }">sell</text>
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
import { onLoad } from '@dcloudio/uni-app'

const isBuy = ref(true)
const price = ref('22.10')
const quantity = ref('')

const limitUp = '24.32'
const limitDown = '19.88'
const availableFunds = '¥315,420.00'
const marketValue = '¥1,240,850.00'
const availableQuantity = '1,200'
const totalHoldings = 1200

const selectedStock = ref({
  name: '腾讯控股',
  code: '00700.HK',
  price: '412.800',
  change: '-0.42%',
  changeClass: 'text-loss'
})

const ratios = [
  { label: '1/4', value: 0.25 },
  { label: '1/2', value: 0.5 },
  { label: '3/4', value: 0.75 },
  { label: '全仓', value: 1 }
]

onLoad((options) => {
  if (options?.type === 'sell') {
    isBuy.value = false
    price.value = '412.800'
  }
})

const maxBuyQuantity = computed(() => {
  const p = parseFloat(price.value) || 0
  const funds = parseFloat(availableFunds.replace(/[¥,]/g, '')) || 0
  if (p > 0) {
    return Math.floor(funds / p / 100) * 100
  }
  return 14200
})

const estimatedAmount = computed(() => {
  const p = parseFloat(price.value) || 0
  const q = parseInt(quantity.value) || 0
  return (p * q).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
})

const sellOrders = ref(isBuy.value ? [
  { price: '22.15', volume: '824' },
  { price: '22.14', volume: '1,202' },
  { price: '22.13', volume: '543' },
  { price: '22.12', volume: '3,491' },
  { price: '22.11', volume: '762' }
] : [
  { price: '413.200', volume: '2.1k', percent: 15 },
  { price: '413.100', volume: '5.8k', percent: 45 },
  { price: '413.000', volume: '8.4k', percent: 65 },
  { price: '412.900', volume: '3.2k', percent: 30 },
  { price: '412.800', volume: '12.5k', percent: 85 }
])

const buyOrders = ref(isBuy.value ? [
  { price: '22.10', volume: '1,104' },
  { price: '22.09', volume: '882' },
  { price: '22.08', volume: '2,551' },
  { price: '22.07', volume: '430' },
  { price: '22.06', volume: '1,829' }
] : [
  { price: '412.700', volume: '7.1k', percent: 60 },
  { price: '412.600', volume: '4.3k', percent: 35 },
  { price: '412.500', volume: '18.2k', percent: 90 },
  { price: '412.400', volume: '2.5k', percent: 20 },
  { price: '412.300', volume: '6.7k', percent: 50 }
])

const adjustPrice = (delta) => {
  let current = parseFloat(price.value) || 0
  current = Math.max(isBuy.value ? 0.01 : 0.001, current + delta)
  price.value = isBuy.value ? current.toFixed(2) : current.toFixed(3)
}

const fillPrice = (p) => {
  price.value = p
}

const setQuantity = (ratio) => {
  const max = isBuy.value ? maxBuyQuantity.value : totalHoldings
  quantity.value = Math.floor(max * ratio).toString()
}

const switchMode = (buy) => {
  isBuy.value = buy
  if (buy) {
    price.value = '22.10'
    sellOrders.value = [
      { price: '22.15', volume: '824' },
      { price: '22.14', volume: '1,202' },
      { price: '22.13', volume: '543' },
      { price: '22.12', volume: '3,491' },
      { price: '22.11', volume: '762' }
    ]
    buyOrders.value = [
      { price: '22.10', volume: '1,104' },
      { price: '22.09', volume: '882' },
      { price: '22.08', volume: '2,551' },
      { price: '22.07', volume: '430' },
      { price: '22.06', volume: '1,829' }
    ]
  } else {
    price.value = '412.800'
    sellOrders.value = [
      { price: '413.200', volume: '2.1k', percent: 15 },
      { price: '413.100', volume: '5.8k', percent: 45 },
      { price: '413.000', volume: '8.4k', percent: 65 },
      { price: '412.900', volume: '3.2k', percent: 30 },
      { price: '412.800', volume: '12.5k', percent: 85 }
    ]
    buyOrders.value = [
      { price: '412.700', volume: '7.1k', percent: 60 },
      { price: '412.600', volume: '4.3k', percent: 35 },
      { price: '412.500', volume: '18.2k', percent: 90 },
      { price: '412.400', volume: '2.5k', percent: 20 },
      { price: '412.300', volume: '6.7k', percent: 50 }
    ]
  }
  quantity.value = ''
}

const handleBack = () => {
  uni.navigateBack()
}

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

const navigateTo = (page) => {
  const paths = {
    transfer: '/pages/trade/transfer',
    query: '/pages/trade/query'
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

.order-type {
  font-size: $font-size-xs;
  color: $outline-variant;
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

.summary-card {
  background: $surface-container-low;
  border-radius: $rounded-lg;
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

.action-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
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
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-2;
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
  
  &.bid-bar {
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