<template>
  <view class="page">
    <view class="content">
      <!-- Search Input -->
      <view class="search-wrap">
        <image class="icon-search" src="/static/images/search.png" />
        <input 
          class="search-input" 
          type="text" 
          :placeholder="placeholder"
          v-model="searchText"
          confirm-type="search"
          @confirm="handleSearch"
          @input="handleInput"
          focus
        />
        <image v-if="searchText" class="icon-clear" @tap="clearSearch" src="/static/images/cancel.png" />
      </view>

      <!-- History Section (before search) -->
      <view v-if="!hasSearched && !searchText" class="history-section">
        <view class="section-header">
          <text class="section-title">历史记录</text>
          <text class="clear-btn" @tap="handleClearHistory">清除</text>
        </view>
        <view class="history-tags" v-if="historyTags.length">
          <text 
            v-for="tag in historyTags" 
            :key="tag" 
            class="history-tag"
            @tap="handleTagTap(tag)"
          >{{ tag }}</text>
        </view>
        <view v-else class="empty-history">暂无历史记录</view>
      </view>

      <!-- Search Results -->
      <view v-if="hasSearched" class="results-section">
        <view class="section-header">
          <text class="section-title">搜索结果</text>
          <text class="result-count">共 {{ filteredResults.length }} 条</text>
        </view>

        <view v-if="filteredResults.length" class="results-list">
          <view 
            v-for="item in filteredResults" 
            :key="item.id" 
            class="result-card"
            @tap="handleSelect(item)"
          >
            <view class="card-left">
              <text class="item-name">{{ item.name }}</text>
              <view class="item-meta">
                <text class="item-code font-mono">{{ item.code }}</text>
                <text v-if="item.type" class="item-type">{{ item.type }}</text>
              </view>
            </view>
            <view class="card-right">
              <text class="select-hint">选择</text>
            </view>
          </view>
        </view>

        <view v-else class="empty-state">
          <text class="icon-empty">search_off</text>
          <text class="empty-text">未找到相关{{ currentTypeLabel }}</text>
        </view>
      </view>

      <!-- Hot Recommendations (before search) -->
      <view v-if="!hasSearched && !searchText" class="hot-section">
        <view class="section-header">
          <text class="section-title">热门推荐</text>
        </view>
        <view class="hot-list">
          <view 
            v-for="item in hotList" 
            :key="item.id" 
            class="result-card"
            @tap="handleSelect(item)"
          >
            <view class="card-left">
              <text class="item-name">{{ item.name }}</text>
              <view class="item-meta">
                <text class="item-code font-mono">{{ item.code }}</text>
                <text v-if="item.type" class="item-type">{{ item.type }}</text>
              </view>
            </view>
            <view class="card-right">
              <text class="hot-tag">HOT</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const searchText = ref('')
const activeType = ref('fund')
const hasSearched = ref(false)
const historyTags = ref(['沪深300', '医疗健康', '000001', '低碳经济'])

const typeLabels = {
  fund: '基金',
  stock: '股票',
  hk: '港股',
  us: '美股'
}

const placeholder = computed(() => {
  const map = {
    fund: '输入基金名称或代码',
    stock: '输入股票名称或代码',
    hk: '输入港股名称或代码',
    us: '输入美股名称或代码'
  }
  return map[activeType.value] || '请输入关键词'
})

const currentTypeLabel = computed(() => typeLabels[activeType.value] || '')

const mockDatabase = [
  { id: 'f1', name: '招商中证500指数增强', code: '004192', type: '指数型', category: 'fund' },
  { id: 'f2', name: '中欧医疗健康混合A', code: '003095', type: '混合型', category: 'fund' },
  { id: 'f3', name: '易方达蓝筹精选混合', code: '005827', type: '混合型', category: 'fund' },
  { id: 'f4', name: '华夏上证50ETF联接A', code: '001051', type: '指数型', category: 'fund' },
  { id: 'f5', name: '汇添富消费行业混合', code: '000083', type: '股票型', category: 'fund' },
  { id: 'f6', name: '天弘余额宝货币', code: '000198', type: '货币型', category: 'fund' },
  { id: 'f7', name: '华安纳斯达克100', code: '040046', type: 'QDII型', category: 'fund' },
  { id: 's1', name: '贵州茅台', code: '600519.SH', type: '白酒', category: 'stock' },
  { id: 's2', name: '招商银行', code: '600036.SH', type: '银行', category: 'stock' },
  { id: 's3', name: '宁德时代', code: '300750.SZ', type: '新能源', category: 'stock' },
  { id: 's4', name: '比亚迪', code: '002594.SZ', type: '新能源汽车', category: 'stock' },
  { id: 's5', name: '中国平安', code: '601318.SH', type: '保险', category: 'stock' },
  { id: 'hk1', name: '腾讯控股', code: '00700.HK', type: '科技', category: 'hk' },
  { id: 'hk2', name: '阿里巴巴', code: '09988.HK', type: '电商', category: 'hk' },
  { id: 'hk3', name: '美团-W', code: '03690.HK', type: '本地生活', category: 'hk' },
  { id: 'hk4', name: '小米集团-W', code: '01810.HK', type: '消费电子', category: 'hk' },
  { id: 'us1', name: '苹果', code: 'AAPL', type: '科技', category: 'us' },
  { id: 'us2', name: '特斯拉', code: 'TSLA', type: '新能源汽车', category: 'us' },
  { id: 'us3', name: '英伟达', code: 'NVDA', type: '芯片', category: 'us' },
  { id: 'us4', name: '微软', code: 'MSFT', type: '科技', category: 'us' }
]

const hotList = computed(() => {
  return mockDatabase
    .filter(item => item.category === activeType.value)
    .slice(0, 8)
})

const filteredResults = computed(() => {
  const kw = searchText.value.trim().toLowerCase()
  return mockDatabase.filter(item => {
    if (item.category !== activeType.value) return false
    if (!kw) return true
    return item.name.toLowerCase().includes(kw) || 
           item.code.toLowerCase().includes(kw)
  })
})

onLoad((options) => {
  if (options?.type && typeLabels[options.type]) {
    activeType.value = options.type
  }
})

const handleInput = () => {
  hasSearched.value = !!searchText.value
}

const handleSearch = () => {
  if (!searchText.value.trim()) {
    uni.showToast({ title: '请输入搜索内容', icon: 'none' })
    return
  }
  hasSearched.value = true
  addToHistory(searchText.value.trim())
}

const clearSearch = () => {
  searchText.value = ''
  hasSearched.value = false
}

const handleClearHistory = () => {
  historyTags.value = []
}

const handleTagTap = (tag) => {
  searchText.value = tag
  hasSearched.value = true
}

const addToHistory = (tag) => {
  const list = historyTags.value.filter(t => t !== tag)
  list.unshift(tag)
  historyTags.value = list.slice(0, 10)
}

const handleSelect = (item) => {
  const pages = getCurrentPages()
  const prevPage = pages[pages.length - 2]
  if (prevPage && prevPage.$vm) {
    const vm = prevPage.$vm
    if (typeof vm.onStockSelected === 'function' && activeType.value === 'stock') {
      vm.onStockSelected(item)
    } else if (typeof vm.onFundSelected === 'function' && activeType.value === 'fund') {
      vm.onFundSelected(item)
    } else if (typeof vm.onItemSelected === 'function') {
      vm.onItemSelected(item, activeType.value)
    }
  }
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

.search-wrap {
  display: flex;
  align-items: center;
  background: $surface-container-low;
  border-radius: $rounded-full;
  padding: $spacing-2 $spacing-3;
}

.icon-search {
  width: 40rpx;
  height: 40rpx;
  margin-right: $spacing-2;
}

.search-input {
  flex: 1;
  background: transparent;
  border: none;
  padding: 0;
  font-size: $font-size-body-sm;
  color: $on-surface;
  
  &::placeholder {
    color: $on-surface-variant;
  }
}

.icon-clear {
  width: 36rpx;
  height: 36rpx;
  padding: 0 $spacing-2;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: $spacing-4 0 $spacing-3;
}

.section-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.clear-btn {
  font-size: $font-size-xs;
  color: $outline;
}

.result-count {
  font-size: $font-size-xs;
  color: $outline;
  font-family: $font-family-mono;
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
}

.empty-history {
  font-size: $font-size-body-sm;
  color: $outline;
  padding: $spacing-4 0;
}

.results-list,
.hot-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.result-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: $surface-container-lowest;
  padding: $spacing-4;
  border-radius: $rounded-lg;
  box-shadow: $shadow-soft;
  
  &:active {
    transform: scale(0.98);
  }
}

.card-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.item-name {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.item-code {
  font-size: $font-size-xs;
  color: $outline;
  background: $surface-container;
  padding: 2rpx 12rpx;
  border-radius: $rounded-sm;
}

.item-type {
  font-size: 22rpx;
  color: $on-surface-variant;
}

.card-right {
  display: flex;
  align-items: center;
}

.select-hint {
  font-size: $font-size-xs;
  color: $primary;
  font-weight: $font-weight-semibold;
}

.hot-tag {
  font-size: 20rpx;
  color: $secondary;
  background: rgba($secondary, 0.1);
  padding: 4rpx 16rpx;
  border-radius: $rounded-sm;
  font-weight: $font-weight-bold;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-12 0;
  opacity: 0.5;
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

.font-mono {
  font-family: $font-family-mono;
}
</style>
