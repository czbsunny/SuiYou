<template>
  <view class="search-page">
    <!-- 1. 顶部搜索栏：沉浸式白色背景 -->
    <view class="search-header">
      <view class="search-input-wrapper" :class="{ 'is-focus': isFocused }">
        <image src="/static/images/search.png" class="search-icon"></image>
        <input 
          type="text" 
          class="search-input" 
          placeholder="输入代码或名称搜索" 
          placeholder-class="placeholder-style"
          v-model="searchKeyword" 
          @input="onSearchInput" 
          @focus="isFocused = true"
          @blur="isFocused = false"
          :focus="true"
          confirm-type="search"
        />
        <view v-if="searchKeyword" class="clear-input-btn" @click="clearSearch">
            <text class="clear-icon">×</text>
        </view>
      </view>
    </view>
    
    <!-- 2. 内容区域 -->
    <scroll-view scroll-y class="content-area" @scrolltolower="onReachBottom">
        <view class="fund-list" v-if="fundList.length > 0">
          <view 
            class="fund-item" 
            v-for="fund in fundList" 
            :key="fund.fundCode" 
            @click="selectAndReturn(fund)"
            hover-class="card-hover"
          >
            <view class="fund-info">
              <text class="fund-name">{{ fund.fundName }}</text>
              <view class="fund-meta">
                  <text class="fund-code num-font">{{ fund.fundCode }}</text>
                  <text class="divider">|</text>
                  <text class="fund-type">{{ fund.type || '混合型' }}</text>
              </view>
            </view>

            <image src="/static/images/arrow-right.png" mode="aspectFit" class="arrow-icon"></image>
          </view>
        </view>
          
        <!-- 状态提示重构 -->
        <view v-if="loading" class="status-box">
            <view class="loading-spinner"></view>
            <text class="status-text">正在为您搜索中...</text>
        </view>
        
        <view v-if="!loading && fundList.length === 0" class="empty-state">
            <image v-if="!searchKeyword" src="/static/images/search-placeholder.png" class="empty-icon" />
            <text class="empty-text">{{ searchKeyword ? '未找到相关基金，请尝试换个关键词' : '请输入基金代码或名称' }}</text>
        </view>
        
        <view v-if="!loading && fundList.length > 0 && fundList.length >= totalCount" class="status-box">
            <text class="status-text">已显示全部检索结果</text>
        </view>
        
        <!-- 适配全面屏底部 -->
        <view class="safe-area-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { fundSearch } from '@/services/fund.js';

const searchKeyword = ref('');
const fundList = ref([]);
const currentPage = ref(1);
const pageSize = ref(20);
const totalCount = ref(0);
const loading = ref(false);
const isFocused = ref(false);
let searchTimer = null;

const iconColor = '#CDC7C2';

const selectAndReturn = (fund) => {
    uni.vibrateShort();
    uni.$emit('fund-selected', fund);
    uni.navigateBack();
};

const loadFundList = async () => {
    if (!searchKeyword.value.trim()) {
        fundList.value = [];
        totalCount.value = 0;
        return;
    }
    loading.value = true;
    try {
        const res = await fundSearch(searchKeyword.value, currentPage.value, pageSize.value);

        if (res.success) {
            const data = res.data;
            const newData = (data.funds || []).map(fund => ({
                fundCode: fund.fundCode,
                fundName: fund.name,
                type: fund.fundType,
                ...fund 
            }));
            fundList.value = currentPage.value === 1 ? newData : [...fundList.value, ...newData];
            totalCount.value = data.total || fundList.value.length;
        }
    } catch (error) {
        console.error('搜索失败', error);
    } finally {
        loading.value = false;
    }
};

const onSearchInput = () => {
    if (searchTimer) clearTimeout(searchTimer);
    searchTimer = setTimeout(() => {
        currentPage.value = 1;
        loadFundList();
    }, 400);
};

const onReachBottom = () => {
    if (loading.value || fundList.value.length >= totalCount.value) return;
    currentPage.value++;
    loadFundList();
};

const clearSearch = () => {
    searchKeyword.value = '';
    fundList.value = [];
    totalCount.value = 0;
};
</script>

<style lang="scss" scoped>
.search-page {
  height: 100vh;
  background-color: $bg-page;
  display: flex;
  flex-direction: column;
}

/* 搜索头部重构 */
.search-header {
  padding: 24rpx $spacing-md;
  background-color: $bg-white;
  border-bottom: 1rpx solid $border-color;
  z-index: 10;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background-color: $bg-page;
  border-radius: 40rpx; // 胶囊圆角
  padding: 0 32rpx;
  height: 80rpx;
  border: 1rpx solid transparent;
  transition: all 0.3s ease;
  
  &.is-focus {
    background-color: $bg-white;
    border-color: rgba($primary, 0.3);
    box-shadow: 0 0 12rpx rgba($primary, 0.05);
  }
}

.search-icon {
  width: 32rpx;
  height: 32rpx;
  margin-right: 16rpx;
  opacity: 0.3;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: $text-main;
}

.clear-input-btn {
    padding: 10rpx;
    @include flex-center;
}

.clear-icon {
    font-size: 40rpx;
    color: $text-inverse;
    line-height: 1;
}

.content-area {
  flex: 1;
}

/* 列表项卡片化 */
.fund-list {
  padding: $spacing-md;
  display: flex;
  flex-direction: column;
  gap: $spacing-base;
}

.fund-item {
  background-color: $bg-white;
  border-radius: $radius-lg; // 32rpx
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: $shadow-card;
  transition: all 0.2s;

  .arrow-icon {
    width: 32rpx;
    height: 32rpx;
    opacity: 0.3;
  }
}

.card-hover {
    transform: scale(0.98);
    filter: brightness(0.98);
}

.fund-info {
  flex: 1;
  margin-right: $spacing-base;
  overflow: hidden; 
}

.fund-name {
  font-size: 32rpx;
  font-weight: $fw-semibold;
  color: $text-main;
  margin-bottom: 12rpx;
  display: block;
  @include text-ellipsis; // 使用简写的省略号类
}

.fund-meta {
    display: flex;
    align-items: center;
    font-size: 24rpx;
    color: $text-placeholder;
}

.fund-code {
    background-color: $primary-subtle; 
    color: $primary;
    padding: 2rpx 12rpx;
    border-radius: 8rpx;
    font-weight: $fw-semibold;
}

.divider { margin: 0 16rpx; color: $border-color; }

/* 状态提示 */
.status-box {
    padding: 60rpx 0;
    @include flex-center;
    flex-direction: column;
}

.status-text { 
  font-size: 24rpx; 
  color: $text-placeholder; 
}

.empty-state {
    margin-top: 160rpx;
    @include flex-center;
    flex-direction: column;
}

.empty-icon { 
  width: 200rpx; 
  height: 200rpx; 
  opacity: 0.1; 
  margin-bottom: 32rpx; 
}

.empty-text { 
  font-size: 28rpx; 
  color: $text-placeholder; 
}
</style>