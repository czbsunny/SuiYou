<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="top-bar">
          <view class="top-bar-left">
            <view class="back-btn" @tap="handleBack">
              <text class="icon-arrow">arrow_back</text>
            </view>
            <text class="page-title">交易查询</text>
          </view>
          <view class="top-bar-right">
            <view class="calendar-btn" @tap="handleCalendar">
              <text class="icon-calendar">calendar_today</text>
            </view>
          </view>
        </view>

        <view class="tabs">
          <view 
            v-for="(tab, index) in tabs" 
            :key="index"
            class="tab-item"
            :class="{ active: activeTab === index }"
            @tap="activeTab = index"
          >
            <text class="tab-text">{{ tab }}</text>
          </view>
        </view>

        <view class="transaction-section">
          <view class="section-header">
            <text class="section-title">今日明细</text>
            <view class="transaction-count">
              <text class="count-label">成交笔数</text>
              <text class="count-value">{{ transactionCount }}</text>
            </view>
          </view>

          <view class="transaction-list">
            <view 
              v-for="item in transactions" 
              :key="item.id"
              class="transaction-item"
            >
              <view class="transaction-header">
                <view class="transaction-info">
                  <view class="tag-wrap">
                    <text class="tag" :class="item.typeClass">{{ item.type }}</text>
                  </view>
                  <text class="stock-name">{{ item.name }}</text>
                  <text class="stock-code">{{ item.code }}</text>
                </view>
                <view class="transaction-right">
                  <text class="amount" :class="item.amountClass">{{ item.amount }}</text>
                  <text class="amount-label">成交金额</text>
                </view>
              </view>
              <view class="transaction-time">
                <text class="time-text">{{ item.time }}</text>
              </view>
              <view class="transaction-detail">
                <view class="detail-item">
                  <text class="detail-label">价格</text>
                  <text class="detail-value">{{ item.price }}</text>
                </view>
                <view class="detail-item">
                  <text class="detail-label">成交量</text>
                  <text class="detail-value">{{ item.quantity }}</text>
                </view>
                <view class="detail-item">
                  <text class="detail-label">佣金/税费</text>
                  <text class="detail-value" :class="item.amountClass">{{ item.commission }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="bottom-nav">
      <view class="nav-item" @tap="navigateTo('buy')">
        <text class="nav-icon">add_chart</text>
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
      <view class="nav-item active">
        <text class="nav-icon" style="font-variation-settings: 'FILL' 1;">history_edu</text>
        <text class="nav-label">查询</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const activeTab = ref(1)
const tabs = ['当日成交', '历史成交', '流水查询']

const transactions = ref([
  {
    id: '1',
    type: '买入',
    typeClass: 'type-buy',
    name: '中信证券',
    code: '600030',
    amount: '+25,740.00',
    amountClass: 'text-profit',
    time: '14:22:10',
    price: '21.45',
    quantity: '1,200',
    commission: '+25,740.00'
  },
  {
    id: '2',
    type: '卖出',
    typeClass: 'type-sell',
    name: '比亚迪',
    code: '002594',
    amount: '144,050.00',
    amountClass: 'text-loss',
    time: '11:05:43',
    price: '288.10',
    quantity: '500',
    commission: '144,050.00'
  },
  {
    id: '3',
    type: '撤单',
    typeClass: 'type-cancel',
    name: '贵州茅台',
    code: '600519',
    amount: '0.00',
    amountClass: 'text-gray',
    time: '09:45:00',
    price: '1,680.00',
    quantity: '0',
    commission: '0.00'
  },
  {
    id: '4',
    type: '买入',
    typeClass: 'type-buy',
    name: '腾讯控股',
    code: '00700',
    amount: '+114,720.00',
    amountClass: 'text-profit',
    time: '09:32:15',
    price: '382.40',
    quantity: '300',
    commission: '+114,720.00'
  }
])

const transactionCount = computed(() => {
  return transactions.value.length.toString().padStart(2, '0')
})

const handleBack = () => {
  uni.navigateBack()
}

const handleCalendar = () => {
  uni.showToast({ title: '选择日期', icon: 'none' })
}

const navigateTo = (page) => {
  const paths = {
    buy: '/pages/trade/stock?type=buy',
    sell: '/pages/trade/stock?type=sell',
    transfer: '/pages/trade/transfer'
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
  padding-bottom: 180rpx;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: $background;
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

.calendar-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-calendar {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $on-surface-variant;
}

.tabs {
  background: $surface;
  padding: $spacing-4;
  padding-bottom: $spacing-3;
  display: flex;
  gap: $spacing-6;
  border-bottom: 2rpx solid $surface-container;
}

.tab-item {
  position: relative;
  padding-bottom: $spacing-3;
  
  &.active {
    .tab-text {
      color: $primary;
      font-weight: $font-weight-bold;
    }
    
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 32rpx;
      height: 6rpx;
      background: $primary;
      border-radius: 99rpx;
    }
  }
}

.tab-text {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
}

.transaction-section {
  padding: $spacing-4;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-4;
}

.section-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.transaction-count {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.count-label {
  font-size: $font-size-xs;
  color: $on-surface-variant;
  opacity: 0.6;
}

.count-value {
  font-family: $font-family-mono;
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $primary;
}

.transaction-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.transaction-item {
  background: $surface-container-lowest;
  border-radius: $rounded-default;
  padding: $spacing-5;
  box-shadow: $shadow-soft;
}

.transaction-header {
  display: flex;
  justify-content: space-between;
}

.transaction-info {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.tag-wrap {
  flex-shrink: 0;
}

.tag {
  font-size: 20rpx;
  font-weight: $font-weight-bold;
  padding: 4rpx 12rpx;
  border-radius: 99rpx;
  
  &.type-buy {
    background: rgba($secondary, 0.1);
    color: $secondary;
  }
  &.type-sell {
    background: rgba($primary, 0.1);
    color: $primary;
  }
  &.type-cancel {
    background: $surface-container-highest;
    color: $on-surface-variant;
  }
}

.stock-name {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.stock-code {
  font-family: $font-family-mono;
  font-size: $font-size-xs;
  color: $on-surface-variant;
  opacity: 0.6;
}

.transaction-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.amount {
  font-family: $font-family-mono;
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  
  &.text-profit {
    color: $secondary;
  }
  &.text-loss {
    color: $primary;
  }
  &.text-gray {
    color: $on-surface-variant;
  }
}

.amount-label {
  font-size: $font-size-xs;
  color: $on-surface-variant;
  opacity: 0.6;
}

.transaction-time {
  margin-top: $spacing-1;
}

.time-text {
  font-family: $font-family-mono;
  font-size: $font-size-xs;
  color: $on-surface-variant;
}

.transaction-detail {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-4;
  margin-top: $spacing-4;
  padding-top: $spacing-4;
  border-top: 2rpx solid $surface-container-low;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-label {
  font-size: $font-size-xs;
  color: $on-surface-variant;
  opacity: 0.6;
  margin-bottom: $spacing-1;
}

.detail-value {
  font-family: $font-family-mono;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $on-surface;
  
  &.text-profit {
    color: $secondary;
  }
  &.text-loss {
    color: $primary;
  }
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