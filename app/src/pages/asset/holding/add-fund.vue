<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Manual Entry Form -->
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
            <!-- Fund Selection -->
            <view class="form-item">
              <label class="form-label">基金名称</label>
              <view class="select-trigger" @tap="handleSelectFund">
                <view v-if="selectedFund" class="select-content">
                  <text class="select-primary">{{ selectedFund.name }}</text>
                  <text class="select-secondary font-mono">{{ selectedFund.code }}</text>
                </view>
                <text v-else class="select-placeholder">点击搜索基金名称或代码...</text>
                <image src="/static/images/search.png" class="icon-search" />
              </view>
            </view>

            <!-- Input Grid -->
            <view class="input-grid">
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
            </view>

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

  </view>
</template>

<script setup>
import { ref, reactive, getCurrentInstance } from 'vue'

const instance = getCurrentInstance()

const formData = reactive({
  amount: '',
  profit: '',
  days: ''
})

const selectedFund = ref(null)

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

const handleOCR = () => {
  uni.showToast({ title: '图片识别功能开发中', icon: 'none' })
}

const handleSelectFund = () => {
  uni.navigateTo({
    url: '/pages/asset/holding/search?type=fund'
  })
}

instance.proxy.onFundSelected = (item) => {
  selectedFund.value = item
}

const handleAddToList = () => {
  if (!selectedFund.value) {
    uni.showToast({ title: '请先选择基金', icon: 'none' })
    return
  }
  if (!formData.amount) {
    uni.showToast({ title: '请输入持有金额', icon: 'none' })
    return
  }
  
  const newItem = {
    id: Date.now().toString(),
    name: selectedFund.value.name,
    code: selectedFund.value.code,
    amount: parseFloat(formData.amount).toLocaleString('zh-CN', { 
      minimumFractionDigits: 2, 
      maximumFractionDigits: 2 
    }),
    profit: parseFloat(formData.profit) || 0
  }
  
  pendingList.value.push(newItem)
  selectedFund.value = null
  formData.amount = ''
  formData.profit = ''
  formData.days = ''
  
  uni.showToast({ title: '已添加到列表', icon: 'success' })
}

const handleDelete = (id) => {
  pendingList.value = pendingList.value.filter(item => item.id !== id)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

.scroll {
  height: 100vh;
  padding-bottom: $spacing-8;
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
  color: $outline;
  flex: 1;
}

.icon-search {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
}

.input-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-5;
}

.form-input {
  height: 72rpx;
  line-height: 72rpx;
  background: rgba($surface-container-low, 0.6);
  border: none;
  padding: 0 $spacing-3;
  border-radius: $rounded-lg;
  font-size: $font-size-body-sm;
  color: $on-surface;
  box-sizing: border-box;
  width: 100%;
  
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
</style>