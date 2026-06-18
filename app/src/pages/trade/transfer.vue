<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="segment-container">
          <view class="segment-bg">
            <view 
              class="segment-indicator" 
              :style="{ transform: isIn ? 'translateX(0)' : 'translateX(calc(100% + 12rpx))' }"
            ></view>
          </view>
          <view 
            class="segment-btn" 
            :class="{ active: isIn }"
            @tap="switchSegment(0)"
          >
            <text class="segment-text">转入证券</text>
          </view>
          <view 
            class="segment-btn" 
            :class="{ active: !isIn }"
            @tap="switchSegment(1)"
          >
            <text class="segment-text">转出证券</text>
          </view>
        </view>

        <view class="flow-section">
          <view class="flow-item">
            <view class="flow-icon-wrap" :class="isIn ? 'bg-normal' : 'bg-active'">
              <text class="flow-icon" :class="isIn ? 'text-primary' : 'text-on-primary'">{{ isIn ? '银行' : '证券' }}</text>
            </view>
            <text class="flow-label" :class="{ active: !isIn }">{{ isIn ? '银行账户' : '证券账户' }}</text>
          </view>
          <view class="flow-arrow">
            <text class="arrow-icon" :style="{ transform: isIn ? 'rotate(0deg)' : 'rotate(180deg)' }">→</text>
          </view>
          <view class="flow-item">
            <view class="flow-icon-wrap" :class="isIn ? 'bg-active' : 'bg-normal'">
              <text class="flow-icon" :class="isIn ? 'text-on-primary' : 'text-primary'">{{ isIn ? '证券' : '银行' }}</text>
            </view>
            <text class="flow-label" :class="{ active: isIn }">{{ isIn ? '证券账户' : '银行账户' }}</text>
          </view>
        </view>

        <view class="card-section">
          <view class="card-header">
            <text class="card-label">{{ isIn ? '选择银行卡' : '银行账户' }}</text>
            <text v-if="isIn" class="card-change">更换</text>
          </view>
          <view class="card-item">
            <image class="bank-logo" :src="bankLogo" mode="aspectFit" />
            <view class="bank-info">
              <text class="bank-name">{{ bankName }}</text>
              <text class="bank-number">{{ bankNumber }}</text>
            </view>
            <text v-if="isIn" class="chevron-icon">></text>
          </view>
        </view>

        <view class="amount-section">
          <view class="amount-header">
            <text class="amount-label">转账金额</text>
            <view class="available-wrap">
              <text class="available-label">可转金额</text>
              <text class="available-value">{{ availableAmount }}</text>
            </view>
          </view>
          <view class="amount-input-wrap">
            <text class="currency-symbol">¥</text>
            <input 
              class="amount-input" 
              type="digit" 
              placeholder="0.00"
              v-model="amount"
            />
            <view class="all-btn" @tap="fillAll">
              <text class="all-text">全部{{ isIn ? '转入' : '转出' }}</text>
            </view>
          </view>
        </view>

        <view class="action-section">
          <view class="action-btn" @tap="handleTransfer">
            <text class="action-text">确认{{ isIn ? '转入' : '转出' }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const isIn = ref(true)
const amount = ref('')

const bankLogo = 'https://neeko-copilot.bytedance.net/api/text2image?prompt=bank%20logo%20minimalist%20green%20color&image_size=square'
const bankName = '招商银行'
const bankNumber = '尾号 8892'
const availableAmount = '¥2,480,000.00'

const switchSegment = (index) => {
  isIn.value = index === 0
}

const fillAll = () => {
  amount.value = '2480000.00'
}

const handleTransfer = () => {
  if (!amount.value || parseFloat(amount.value) <= 0) {
    uni.showToast({ title: '请输入有效金额', icon: 'none' })
    return
  }
  uni.showToast({ title: `${isIn.value ? '转入' : '转出'}申请已提交`, icon: 'success' })
}

const navigateTo = (page) => {
  const paths = {
    buy: '/pages/trade/stock?type=buy',
    sell: '/pages/trade/stock?type=sell',
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
  padding-bottom: $spacing-4;
}

.segment-container {
  position: relative;
  margin: $spacing-4;
  padding: 6rpx;
  background: $surface-container-high;
  border-radius: 99rpx;
  display: flex;
}

.segment-bg {
  position: absolute;
  top: 6rpx;
  left: 6rpx;
  right: 6rpx;
  bottom: 6rpx;
  display: flex;
}

.segment-indicator {
  flex: 1;
  background: $surface-container-lowest;
  border-radius: 99rpx;
  box-shadow: $shadow-sm;
  transition: transform 0.3s ease;
}

.segment-btn {
  position: relative;
  flex: 1;
  padding: $spacing-3;
  text-align: center;
  z-index: 1;
  
  &.active {
    .segment-text {
      color: $primary;
    }
  }
}

.segment-text {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $on-surface-variant;
}

.flow-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-4 $spacing-6;
  margin-bottom: $spacing-6;
}

.flow-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-2;
}

.flow-icon-wrap {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.bg-normal {
    background: $surface-container-lowest;
    border: 2rpx solid $outline-variant;
  }
  &.bg-active {
    background: $primary;
  }
}

.flow-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 48rpx;
}

.flow-label {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface-variant;
  opacity: 0.6;
  
  &.active {
    color: $primary;
    opacity: 1;
  }
}

.flow-arrow {
  flex: 1;
  display: flex;
  justify-content: center;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 2rpx;
    background: $outline-variant;
    transform: translateY(-50%);
  }
}

.arrow-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $primary;
  background: $surface;
  padding: $spacing-2;
  transition: transform 0.3s ease;
}

.card-section {
  background: $surface-container-lowest;
  border-radius: $rounded-default;
  margin: 0 $spacing-4 $spacing-6;
  padding: $spacing-5;
  border: 2rpx solid $surface-container;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-4;
}

.card-label {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $on-surface-variant;
}

.card-change {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $primary;
}

.card-item {
  display: flex;
  align-items: center;
  gap: $spacing-4;
}

.bank-logo {
  width: 80rpx;
  height: 80rpx;
  border-radius: $rounded-sm;
}

.bank-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.bank-name {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.bank-number {
  font-family: $font-family-mono;
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
}

.chevron-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 36rpx;
  color: $outline;
}

.amount-section {
  background: $surface-container-lowest;
  border-radius: $rounded-default;
  margin: 0 $spacing-4 $spacing-8;
  padding: $spacing-5;
  border: 2rpx solid $surface-container;
}

.amount-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-6;
}

.amount-label {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $on-surface-variant;
}

.available-wrap {
  display: flex;
  align-items: center;
  gap: $spacing-1;
}

.available-label {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
}

.available-value {
  font-family: $font-family-mono;
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $primary;
}

.amount-input-wrap {
  display: flex;
  align-items: flex-end;
  gap: $spacing-3;
  padding-bottom: $spacing-2;
  border-bottom: 2rpx solid $surface-container-high;
}

.currency-symbol {
  font-size: $font-size-display-lg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.amount-input {
  flex: 1;
  font-family: $font-family-mono;
  font-size: $font-size-display-lg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.all-btn {
  padding: $spacing-2;
}

.all-text {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $primary;
}

.action-section {
  padding: 0 $spacing-4;
}

.action-btn {
  background: $primary;
  color: $on-primary;
  padding: $spacing-4;
  border-radius: 99rpx;
  text-align: center;
  box-shadow: $shadow-lg;
  
  &:active {
    transform: scale(0.98);
  }
}

.action-text {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
}
</style>