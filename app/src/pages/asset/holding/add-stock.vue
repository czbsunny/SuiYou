<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Manual Entry Section -->
        <section class="form-section">
          <div class="section-header">
            <h2 class="section-title">
              <span class="title-line"></span>
              手动输入
            </h2>
            <button class="ocr-btn" @tap="handleOCR">
              <image src="/static/images/photo_camera.png" class="icon-camera" />
              <span class="ocr-text">图片识别持仓</span>
            </button>
          </div>

          <div class="form-content">
            <!-- Stock Selection -->
            <view class="form-item">
              <label class="form-label">股票名称</label>
              <view class="select-trigger" @tap="handleSelectStock">
                <view v-if="selectedStock" class="select-content">
                  <text class="select-primary">{{ selectedStock.name }}</text>
                  <text class="select-secondary font-mono">{{ selectedStock.code }}</text>
                </view>
                <text v-else class="select-placeholder">点击搜索股票名称或代码...</text>
                <image src="/static/images/search.png" class="icon-search" />
              </view>
            </view>

            <!-- Input Grid -->
            <view class="input-grid">
              <view class="form-item">
                <label class="form-label">成本价</label>
                <view class="input-wrap">
                  <input 
                    class="form-input font-mono" 
                    type="digit" 
                    placeholder="0.00"
                    v-model="formData.cost"
                  />
                  <text class="input-suffix">CNY</text>
                </view>
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
            </view>

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
            <image src="/static/images/add.png" class="icon-add-circle" />
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

  </view>
</template>

<script setup>
import { ref, reactive, getCurrentInstance } from 'vue'

const instance = getCurrentInstance()

const formData = reactive({
  cost: '',
  quantity: '',
  days: ''
})

const selectedStock = ref(null)

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

const handleOCR = () => {
  uni.showToast({ title: '图片识别功能开发中', icon: 'none' })
}

const handleSelectStock = () => {
  uni.navigateTo({
    url: '/pages/asset/holding/search?type=stock'
  })
}

instance.proxy.onStockSelected = (item) => {
  selectedStock.value = item
}

const handleAddToList = () => {
  if (!selectedStock.value) {
    uni.showToast({ title: '请先选择股票', icon: 'none' })
    return
  }
  if (!formData.cost || !formData.quantity) {
    uni.showToast({ title: '请输入成本价和股数', icon: 'none' })
    return
  }
  
  const newItem = {
    id: Date.now().toString(),
    name: selectedStock.value.name,
    code: selectedStock.value.code,
    cost: parseFloat(formData.cost).toFixed(2),
    quantity: parseInt(formData.quantity).toLocaleString()
  }
  
  pendingList.value.push(newItem)
  selectedStock.value = null
  formData.cost = ''
  formData.quantity = ''
  formData.days = ''
  
  uni.showToast({ title: '已添加到列表', icon: 'success' })
}

const handleDelete = (id) => {
  pendingList.value = pendingList.value.filter(item => item.id !== id)
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
  padding-bottom: $spacing-8;
}

.content {
  padding: $spacing-4;
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
  flex: 1;
  justify-content: flex-start;
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
  justify-content: center;
  gap: $spacing-1;
  background: rgba($primary-container, 0.3);
  color: $primary;
  border-radius: $rounded-full;
  margin-left: auto;
}

.icon-camera {
  width: 32rpx;
  height: 32rpx;
}

.ocr-text {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $primary;
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
  padding: $spacing-2 $spacing-3;
  border-radius: $rounded-lg;
  min-height: 72rpx;
  box-sizing: border-box;
}

.select-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
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

.select-placeholder {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-regular;
  color: $outline;
  flex: 1;
}

.icon-search {
  height: 40rpx;
  width: 40rpx;
}

.input-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-5;
}

.input-wrap {
  position: relative;
}

.form-input {
  width: 100%;
  height: 72rpx;
  line-height: 72rpx;
  background: rgba($surface-container-low, 0.6);
  border: none;
  padding: 0 $spacing-3;
  border-radius: $rounded-lg;
  font-size: $font-size-body-sm;
  color: $on-surface;
  box-sizing: border-box;
  
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
  padding: $spacing-1;
  border-radius: $rounded-full;
  margin-top: $spacing-4;
}

.icon-add-circle {
  height: 40rpx;
  width: 40rpx;
}

.btn-text {
  font-size: $font-size-title-sm;
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
</style>