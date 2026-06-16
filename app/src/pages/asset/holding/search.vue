<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- TopAppBar -->
        <header class="top-bar">
          <button class="back-btn" @tap="handleBack">
            <text class="icon-back">arrow_back_ios_new</text>
          </button>
          <div class="search-wrap">
            <text class="icon-search">search</text>
            <input 
              class="search-input" 
              type="text" 
              placeholder="输入基金名称或代码"
              v-model="searchText"
              @confirm="handleSearch"
            />
          </div>
        </header>

        <!-- Asset Type Switcher -->
        <div class="switcher-section">
          <scroll-view scroll-x class="switcher-scroll hide-scrollbar">
            <view class="switcher-wrap">
              <button 
                v-for="type in assetTypes" 
                :key="type.id" 
                class="switcher-btn"
                :class="{ active: activeType === type.id }"
                @tap="handleTypeSwitch(type.id)"
              >
                {{ type.label }}
              </button>
            </view>
          </scroll-view>
        </div>

        <!-- History Section -->
        <section class="history-section">
          <div class="section-header">
            <h2 class="section-title">历史记录</h2>
            <button class="clear-btn" @tap="handleClearHistory">清除</button>
          </div>
          <view class="history-tags">
            <button 
              v-for="tag in historyTags" 
              :key="tag" 
              class="history-tag"
              @tap="handleTagTap(tag)"
            >{{ tag }}</button>
          </view>
        </section>

        <!-- Empty State -->
        <div class="empty-state">
          <view class="empty-illustration">
            <text class="icon-empty">search_off</text>
          </view>
          <p class="empty-text">开始探索你的投资目标</p>
        </div>
      </view>
    </scroll-view>

    <!-- BottomNavBar -->
    <nav class="bottom-nav">
      <button class="nav-item" @tap="navigateTo('home')">
        <text class="nav-icon">home</text>
        <span class="nav-label">Home</span>
      </button>
      <button class="nav-item active" @tap="navigateTo('goals')">
        <text class="nav-icon active">target</text>
        <span class="nav-label active">Goals</span>
      </button>
      <button class="nav-item" @tap="navigateTo('assets')">
        <text class="nav-icon">account_balance_wallet</text>
        <span class="nav-label">Assets</span>
      </button>
      <button class="nav-item" @tap="navigateTo('mine')">
        <text class="nav-icon">person</text>
        <span class="nav-label">Mine</span>
      </button>
    </nav>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const searchText = ref('')
const activeType = ref('fund')

const assetTypes = ref([
  { id: 'fund', label: '基金' },
  { id: 'stock', label: '股票' },
  { id: 'hk', label: '港股' },
  { id: 'us', label: '美股' }
])

const historyTags = ref([
  '沪深300',
  '医疗健康',
  '000001',
  '低碳经济'
])

const handleBack = () => {
  uni.navigateBack()
}

const handleSearch = () => {
  if (!searchText.value.trim()) {
    uni.showToast({ title: '请输入搜索内容', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages/asset/holding/search-result?keyword=${encodeURIComponent(searchText.value)}`
  })
}

const handleTypeSwitch = (type) => {
  activeType.value = type
}

const handleClearHistory = () => {
  historyTags.value = []
}

const handleTagTap = (tag) => {
  searchText.value = tag
  uni.navigateTo({
    url: `/pages/asset/holding/search-result?keyword=${encodeURIComponent(tag)}`
  })
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
}

.scroll {
  height: 100vh;
  padding-bottom: 180rpx;
}

.content {
  padding-bottom: $spacing-8;
}

.top-bar {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-3 $spacing-4;
  background: $surface;
  position: sticky;
  top: 0;
  z-index: 100;
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
  color: $on-surface;
}

.search-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  background: $surface-container-low;
  border-radius: $rounded-full;
  padding: $spacing-2;
}

.icon-search {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $outline;
  margin-left: $spacing-4;
}

.search-input {
  flex: 1;
  background: transparent;
  border: none;
  padding: 0 $spacing-4;
  font-size: $font-size-body-sm;
  color: $on-surface;
  
  &::placeholder {
    color: $on-surface-variant;
  }
}

.switcher-section {
  padding: $spacing-2 $spacing-4;
  background: $surface;
}

.switcher-scroll {
  white-space: nowrap;
}

.hide-scrollbar::-webkit-scrollbar {
  display: none;
}

.switcher-wrap {
  display: inline-flex;
  gap: $spacing-2;
}

.switcher-btn {
  padding: $spacing-2 $spacing-6;
  background: $surface-container-low;
  color: $on-surface-variant;
  border-radius: $rounded-md;
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
  
  &.active {
    background: $primary-container;
    color: $on-primary-container;
  }
}

.history-section {
  padding: $spacing-6 $spacing-4;
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

.clear-btn {
  font-size: $font-size-xs;
  color: $outline;
  text-transform: uppercase;
  background: transparent;
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-2;
}

.history-tag {
  padding: $spacing-2 $spacing-4;
  background: $surface-container-low;
  color: $on-surface-variant;
  border-radius: $rounded-md;
  font-size: $font-size-body-sm;
  
  &:active {
    transform: scale(0.95);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-20 0;
  opacity: 0.3;
}

.empty-illustration {
  width: 128rpx;
  height: 128rpx;
  background: $surface-container-high;
  border-radius: $rounded-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(to top right, rgba($primary, 0.1), transparent);
  }
  
  &::after {
    content: '';
    position: absolute;
    bottom: -16rpx;
    right: -16rpx;
    width: 64rpx;
    height: 64rpx;
    background: rgba($primary, 0.1);
    border-radius: 50%;
    filter: blur(20rpx);
  }
}

.icon-empty {
  font-family: 'Material Symbols Outlined';
  font-size: 80rpx;
  color: $outline;
}

.empty-text {
  margin-top: $spacing-4;
  font-size: $font-size-body-sm;
  color: $outline;
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
  font-size: 20rpx;
  color: $on-surface-variant;
}
</style>