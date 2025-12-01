<template>
  <view class="page-container">
    
    <!-- 1. 吸顶筛选栏 (修改了定位方式) -->
    <view class="sticky-header">
      <view class="filter-tabs">
        <view 
          v-for="tab in filterOptions" 
          :key="tab.value"
          class="tab-item"
          :class="{ active: activeFilter === tab.value }"
          @click="switchFilter(tab.value)"
        >
          <text class="tab-text">{{ tab.label }}</text>
          <view class="active-line" v-if="activeFilter === tab.value"></view>
        </view>
      </view>
    </view>
    
    <!-- 已删除 header-placeholder，sticky 模式下不需要占位 -->

    <!-- 2. 交易列表内容 -->
    <view class="content-area">
      
      <!-- 空状态 -->
      <view v-if="!loading && transactions.length === 0" class="empty-state">
        <image src="/static/images/empty-box.png" class="empty-img" mode="aspectFit" />
        <text class="empty-text">暂无相关交易记录</text>
      </view>

      <!-- 列表 -->
      <view v-else class="list-container">
        <view 
          v-for="(item, index) in transactions" 
          :key="item.id || index" 
          class="trade-card"
        >
          <!-- 卡片头部：名称与类型 -->
          <view class="card-header">
             <view class="header-left">
                 <text class="fund-name">{{ item.fund_name }}</text>
                 <text class="fund-code">{{ item.fund_code }}</text>
             </view>
             <view class="type-tag" :class="getTypeClass(item.type)">
                 {{ getTypeLabel(item.type) }}
             </view>
          </view>

          <view class="divider"></view>

          <!-- 卡片中部：核心数据 -->
          <view class="card-body">
              <view class="data-row">
                  <view class="data-col">
                      <text class="label">交易金额</text>
                      <text class="value money-font">{{ formatCurrency(item.amount) }}</text>
                  </view>
                  <view class="data-col align-right">
                      <text class="label">交易日期</text>
                      <text class="value">{{ formatDate(item.transaction_date) }}</text>
                  </view>
              </view>

              <view class="data-row mt-sm">
                  <view class="data-col">
                      <text class="label">确认份额</text>
                      <text class="value">{{ item.shares ? Number(item.shares).toFixed(2) : '--' }}</text>
                  </view>
                  <view class="data-col align-right">
                      <text class="label">确认净值</text>
                      <text class="value">{{ item.net_value ? Number(item.net_value).toFixed(4) : '--' }}</text>
                  </view>
              </view>
          </view>

          <!-- 卡片底部：状态 -->
          <view class="card-footer">
              <view class="status-row">
                  <view class="status-dot" :class="getStatusColor(item.status)"></view>
                  <text class="status-text" :class="getStatusColor(item.status)">
                      {{ getStatusLabel(item.status) }}
                  </text>
              </view>
              <text v-if="item.type === 'dca'" class="source-text">来自: 智能定投计划</text>
          </view>
        </view>
      </view>

      <!-- 加载更多/底部提示 -->
      <view class="loading-footer" v-if="transactions.length > 0">
          <text class="loading-text">{{ hasMore ? '加载中...' : '- 到底了 -' }}</text>
      </view>

    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { get } from '../../services/apiService.js';

// --- 状态管理 ---
const portfolioId = ref('');
const activeFilter = ref('all');
const transactions = ref([]);
const page = ref(1);
const pageSize = ref(20);
const loading = ref(false);
const hasMore = ref(true);

const filterOptions = [
    { label: '全部', value: 'all' },
    { label: '买入', value: 'buy' },
    { label: '卖出', value: 'sell' },
    { label: '定投', value: 'dca' }
];

// --- 生命周期 ---
onLoad((options) => {
    if (options.portfolio_id) {
        portfolioId.value = options.portfolio_id;
        loadData(true);
    }
});

onPullDownRefresh(() => {
    loadData(true).then(() => {
        uni.stopPullDownRefresh();
    });
});

onReachBottom(() => {
    if (hasMore.value && !loading.value) {
        loadData(false);
    }
});

// --- 核心逻辑 ---

const switchFilter = (val) => {
    if (activeFilter.value === val) return;
    activeFilter.value = val;
    loadData(true);
};

const loadData = async (reset = false) => {
    if (loading.value) return;
    loading.value = true;
    
    if (reset) {
        page.value = 1;
        hasMore.value = true;
    }

    try {
        const params = {
            page: page.value,
            page_size: pageSize.value
        };
        if (activeFilter.value !== 'all') {
            params.type = activeFilter.value;
        }

        const res = await get(`/api/transactions/portfolio/${portfolioId.value}`, params);
        
        if (res.statusCode === 200 && res.data) {
            const list = res.data.transactions || [];
            
            if (reset) {
                transactions.value = list;
            } else {
                transactions.value = [...transactions.value, ...list];
            }
            
            if (list.length < pageSize.value) {
                hasMore.value = false;
            } else {
                page.value++;
            }
        }
    } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' });
    } finally {
        loading.value = false;
    }
};

// --- 工具函数 ---

const formatDate = (str) => {
    if (!str) return '';
    const date = new Date(str);
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${date.getFullYear()}-${m}-${d}`;
};

const formatCurrency = (val) => {
    return '¥' + Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};

const getTypeClass = (type) => {
    switch(type) {
        case 'buy': return 'tag-buy'; 
        case 'sell': return 'tag-sell';
        case 'dca': return 'tag-dca';
        default: return 'tag-default';
    }
};

const getTypeLabel = (type) => {
    const map = { buy: '买入', sell: '卖出', dca: '定投', dividend: '分红' };
    return map[type] || '交易';
};

const getStatusColor = (status) => {
    if (status === 'completed' || status === 'success') return 'status-success';
    if (status === 'pending' || status === 'processing') return 'status-warning';
    return 'status-error';
};

const getStatusLabel = (status) => {
    const map = { completed: '确认成功', success: '确认成功', pending: '确认中', failed: '交易失败' };
    return map[status] || status;
};

</script>

<style lang="scss" scoped>
/* 引用一致的主题色 */
$primary: #2a806c; 
$bg-page: #f5f7fa;
$white: #ffffff;
$text-main: #1f2937;
$text-sub: #6b7280;
$border-color: #f3f4f6;

/* 功能色 */
$red: #ef4444;    /* 买入/涨 */
$green: #10b981;  /* 卖出/跌 */
$blue: #3b82f6;   /* 辅助 */
$orange: #f59e0b; /* 等待 */

.page-container {
    min-height: 100vh;
    background-color: $bg-page;
    padding-bottom: 20px;
}

/* 1. 吸顶筛选栏 - 重点修改 */
.sticky-header {
    /* 关键：使用 sticky 代替 fixed */
    position: sticky;
    /* 0px 意味着它会粘在父容器可视区域的最顶部（即原生导航栏下方） */
    top: 0;
    width: 100%;
    background-color: $white;
    z-index: 99;
    box-shadow: 0 2px 10px rgba(0,0,0,0.03);
}

.filter-tabs {
    display: flex;
    justify-content: space-around;
    height: 48px;
    align-items: center;
}

.tab-item {
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    position: relative;
    padding: 0 12px;
}

.tab-text {
    font-size: 14px;
    color: $text-sub;
    transition: color 0.3s;
}

.tab-item.active .tab-text {
    color: $primary;
    font-weight: 600;
}

.active-line {
    position: absolute;
    bottom: 0;
    width: 24px;
    height: 3px;
    background-color: $primary;
    border-radius: 3px 3px 0 0;
}

/* 2. 内容区 */
.content-area {
    padding: 16px;
}

.list-container {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

/* 交易卡片 */
.trade-card {
    background-color: $white;
    border-radius: 16px;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
}

.header-left {
    display: flex;
    flex-direction: column;
    max-width: 70%;
}

.fund-name {
    font-size: 15px;
    font-weight: 600;
    color: $text-main;
    margin-bottom: 4px;
    line-height: 1.4;
}

.fund-code {
    font-size: 12px;
    color: $text-sub;
    background-color: #f3f4f6;
    padding: 2px 6px;
    border-radius: 4px;
    align-self: flex-start;
}

.type-tag {
    font-size: 11px;
    padding: 4px 8px;
    border-radius: 8px;
    font-weight: 500;
    
    &.tag-buy { background-color: rgba($red, 0.1); color: $red; }
    &.tag-sell { background-color: rgba($blue, 0.1); color: $blue; }
    &.tag-dca { background-color: rgba($primary, 0.1); color: $primary; }
    &.tag-default { background-color: #f3f4f6; color: $text-sub; }
}

.divider {
    height: 1px;
    background-color: $border-color;
    margin-bottom: 12px;
}

.card-body {
    margin-bottom: 12px;
}

.data-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    &.mt-sm { margin-top: 8px; }
}

.data-col {
    display: flex;
    flex-direction: column;
    
    &.align-right { align-items: flex-end; }
}

.label {
    font-size: 11px;
    color: $text-sub;
    margin-bottom: 2px;
}

.value {
    font-size: 13px;
    color: $text-main;
    font-family: monospace;
    font-weight: 500;
    
    &.money-font {
        font-size: 16px;
        font-weight: bold;
    }
}

.card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #fafafa;
    margin: -16px;
    margin-top: 0;
    padding: 10px 16px;
    border-top: 1px solid $border-color;
    border-radius: 0 0 16px 16px;
}

.status-row {
    display: flex;
    align-items: center;
}

.status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-right: 6px;
    
    &.status-success { background-color: $green; }
    &.status-warning { background-color: $orange; }
    &.status-error { background-color: $text-sub; }
}

.status-text {
    font-size: 11px;
    
    &.status-success { color: $green; }
    &.status-warning { color: $orange; }
    &.status-error { color: $text-sub; }
}

.source-text {
    font-size: 10px;
    color: $text-sub;
}

/* 空状态与加载 */
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-top: 60px;
}
.empty-img { width: 120px; height: 120px; opacity: 0.5; margin-bottom: 16px; }
.empty-text { font-size: 13px; color: $text-sub; }

.loading-footer {
    padding: 20px 0;
    text-align: center;
}
.loading-text { font-size: 12px; color: #9ca3af; }
</style>