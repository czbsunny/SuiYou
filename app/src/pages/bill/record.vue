<template>
  <view class="page record-page">
    <!-- Top AppBar -->
    <view class="header">
      <view class="header-left">
        <view class="avatar-wrapper">
          <image class="avatar" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCEI_rZ9JsQgc39F5xnFsMUSZhhdAzg_FEltdYY5STKZMIqn_AlbVKpP-XqqprMv5xcyRW2kVdq94aWcs9sgPLnyMkETP28fk45uJf3xpetT69F_RdmlJ8zYjwatDkyiGOkO4KnW6HK-hFDZq80qUjxoz_vitoOXe_OLb1wQgkSvHwqMl6hTjwlFNiwjZX9SGrflhBFSsHasXyEwFdMxu2RVEjCMkECXdLKmFPizrIbhsrx_QANKbplxr78Km04Iwt0EDV_PZqa9WS7" mode="aspectFill" />
        </view>
        <text class="app-name">Stewardship</text>
      </view>
      <view class="close-btn" @tap="handleClose">
        <text class="icon">close</text>
      </view>
    </view>

    <!-- Segmented Control -->
    <view class="segmented-bar">
      <view class="segmented-picker" :class="{ 'income-active': isIncome }">
        <view class="segmented-indicator"></view>
        <view class="segment-btn" :class="{ active: !isIncome }" @tap="setType('expense')">
          <text>支出</text>
        </view>
        <view class="segment-btn" :class="{ active: isIncome }" @tap="setType('income')">
          <text>收入</text>
        </view>
      </view>
    </view>

    <!-- Main Content -->
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Category Selection Card -->
        <view class="category-card">
          <!-- Primary Categories Row 1 -->
          <view class="category-grid">
            <view v-for="cat in primaryCategories" :key="cat.name" class="category-item" :class="{ active: selectedPrimary === cat.name }" @tap="selectPrimary(cat)">
              <view class="category-icon-wrapper" :class="{ active: selectedPrimary === cat.name }">
                <text class="category-icon">{{ cat.icon }}</text>
              </view>
              <text class="category-label" :class="{ active: selectedPrimary === cat.name }">{{ cat.name }}</text>
            </view>
          </view>

          <!-- Sub-category Block -->
          <view v-if="selectedPrimary" class="sub-category-section">
            <view class="sub-category-grid">
              <view v-for="sub in subCategories" :key="sub.name" class="sub-category-item" :class="{ active: selectedSub === sub.name }" @tap="selectSub(sub)">
                <view class="sub-icon-wrapper" :class="{ active: selectedSub === sub.name }">
                  <text class="sub-icon">{{ sub.icon }}</text>
                </view>
                <text class="sub-label" :class="{ active: selectedSub === sub.name }">{{ sub.name }}</text>
              </view>
            </view>
          </view>

          <!-- Primary Categories Row 2 -->
          <view class="category-grid">
            <view v-for="cat in secondaryCategories" :key="cat.name" class="category-item" :class="{ active: selectedPrimary === cat.name }" @tap="selectPrimary(cat)">
              <view class="category-icon-wrapper" :class="{ active: selectedPrimary === cat.name }">
                <text class="category-icon">{{ cat.icon }}</text>
              </view>
              <text class="category-label" :class="{ active: selectedPrimary === cat.name }">{{ cat.name }}</text>
            </view>
          </view>
        </view>

        <!-- Info Block -->
        <view class="info-card">
          <view class="amount-display">
            <text class="currency">¥</text>
            <text class="amount-value">{{ currentAmount }}</text>
          </view>
          <view class="info-tags">
            <view class="info-tag">
              <text class="tag-icon">calendar_today</text>
              <input class="tag-input" type="text" placeholder="今天 15:00" value="今天 15:00" />
            </view>
            <view class="info-tag" @tap="handleAccount">
              <text class="tag-icon">account_balance_wallet</text>
              <text class="tag-text">账户</text>
            </view>
            <view class="info-tag">
              <text class="tag-icon">notes</text>
              <input class="tag-input" type="text" placeholder="备注..." />
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Keypad -->
    <view class="keypad">
      <view class="keypad-grid">
        <view class="keypad-numbers">
          <view v-for="num in keypadNumbers" :key="num" class="keypad-btn" @tap="appendNumber(num)">
            <text>{{ num }}</text>
          </view>
          <view class="keypad-btn" @tap="deleteChar">
            <text class="icon">backspace</text>
          </view>
        </view>
        <view class="keypad-actions">
          <view class="keypad-row">
            <view class="action-btn" @tap="setOperator('+')">
              <text>+</text>
            </view>
            <view class="action-btn" @tap="setOperator('-')">
              <text>-</text>
            </view>
          </view>
          <view class="keypad-row">
            <view class="action-btn" @tap="setOperator('×')">
              <text>×</text>
            </view>
            <view class="action-btn" @tap="setOperator('÷')">
              <text>÷</text>
            </view>
          </view>
          <view class="action-btn secondary" @tap="saveAndAgain">
            <text>再记</text>
          </view>
          <view class="action-btn primary" @tap="confirmSave">
            <text>完成</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const isIncome = ref(false)
const selectedPrimary = ref('食品餐饮')
const selectedSub = ref('早餐')
const currentAmount = ref('6')

const primaryCategories = [
  { name: '食品餐饮', icon: 'restaurant' },
  { name: '购物消费', icon: 'shopping_cart' },
  { name: '出行交通', icon: 'commute' },
  { name: '休闲娱乐', icon: 'sports_esports' },
  { name: '教育', icon: 'school' }
]

const secondaryCategories = [
  { name: '居家', icon: 'home' },
  { name: '医疗', icon: 'medical_services' },
  { name: '人情', icon: 'payments' },
  { name: '职业', icon: 'work' },
  { name: '更多', icon: 'more_horiz' }
]

const subCategories = [
  { name: '早餐', icon: 'bakery_dining' },
  { name: '午餐', icon: 'lunch_dining' },
  { name: '晚餐', icon: 'dinner_dining' },
  { name: '宵夜', icon: 'local_bar' },
  { name: '酒水', icon: 'coffee' },
  { name: '甜点', icon: 'icecream' },
  { name: '零食', icon: 'fastfood' },
  { name: '食材', icon: 'restaurant_menu' },
  { name: '生鲜', icon: 'set_meal' },
  { name: '自定义', icon: 'add' }
]

const keypadNumbers = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '0']

const setType = (type) => {
  isIncome.value = type === 'income'
}

const selectPrimary = (cat) => {
  selectedPrimary.value = cat.name
}

const selectSub = (sub) => {
  selectedSub.value = sub.name
}

const handleClose = () => {
  uni.navigateBack()
}

const handleAccount = () => {
  uni.showToast({ title: '选择账户', icon: 'none' })
}

const appendNumber = (num) => {
  if (currentAmount.value === '0' || currentAmount.value === '0.00') {
    currentAmount.value = num === '.' ? '0.' : num
  } else {
    if (num === '.' && currentAmount.value.includes('.')) return
    currentAmount.value += num
  }
}

const deleteChar = () => {
  if (currentAmount.value.length <= 1) {
    currentAmount.value = '0'
  } else {
    currentAmount.value = currentAmount.value.slice(0, -1)
  }
}

const setOperator = (op) => {
  console.log('Operator:', op)
}

const saveAndAgain = () => {
  uni.showToast({ title: '已保存: ¥' + currentAmount.value, icon: 'none' })
  currentAmount.value = '0'
}

const confirmSave = () => {
  uni.showToast({ title: '交易已保存', icon: 'success' })
  setTimeout(() => {
    uni.navigateBack()
  }, 1000)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $background;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-3 $container-padding;
  background: $background;
}

.header-left {
  display: flex;
  align-items: center;
  gap: $stack-gap-sm;
}

.avatar-wrapper {
  width: 64rpx;
  height: 64rpx;
  border-radius: $rounded-full;
  border: 3rpx solid #C5A36A;
  overflow: hidden;
}

.avatar {
  width: 100%;
  height: 100%;
}

.app-name {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $primary;
}

.close-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $primary;
}

.segmented-bar {
  padding: $spacing-2 0;
  border-bottom: 1rpx solid $surface-container-low;
}

.segmented-picker {
  position: relative;
  width: 480rpx;
  height: 68rpx;
  margin: 0 auto;
  background: $surface-container;
  border-radius: $rounded-full;
  display: flex;
  padding: 4rpx;
}

.segmented-indicator {
  position: absolute;
  top: 4rpx;
  bottom: 4rpx;
  left: 4rpx;
  width: calc(50% - 4rpx);
  background: $surface-container-lowest;
  border-radius: $rounded-full;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.income-active .segmented-indicator {
  transform: translateX(100%);
}

.segment-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  
  text {
    font-size: 32rpx;
    font-weight: $font-weight-semibold;
    color: $outline;
  }
  
  &.active text {
    color: $primary;
  }
}

.scroll {
  flex: 1;
}

.content {
  padding: $spacing-4;
}

.category-card {
  padding: $spacing-4;
  border-radius: $rounded-xl;
  background: $surface-container-lowest;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: $stack-gap-md;
  padding: $spacing-2 0;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $stack-gap-sm;
}

.category-icon-wrapper {
  width: 96rpx;
  height: 96rpx;
  border-radius: $rounded-lg;
  background: $surface-container-low;
  display: flex;
  align-items: center;
  justify-content: center;

  &.active {
    background: $primary-container;
    .category-icon {
      color: $on-primary-container;
    }
  }
}

.category-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 44rpx;
  color: $on-surface-variant;
}

.category-label {
  font-size: 22rpx;
  font-weight: $font-weight-bold;
  color: $on-surface-variant;

  &.active {
    color: $primary;
  }
}

.sub-category-section {
  margin: $stack-gap-md 0;
  padding: $spacing-3;
  border-radius: $rounded-xl;
  background: $surface-container-low;
}

.sub-category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: $stack-gap-sm;
}

.sub-category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $stack-gap-sm;
}

.sub-icon-wrapper {
  width: 88rpx;
  height: 88rpx;
  border-radius: $rounded-lg;
  background: $surface-container-lowest;
  border: 1rpx solid rgba($outline-variant, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;

  &.active {
    background: $primary-container;
    border-color: transparent;
    .sub-icon {
      color: $on-primary-container;
    }
  }
}

.sub-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 36rpx;
  color: $outline;
}

.sub-label {
  font-size: 20rpx;
  color: $outline;

  &.active {
    color: $primary;
    font-weight: $font-weight-bold;
  }
}

.info-card {
  margin-top: $stack-gap-md;
  padding: $spacing-4;
  border-radius: $rounded-xl;
  background: $surface-container-lowest;
  box-shadow: $shadow-soft;
}

.amount-display {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  padding: $spacing-1;
}

.currency {
  font-size: $font-size-headline-md;
  font-weight: $font-weight-bold;
  color: $primary;
}

.amount-value {
  font-family: $font-family-mono;
  font-size: 64rpx;
  font-weight: $font-weight-semibold;
  color: $on-surface;
  line-height: 1;
}

.info-tags {
  display: flex;
  gap: $stack-gap-sm;
  margin-top: $stack-gap-md;
  overflow-x: auto;
  padding-bottom: $spacing-1;

  &::-webkit-scrollbar {
    display: none;
  }
}

.info-tag {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  padding: $spacing-2 $spacing-3;
  border-radius: $rounded-full;
  background: $surface-container-low;
  flex-shrink: 0;
}

.tag-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 28rpx;
  color: $on-surface-variant;
}

.tag-text {
  font-size: 24rpx;
  color: $on-surface-variant;
}

.tag-input {
  font-size: 24rpx;
  color: $on-surface-variant;
  background: transparent;
  border: none;
  outline: none;
  width: 160rpx;
}

.keypad {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: $surface-container-lowest;
  border-top: 1rpx solid $surface-variant;
  padding-bottom: env(safe-area-inset-bottom);
}

.keypad-grid {
  display: flex;
}

.keypad-numbers {
  flex: 3;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
}

.keypad-btn {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $surface-container-lowest;
  border-right: 1rpx solid $surface-variant;
  border-bottom: 1rpx solid $surface-variant;

  text {
    font-family: $font-family-mono;
    font-size: $font-size-headline-md;
    font-weight: $font-weight-medium;
    color: $on-surface;
  }

  &:active {
    background: $surface-container-low;
    transform: scale(0.95);
  }
}

.keypad-actions {
  flex: 1;
  display: grid;
  grid-template-rows: repeat(4, 1fr);
}

.keypad-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
}

.action-btn {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $surface-container-low;
  border-bottom: 1rpx solid $surface-variant;

  text {
    font-family: $font-family-mono;
    font-size: $font-size-title-sm;
    font-weight: $font-weight-medium;
    color: $on-surface-variant;
  }

  &.secondary {
    background: $surface-container-lowest;
    
    text {
      color: $primary;
      font-weight: $font-weight-bold;
    }
  }

  &.primary {
    background: $primary-container;
    
    text {
      color: $on-primary-container;
      font-weight: $font-weight-bold;
    }
  }

  &:active {
    transform: scale(0.95);
  }
}
</style>