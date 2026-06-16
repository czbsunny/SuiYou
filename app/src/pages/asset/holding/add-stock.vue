<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- TopAppBar -->
        <header class="top-bar">
          <button class="back-btn" @tap="handleBack">
            <text class="icon-back">arrow_back</text>
          </button>
          <h1 class="page-title">录入持仓</h1>
        </header>

        <!-- Manual Entry Section -->
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
            <!-- Stock Selection -->
            <view class="form-item">
              <label class="form-label">股票名称</label>
              <button class="select-trigger" @tap="handleSelectStock">
                <span class="select-placeholder">点击搜索股票名称或代码...</span>
                <text class="icon-search">search</text>
              </button>
            </view>

            <!-- Input Grid -->
            <div class="input-grid">
              <view class="form-item">
                <label class="form-label">成本价</label>
                <div class="input-wrap">
                  <input 
                    class="form-input font-mono" 
                    type="digit" 
                    placeholder="0.00"
                    v-model="formData.cost"
                  />
                  <span class="input-suffix">CNY</span>
                </div>
              </view>
              <view class="form-item">
                <label class="form-label">持仓股数</label>
                <input 
                  class="form-input font-mono" 
                  type="number" 
                  placeholder="100"
                  v-model="formData.quantity"
                />
              </view>
            </div>

            <!-- Holding Days -->
            <view class="form-item">
              <label class="form-label">持有天数 (可选)</label>
              <input 
                class="form-input font-mono" 
                type="number" 
                placeholder="选填"
                v-model="formData.days"
              />
            </view>
          </div>

          <!-- Add to List Button -->
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
                    <span class="info-label">成本</span>
                    <span class="info-value font-mono">{{ item.cost }}</span>
                  </div>
                  <div class="info-block">
                    <span class="info-label">股数</span>
                    <span class="info-value font-mono">{{ item.quantity }}</span>
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
        <span class="confirm-text">确认录入持仓</span>
        <text class="icon-check">check_circle</text>
      </button>
      <p class="hint-text">录入后资产将自动更新至您的投资组合</p>
    </div>

    <!-- BottomNavBar -->
    <nav class="bottom-nav">
      <button class="nav-item" @tap="navigateTo('home')">
        <text class="nav-icon">home</text>
        <span class="nav-label">首页</span>
      </button>
      <button class="nav-item" @tap="navigateTo('goals')">
        <text class="nav-icon">track_changes</text>
        <span class="nav-label">目标</span>
      </button>
      <button class="nav-item active" @tap="navigateTo('assets')">
        <text class="nav-icon active">account_balance_wallet</text>
        <span class="nav-label active">资产</span>
      </button>
      <button class="nav-item" @tap="navigateTo('mine')">
        <text class="nav-icon">person</text>
        <span class="nav-label">我的</span>
      </button>
    </nav>

    <!-- Background Decor -->
    <div class="bg-decor"></div>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'

const formData = reactive({
  cost: '',
  quantity: '',
  days: ''
})

const pendingList = ref([
  {
    id: '1',
    name: '腾讯控股',
    code: '00700.HK',
    cost: '384.20',
    quantity: '500'
  },
  {
    id: '2',
    name: '招商银行',
    code: '600036.SH',
    cost: '32.15',
    quantity: '2,400'
  }
])

const handleBack = () => {
  uni.navigateBack()
}

const handleOCR = () => {
  uni.showToast({ title: '图片识别功能开发中', icon: 'none' })
}

const handleSelectStock = () => {
  uni.navigateTo({
    url: '/pages/asset/holding/search?type=stock'
  })
}

const handleAddToList = () => {
  if (!formData.cost || !formData.quantity) {
    uni.showToast({ title: '请输入成本价和股数', icon: 'none' })
    return
  }
  
  const newItem = {
    id: Date.now().toString(),
    name: '股票名称',
    code: '待搜索',
    cost: parseFloat(formData.cost).toFixed(2),
    quantity: parseInt(formData.quantity).toLocaleString()
  }
  
  pendingList.value.push(newItem)
  formData.cost = ''
  formData.quantity = ''
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
  
  uni.showToast({ title: '录入成功', icon: 'success' })
  setTimeout(() => {
    uni.navigateBack()
  }, 1500)
}

const navigateTo = (page) => {
  const pages = {
    home: '/pages/home/index',
    goals: '/pages/goal/index',
    assets: '/pages/asset/index',
    mine: '/pages/profile/index'
  }
  uni.navigateTo({ url: pages[page] })
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
  padding-bottom: 320rpx;
}

.content {
  padding: $spacing-4;
}

.top-bar {
  display: flex;
  align-items: center;
  gap: $spacing-4;
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
  font-size: 44rpx;
  color: $primary;
}

.page-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
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

.select-placeholder {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface-variant;
}

.icon-search {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $outline;
}

.input-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-3;
}

.input-wrap {
  position: relative;
}

.form-input {
  width: 100%;
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

.input-suffix {
  position: absolute;
  right: $spacing-3;
  top: 50%;
  transform: translateY(-50%);
  font-size: $font-size-xs;
  color: $outline;
  font-family: $font-family-mono;
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
  gap: $spacing-8;
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
  bottom: 140rpx;
  left: 0;
  right: 0;
  padding: $spacing-4;
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

.hint-text {
  text-align: center;
  font-size: $font-size-xs;
  color: $outline;
  margin-top: $spacing-3;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: $spacing-3;
  padding-bottom: calc($spacing-3 + env(safe-area-inset-bottom));
  background: $surface-container-lowest;
  box-shadow: 0 -4px 20px 0 rgba(0, 0, 0, 0.04);
  border-radius: $rounded-lg 0 0 0;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-1;
  padding: $spacing-2;
  background: transparent;
  
  &.active {
    background: rgba($primary-container, 0.1);
    border-radius: $rounded-lg;
    
    .nav-icon {
      font-variation-settings: 'FILL' 1;
    }
    
    .nav-label {
      color: $primary;
    }
  }
}

.nav-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 48rpx;
  color: $on-surface-variant;
  
  &.active {
    color: $primary;
  }
}

.nav-label {
  font-family: $font-family-mono;
  font-size: 20rpx;
  font-weight: $font-weight-bold;
  text-transform: uppercase;
  color: $on-surface-variant;
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
  
  &::before {
    content: '';
    position: absolute;
    top: -10%;
    right: -10%;
    width: 60%;
    height: 40%;
    background: $primary;
    border-radius: 50%;
    opacity: 0.05;
    filter: blur(120rpx);
  }
}
</style>