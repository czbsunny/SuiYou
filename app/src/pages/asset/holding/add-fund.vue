<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- TopAppBar -->
        <header class="top-bar">
          <button class="back-btn" @tap="handleBack">
            <text class="icon-back">arrow_back_ios</text>
          </button>
          <h1 class="page-title">添加基金</h1>
          <view class="placeholder"></view>
        </header>

        <!-- Manual Entry Form -->
        <section class="form-section">
          <div class="section-header">
            <h2 class="section-title">
              <span class="title-line"></span>
              手动输入
            </h2>
            <button class="ocr-btn" @tap="handleOCR">
              <text class="icon-camera">photo_camera</text>
              <span class="ocr-text">图片识别持仓</span>
            </button>
          </div>

          <div class="form-content">
            <!-- Fund Selection -->
            <view class="form-item">
              <label class="form-label">基金名称</label>
              <button class="select-trigger" @tap="handleSelectFund">
                <div class="select-content">
                  <span class="select-primary">招商中证500指数增强</span>
                  <span class="select-secondary font-mono">004192</span>
                </div>
                <text class="icon-arrow">chevron_right</text>
              </button>
            </view>

            <!-- Input Grid -->
            <div class="input-grid">
              <view class="form-item">
                <label class="form-label">持有金额 (元)</label>
                <input 
                  class="form-input font-mono" 
                  type="number" 
                  placeholder="0.00"
                  v-model="formData.amount"
                />
              </view>
              <view class="form-item">
                <label class="form-label">持有收益 (元)</label>
                <input 
                  class="form-input font-mono" 
                  type="number" 
                  placeholder="0.00"
                  v-model="formData.profit"
                />
              </view>
            </div>

            <view class="form-item">
              <label class="form-label">持有天数</label>
              <input 
                class="form-input font-mono" 
                type="number" 
                placeholder="选填"
                v-model="formData.days"
              />
            </view>
          </div>

          <button class="add-list-btn" @tap="handleAddToList">
            <text class="icon-add-circle">add_circle</text>
            <span class="btn-text">加入待添加列表</span>
          </button>
        </section>

        <!-- Pending List Section -->
        <section class="pending-section">
          <div class="section-header">
            <h2 class="section-title">待添加列表</h2>
            <span class="count-badge">{{ pendingList.length }} ITEMS</span>
          </div>

          <view class="pending-list">
            <view 
              v-for="item in pendingList" 
              :key="item.id" 
              class="pending-item"
            >
              <div class="item-content">
                <div class="item-header">
                  <span class="item-name">{{ item.name }}</span>
                  <span class="item-code font-mono">{{ item.code }}</span>
                </div>
                <div class="item-info">
                  <div class="info-block">
                    <span class="info-label">金额</span>
                    <span class="info-value font-mono">{{ item.amount }}</span>
                  </div>
                  <div class="info-block">
                    <span class="info-label">收益</span>
                    <span class="info-value font-mono" :class="{ profit: item.profit > 0 }">
                      {{ item.profit > 0 ? '+' : '' }}{{ item.profit }}
                    </span>
                  </div>
                </div>
              </div>
              <button class="delete-btn" @tap="handleDelete(item.id)">
                <text class="icon-delete">delete</text>
              </button>
            </view>
          </view>
        </section>
      </view>
    </scroll-view>

    <!-- Bottom Action Bar -->
    <div class="bottom-bar">
      <button class="confirm-btn" @tap="handleConfirm">
        <span class="confirm-text">确认添加</span>
        <text class="icon-check">check_circle</text>
      </button>
    </div>

    <!-- Background Decor -->
    <div class="bg-decor"></div>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'

const formData = reactive({
  amount: '',
  profit: '',
  days: ''
})

const pendingList = ref([
  {
    id: '1',
    name: '纳斯达克100ETF(QDII)',
    code: '159941',
    amount: '130,849.00',
    profit: 1102.32
  },
  {
    id: '2',
    name: '易方达蓝筹精选混合',
    code: '005827',
    amount: '50,000.00',
    profit: -420.15
  }
])

const handleBack = () => {
  uni.navigateBack()
}

const handleOCR = () => {
  uni.showToast({ title: '图片识别功能开发中', icon: 'none' })
}

const handleSelectFund = () => {
  uni.navigateTo({
    url: '/pages/asset/holding/search'
  })
}

const handleAddToList = () => {
  if (!formData.amount) {
    uni.showToast({ title: '请输入持有金额', icon: 'none' })
    return
  }
  
  const newItem = {
    id: Date.now().toString(),
    name: '招商中证500指数增强',
    code: '004192',
    amount: parseFloat(formData.amount).toLocaleString('zh-CN', { 
      minimumFractionDigits: 2, 
      maximumFractionDigits: 2 
    }),
    profit: parseFloat(formData.profit) || 0
  }
  
  pendingList.value.push(newItem)
  formData.amount = ''
  formData.profit = ''
  formData.days = ''
  
  uni.showToast({ title: '已添加到列表', icon: 'success' })
}

const handleDelete = (id) => {
  pendingList.value = pendingList.value.filter(item => item.id !== id)
}

const handleConfirm = () => {
  if (pendingList.value.length === 0) {
    uni.showToast({ title: '请先添加持仓', icon: 'none' })
    return
  }
  
  uni.showToast({ title: '添加成功', icon: 'success' })
  setTimeout(() => {
    uni.navigateBack()
  }, 1500)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $background;
  position: relative;
  overflow: hidden;
}

.scroll {
  height: 100vh;
  padding-bottom: 200rpx;
}

.content {
  padding: $spacing-4;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-5 $spacing-4 $spacing-4;
  background: $surface;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  padding: 0;
}

.icon-back {
  font-family: 'Material Symbols Outlined';
  font-size: 48rpx;
  color: $primary;
}

.page-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.placeholder {
  width: 64rpx;
}

.form-section {
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  padding: $spacing-5;
  box-shadow: $shadow-soft;
  margin-bottom: $spacing-4;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-5;
}

.section-title {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.title-line {
  width: 8rpx;
  height: 32rpx;
  background: $primary;
  border-radius: $rounded-sm;
}

.ocr-btn {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  background: rgba($primary-fixed, 0.3);
  color: $on-primary-fixed-variant;
  padding: $spacing-2 $spacing-3;
  border-radius: $rounded-full;
}

.icon-camera {
  font-family: 'Material Symbols Outlined';
  font-size: 36rpx;
  font-variation-settings: 'FILL' 1;
}

.ocr-text {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
}

.form-label {
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  text-transform: uppercase;
  letter-spacing: 1rpx;
  color: rgba($on-surface-variant, 0.7);
  padding-left: $spacing-1;
}

.select-trigger {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba($surface-container-low, 0.6);
  padding: $spacing-3;
  border-radius: $rounded-lg;
}

.select-content {
  display: flex;
  flex-direction: column;
}

.select-primary {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.select-secondary {
  font-size: $font-size-xs;
  color: $outline;
}

.icon-arrow {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $outline;
}

.input-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-3;
}

.form-input {
  background: rgba($surface-container-low, 0.6);
  border: none;
  padding: $spacing-3;
  border-radius: $rounded-lg;
  font-size: $font-size-body-sm;
  color: $on-surface;
  
  &::placeholder {
    color: $outline-variant;
  }
}

.font-mono {
  font-family: $font-family-mono;
}

.add-list-btn {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: $spacing-2;
  background: $primary;
  color: $on-primary;
  padding: $spacing-3;
  border-radius: $rounded-full;
  margin-top: $spacing-4;
}

.icon-add-circle {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
}

.btn-text {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
}

.pending-section {
  padding-bottom: $spacing-8;
}

.count-badge {
  background: $primary-container;
  color: $on-primary-container;
  font-size: $font-size-xs;
  padding: 4rpx 16rpx;
  border-radius: $rounded-full;
  font-weight: $font-weight-bold;
  font-family: $font-family-mono;
}

.pending-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.pending-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: $surface-container-lowest;
  padding: $spacing-4;
  border-radius: $rounded-lg;
  box-shadow: $shadow-soft;
}

.item-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.item-header {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.item-name {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.item-code {
  font-size: $font-size-xs;
  color: $outline;
  background: $surface-container;
  padding: 2rpx 12rpx;
  border-radius: $rounded-sm;
}

.item-info {
  display: flex;
  gap: $spacing-6;
}

.info-block {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  text-transform: uppercase;
  color: $outline-variant;
}

.info-value {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $on-surface;
  
  &.profit {
    color: $secondary;
  }
}

.delete-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: transparent;
  padding: 0;
}

.icon-delete {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $outline-variant;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: $spacing-4;
  padding-bottom: calc($spacing-4 + env(safe-area-inset-bottom));
  background: $surface;
  box-shadow: $shadow-soft;
  border-radius: $rounded-lg 0 0 0;
}

.confirm-btn {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: $spacing-2;
  background: $primary;
  color: $on-primary;
  padding: $spacing-4;
  border-radius: $rounded-full;
  box-shadow: 0 8rpx 24rpx rgba($primary, 0.2);
}

.confirm-text {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
}

.icon-check {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
}

.bg-decor {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: -1;
  
  &::before,
  &::after {
    content: '';
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;
  }
  
  &::before {
    top: -10%;
    right: -10%;
    width: 60%;
    height: 40%;
    background: $primary-fixed;
    filter: blur(120rpx);
  }
  
  &::after {
    bottom: -5%;
    left: -5%;
    width: 40%;
    height: 30%;
    background: $secondary-fixed;
    filter: blur(100rpx);
  }
}
</style>