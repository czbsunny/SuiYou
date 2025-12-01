<template>
  <!-- 根容器：全屏 Flex 布局 -->
  <view class="search-page">
    
    <!-- 1. 顶部搜索栏 (固定在文档流顶部，不随列表滚动) -->
    <view class="search-header">
      <view class="search-input-wrapper">
        <image src="/static/images/search.png" class="search-icon"></image>
        <input 
          type="text" 
          class="search-input" 
          placeholder="输入代码或名称搜索基金" 
          v-model="searchKeyword" 
          @input="onSearchInput" 
          :focus="true"
        />
        <view v-if="searchKeyword" class="clear-input-btn" @click="clearSearch">
            <text class="clear-icon">×</text>
        </view>
      </view>
    </view>
    
    <!-- 2. 中间内容区域 (占据剩余空间，独立滚动) -->
    <scroll-view scroll-y class="content-area" @scrolltolower="onReachBottom">
        <!-- 基金列表 -->
        <view class="fund-list" v-if="fundList.length > 0">
          <view 
            class="fund-item" 
            v-for="fund in fundList" 
            :key="fund.fundCode" 
            @click="toggleFundSelection(fund)"
          >
            <view class="fund-info">
              <text class="fund-name">{{ fund.fundName }}</text>
              <view class="fund-meta">
                  <text class="fund-code">{{ fund.fundCode }}</text>
                  <text class="divider">|</text>
                  <text class="fund-type">{{ fund.type }}</text>
              </view>
            </view>
            
            <view class="checkbox" :class="{ checked: isFundSelected(fund.fundCode) }">
              <text v-if="isFundSelected(fund.fundCode)" class="check-mark">✓</text>
            </view>
          </view>
        </view>
          
        <!-- 加载/空状态 -->
        <view v-if="loading" class="status-box">
            <text class="status-text">加载中...</text>
        </view>
        
        <view v-if="!loading && fundList.length === 0" class="empty-state">
            <image v-if="!searchKeyword" src="/static/images/search-placeholder.png" class="empty-icon" />
            <text class="empty-text">{{ searchKeyword ? '未找到相关基金' : '请输入关键词开始搜索' }}</text>
        </view>
        
        <view v-if="!loading && fundList.length > 0 && fundList.length >= totalCount" class="status-box">
            <text class="status-text">没有更多数据了</text>
        </view>
        
        <!-- 底部占位 (可选，只是为了视觉缓冲) -->
        <view class="bottom-spacer"></view>
    </scroll-view>
      
    <!-- 3. 底部工具栏 (固定在文档流底部) -->
    <view class="footer-bar">
      <view class="select-all-box" @click="toggleSelectAll">
          <view class="checkbox mini" :class="{ checked: isAllSelected }">
              <text v-if="isAllSelected" class="check-mark mini-mark">✓</text>
          </view>
          <text class="select-text">{{ isAllSelected ? '取消全选' : '全选' }}</text>
      </view>
      
      <button class="confirm-btn" @click="handleConfirm">
        完成 <text v-if="selectedFundsLocal.length > 0">({{ selectedFundsLocal.length }})</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onReachBottom } from '@dcloudio/uni-app';
import { get } from '../../services/apiService.js'; 

const searchKeyword = ref('');
const fundList = ref([]);
const selectedFundsLocal = ref([]); 
const selectedFunds = ref([]);
const currentPage = ref(1);
const pageSize = ref(20);
const totalCount = ref(0);
const loading = ref(false);
let searchTimer = null;

onLoad((options) => {
    if (options.selected) {
        try {
            const decoded = decodeURIComponent(options.selected);
            selectedFunds.value = JSON.parse(decoded);
        } catch (e) {
            console.error('解析已选基金失败', e);
        }
    }
});

const loadFundList = async () => {
    if (!searchKeyword.value.trim()) {
        fundList.value = [];
        totalCount.value = 0;
        return;
    }

    loading.value = true;
    try {
        const searchParams = {
            keyword: searchKeyword.value,
            page: currentPage.value,
            pageSize: pageSize.value
        };
        
        const res = await get('/api/fund/search', searchParams);

        if (res.statusCode === 200) {
            const data = res.data;
            const newData = (data.funds || []).map(fund => ({
                fundCode: fund.fundCode,
                fundName: fund.name,
                type: fund.fundType || '混合型',
                originalData: fund
            }));
            
            if (currentPage.value === 1) {
                fundList.value = newData;
            } else {
                fundList.value = [...fundList.value, ...newData];
            }
            
            selectedFundsLocal.value = fundList.value.filter(f => selectedFunds.value.some(sf => sf === f.fundCode));
            
            totalCount.value = data.total || fundList.value.length;
        }
    } catch (error) {
        console.error('搜索失败', error);
        uni.showToast({ title: '搜索失败，请重试', icon: 'none' });
    } finally {
        loading.value = false;
    }
};

const onSearchInput = () => {
    if (searchTimer) clearTimeout(searchTimer);
    searchTimer = setTimeout(() => {
        currentPage.value = 1;
        loadFundList();
    }, 500);
};

const clearSearch = () => {
    searchKeyword.value = '';
    fundList.value = [];
    totalCount.value = 0;
};

const isFundSelected = (code) => {
    return selectedFunds.value.some(f => f === code);
};

const toggleFundSelection = (fund) => {
    const index = selectedFunds.value.findIndex(f => f === fund.fundCode);
    if (index > -1) {
        selectedFunds.value.splice(index, 1);
        selectedFundsLocal.value = selectedFundsLocal.value.filter(f => f.fundCode !== fund.fundCode);
    } else {
        selectedFunds.value.push(fund.fundCode);
        selectedFundsLocal.value.push(fund);
    }
};

const isAllSelected = computed(() => {
    if (fundList.value.length === 0) return false;
    return fundList.value.every(item => isFundSelected(item.fundCode));
});

const toggleSelectAll = () => {
    if (fundList.value.length === 0) return;
    if (isAllSelected.value) {
        const currentCodes = new Set(fundList.value.map(f => f.fundCode));
        selectedFunds.value = selectedFunds.value.filter(f => !currentCodes.has(f));
        selectedFundsLocal.value = [];
    } else {
        fundList.value.forEach(fund => {
            if (!isFundSelected(fund.fundCode)) {
                selectedFunds.value.push(fund.fundCode);
                selectedFundsLocal.value.push(fund);
            }
        });
    }
};

const handleConfirm = () => {
    uni.$emit('fund-selected', selectedFundsLocal.value);
    uni.navigateBack();
};
</script>

<style lang="scss" scoped>

/* 页面布局：Flex Column 实现头尾固定 */
.search-page {
  height: 100vh;
  width: 100%;
  background-color: $bg-page;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 1. 顶部搜索栏 (Flex Item, 不收缩) */
.search-header {
  flex-shrink: 0; /* 防止被挤压 */
  padding: 12px 16px;
  background-color: $bg-white;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background-color: $bg-subtle;
  border-radius: 20px;
  padding: 8px 16px;
  height: 40px;
  box-sizing: border-box;
}

.search-icon {
  width: 16px;
  height: 16px;
  margin-right: 8px;
  opacity: 0.5;
}

.search-input {
  flex: 1;
  font-size: 14px;
  color: $text-main;
}

.clear-input-btn {
    padding: 4px;
    display: flex;
    align-items: center;
}

.clear-icon {
    font-size: 18px;
    color: $text-muted;
    line-height: 1;
}

/* 2. 内容区域 (Flex Item, 占据剩余空间) */
.content-area {
  flex: 1;
  height: 0; /* 关键 */
  width: 100%;
  padding: 12px 16px;
  box-sizing: border-box;
}

.fund-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fund-item {
  background-color: $bg-white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}

.fund-info {
  flex: 1;
  margin-right: 16px;
  overflow: hidden; 
}

.fund-name {
  font-size: 15px;
  font-weight: 500;
  color: $text-main;
  margin-bottom: 6px;
  display: block;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.fund-meta {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: $text-muted;
}

.fund-code {
    font-family: monospace;
    background-color: $bg-subtle;
    padding: 2px 4px;
    border-radius: 4px;
}

.divider { margin: 0 6px; color: $border-color; }

/* 复选框样式 (圆圈 + 勾) */
.checkbox {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid $border-color;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  background-color: $bg-white;
  flex-shrink: 0;
  
  &.checked {
    background-color: $primary;
    border-color: $primary;
  }
  
  &.mini {
      width: 18px;
      height: 18px;
      margin-right: 8px;
  }
}

.check-mark {
    color: #ffffff;
    font-size: 14px;
    font-weight: bold;
    line-height: 1;
}

.mini-mark {
    font-size: 12px;
}

/* 状态展示 */
.status-box {
    padding: 24px 0;
    text-align: center;
}
.status-text { font-size: 12px; color: $text-muted; }

.empty-state {
    margin-top: 80px;
    display: flex;
    flex-direction: column;
    align-items: center;
}
.empty-icon { width: 64px; height: 64px; opacity: 0.3; margin-bottom: 16px; }
.empty-text { font-size: 14px; color: $text-muted; }

.bottom-spacer {
    height: 20px;
}

/* 3. 底部工具栏 (Flex Item, 不收缩) */
.footer-bar {
  flex-shrink: 0;
  width: 100%;
  background-color: $bg-white;
  padding: 12px 24px;
  border-top: 1px solid $border-color;
    
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  box-sizing: border-box;
}

.select-all-box {
    display: flex;
    align-items: center;
    padding: 8px 8px 8px 0;
}

.select-text {
    font-size: 14px;
    color: $text-main;
}

.confirm-btn {
  margin: 0; 
  background-color: $primary;
  color: #fff;
  font-size: 14px;
  font-weight: bold;
  border-radius: 20px;
  padding: 0 32px;
  height: 38px;
  line-height: 38px;
  border: none;
  
  &:active { opacity: 0.9; }
}
</style>