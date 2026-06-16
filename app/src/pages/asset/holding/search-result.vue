<template>
  <view class="page">
    <scroll-view scroll-y class="scroll" @scrolltolower="loadMore">
      <view class="content">
        <!-- Header Section -->
        <header class="top-bar">
          <button class="back-btn" @tap="handleBack">
            <text class="icon-back">arrow_back</text>
          </button>
          <div class="search-box">
            <text class="icon-search">search</text>
            <input 
              class="search-input" 
              type="text" 
              placeholder="搜索基金、股票或代码"
              v-model="searchKeyword"
              @confirm="handleSearch"
            />
            <button class="clear-btn" @tap="clearSearch">
              <text class="icon-clear">cancel</text>
            </button>
          </div>
        </header>

        <!-- Filter Tabs -->
        <nav class="filter-tabs">
          <button 
            v-for="tab in filterTabs" 
            :key="tab.id" 
            class="tab-item"
            :class="{ active: activeTab === tab.id }"
            @tap="handleTabSwitch(tab.id)"
          >
            {{ tab.label }}
            <view v-if="activeTab === tab.id" class="tab-indicator"></view>
          </button>
        </nav>

        <!-- Results Summary -->
        <div class="results-summary">
          <span class="summary-text">共找到 {{ totalCount }} 个结果</span>
          <button class="filter-btn">
            <text class="icon-filter">filter_list</text>
            <span class="filter-text">筛选</span>
          </button>
        </div>

        <!-- Results List -->
        <div class="results-list">
          <view 
            v-for="fund in fundList" 
            :key="fund.id" 
            class="result-card"
            @tap="handleFundTap(fund)"
          >
            <div class="card-content">
              <div class="fund-header">
                <h3 class="fund-name">{{ fund.name }}</h3>
                <span v-if="fund.hot" class="hot-tag">热门</span>
              </div>
              <div class="fund-meta">
                <span class="fund-code font-mono">{{ fund.code }}</span>
                <span class="fund-type">{{ fund.type }}</span>
                <span v-if="fund.risk" class="fund-risk">{{ fund.risk }}</span>
              </div>
            </div>
            <div class="card-right">
              <span class="fund-pnl font-mono" :class="{ positive: fund.pnl > 0 }">
                {{ fund.pnl > 0 ? '+' : '' }}{{ fund.pnl }}%
              </span>
              <button class="add-btn" :class="{ added: fund.added }" @tap.stop="handleAdd(fund)">
                <text class="icon-add">{{ fund.added ? 'check' : 'add' }}</text>
              </button>
            </div>
          </view>
        </div>

        <!-- Loading Indicator -->
        <view v-if="loading" class="loading-indicator">
          <view class="loading-dots">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
          <p class="loading-text">正在加载更多优质基金...</p>
        </view>
      </view>
    </scroll-view>

    <!-- BottomNavBar -->
    <nav class="bottom-nav">
      <button class="nav-item" @tap="navigateTo('home')">
        <text class="nav-icon">home</text>
        <span class="nav-label">首页</span>
      </button>
      <button class="nav-item" @tap="navigateTo('goals')">
        <text class="nav-icon">target</text>
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

    <!-- Decorative Elements -->
    <div class="bg-glow top"></div>
    <div class="bg-glow bottom"></div>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const searchKeyword = ref('基金')
const activeTab = ref('fund')
const loading = ref(true)
const totalCount = ref(284)

const filterTabs = ref([
  { id: 'fund', label: '基金' },
  { id: 'stock', label: '股票' },
  { id: 'hk', label: '港股' },
  { id: 'us', label: '美股' },
  { id: 'bond', label: '债券' }
])

const fundList = ref([
  {
    id: '1',
    name: '招商中证500指数增强',
    code: '004192',
    type: '指数型',
    risk: '高风险',
    pnl: 2.48,
    hot: true,
    added: false
  },
  {
    id: '2',
    name: '中欧医疗健康混合A',
    code: '003095',
    type: '混合型',
    pnl: -1.12,
    added: true
  },
  {
    id: '3',
    name: '易方达蓝筹精选混合',
    code: '005827',
    type: '混合型',
    pnl: 0.85,
    added: false
  },
  {
    id: '4',
    name: '华夏上证50ETF联接A',
    code: '001051',
    type: '指数型',
    pnl: 1.22,
    added: false
  },
  {
    id: '5',
    name: '汇添富消费行业混合',
    code: '000083',
    type: '股票型',
    pnl: -0.45,
    added: false
  },
  {
    id: '6',
    name: '富国天惠成长混合',
    code: '161005',
    type: '混合型',
    pnl: 0.15,
    added: false
  },
  {
    id: '7',
    name: '南方中证全指证券公司',
    code: '004070',
    type: '指数型',
    pnl: -2.31,
    added: false
  },
  {
    id: '8',
    name: '嘉实新兴产业股票',
    code: '000751',
    type: '股票型',
    pnl: 3.44,
    added: false
  },
  {
    id: '9',
    name: '广发高端制造股票A',
    code: '004997',
    type: '股票型',
    pnl: -1.08,
    added: false
  },
  {
    id: '10',
    name: '工银瑞信文体产业股票',
    code: '001714',
    type: '股票型',
    pnl: 0.67,
    added: false
  },
  {
    id: '11',
    name: '万家行业优选混合',
    code: '161903',
    type: '混合型',
    pnl: 2.19,
    added: false
  },
  {
    id: '12',
    name: '兴全趋势投资混合',
    code: '163402',
    type: '混合型',
    pnl: -0.05,
    added: false
  },
  {
    id: '13',
    name: '景顺长城新兴成长混合',
    code: '260108',
    type: '混合型',
    pnl: 1.76,
    added: false
  },
  {
    id: '14',
    name: '交银海外中国互联网',
    code: '004882',
    type: 'QDII型',
    pnl: -4.12,
    added: false
  },
  {
    id: '15',
    name: '天弘余额宝货币',
    code: '000198',
    type: '货币型',
    pnl: 0.01,
    added: false
  },
  {
    id: '16',
    name: '博时黄金ETF联接C',
    code: '000930',
    type: '商品型',
    pnl: 0.56,
    added: false
  },
  {
    id: '17',
    name: '华安纳斯达克100',
    code: '040046',
    type: 'QDII型',
    pnl: 1.89,
    added: false
  },
  {
    id: '18',
    name: '民生加银瑞信',
    code: '000001',
    type: '债券型',
    pnl: 0.03,
    added: false
  },
  {
    id: '19',
    name: '诺安成长混合',
    code: '320007',
    type: '混合型',
    pnl: -3.88,
    added: false
  },
  {
    id: '20',
    name: '鹏华中证酒指数',
    code: '160632',
    type: '指数型',
    pnl: 0.92,
    added: false
  }
])

onLoad((options) => {
  if (options?.keyword) {
    searchKeyword.value = decodeURIComponent(options.keyword)
  }
  setTimeout(() => {
    loading.value = false
  }, 1000)
})

const handleBack = () => {
  uni.navigateBack()
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    uni.showToast({ title: '请输入搜索内容', icon: 'none' })
    return
  }
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 1000)
}

const clearSearch = () => {
  searchKeyword.value = ''
}

const handleTabSwitch = (tab) => {
  activeTab.value = tab
}

const handleFundTap = (fund) => {
  uni.showToast({ title: `${fund.name}详情`, icon: 'none' })
}

const handleAdd = (fund) => {
  fund.added = !fund.added
  uni.showToast({ 
    title: fund.added ? '已添加' : '已取消', 
    icon: fund.added ? 'success' : 'none' 
  })
}

const loadMore = () => {
  if (loading.value) return
  loading.value = true
  setTimeout(() => {
    loading.value = false
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
  padding-bottom: 180rpx;
}

.content {
  padding: $spacing-4;
}

.top-bar {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-4;
  padding-top: $spacing-5;
  background: rgba($surface, 0.8);
  backdrop-filter: blur(20rpx);
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
  background: $surface-container;
  border-radius: 50%;
  padding: 0;
}

.icon-back {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $on-surface;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  background: $surface-container-low;
  border-radius: $rounded-full;
  padding: $spacing-2;
  border: 2rpx solid transparent;
  
  &:focus-within {
    border-color: rgba($primary, 0.2);
  }
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

.clear-btn {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  padding: 0;
}

.icon-clear {
  font-family: 'Material Symbols Outlined';
  font-size: 36rpx;
  color: $outline;
}

.filter-tabs {
  display: flex;
  gap: $spacing-6;
  padding: $spacing-4 $spacing-2;
  overflow-x: auto;
  white-space: nowrap;
  
  &::-webkit-scrollbar {
    display: none;
  }
}

.tab-item {
  position: relative;
  padding: $spacing-2 0;
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $on-surface-variant;
  background: transparent;
  
  &.active {
    color: $primary;
  }
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 32rpx;
  height: 6rpx;
  background: $primary;
  border-radius: $rounded-full;
}

.results-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-4 0;
}

.summary-text {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  font-size: $font-size-body-sm;
  color: $primary;
  font-weight: $font-weight-semibold;
  background: transparent;
}

.icon-filter {
  font-family: 'Material Symbols Outlined';
  font-size: 32rpx;
}

.filter-text {
  font-size: $font-size-body-sm;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.result-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: $surface-container-lowest;
  padding: $spacing-5;
  border-radius: $rounded-xl;
  box-shadow: $shadow-soft;
  
  &:active {
    transform: scale(0.98);
  }
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.fund-header {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.fund-name {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.hot-tag {
  background: rgba($secondary, 0.1);
  color: $secondary;
  font-size: 20rpx;
  font-weight: $font-weight-bold;
  padding: 2rpx 12rpx;
  border-radius: $rounded-sm;
}

.fund-meta {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.fund-code {
  font-size: $font-size-xs;
  color: $on-surface-variant;
}

.fund-type,
.fund-risk {
  background: $surface-container;
  color: $on-surface-variant;
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: $rounded-md;
}

.card-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: $spacing-1;
  min-width: 100rpx;
}

.fund-pnl {
  font-size: $font-size-headline-md;
  font-weight: $font-weight-semibold;
  color: $loss;
  
  &.positive {
    color: $profit;
  }
}

.font-mono {
  font-family: $font-family-mono;
}

.add-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: $primary;
  color: $on-primary;
  padding: 0;
  
  &.added {
    background: $surface-container;
    color: $on-surface-variant;
  }
}

.icon-add {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  
  &.added {
    font-variation-settings: 'FILL' 1;
  }
}

.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-10 0;
  gap: $spacing-3;
}

.loading-dots {
  display: flex;
  gap: $spacing-1;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  background: rgba($primary, 0.2);
  border-radius: 50%;
  animation: bounce 1.5s infinite;
  
  &:nth-child(2) {
    animation-delay: 0.2s;
    background: rgba($primary, 0.4);
  }
  
  &:nth-child(3) {
    animation-delay: 0.4s;
    background: $primary;
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-16rpx);
  }
}

.loading-text {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
  font-weight: $font-weight-medium;
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
  color: $on-surface-variant;
}

.bg-glow {
  position: fixed;
  border-radius: 50%;
  pointer-events: none;
  opacity: 0.05;
  z-index: -1;
  
  &.top {
    top: -10%;
    right: -10%;
    width: 400rpx;
    height: 400rpx;
    background: $primary;
    filter: blur(120rpx);
  }
  
  &.bottom {
    bottom: -10%;
    left: -10%;
    width: 400rpx;
    height: 400rpx;
    background: $secondary;
    filter: blur(120rpx);
  }
}
</style>