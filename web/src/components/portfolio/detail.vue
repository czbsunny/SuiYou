<template>
  <view class="detail-page">
       <!-- 头部信息卡片 -->
       <view class="header-card">
           <view class="title-row">
               <text class="portfolio-name">{{ portfolio.name || '加载中...' }}</text>
               <text class="tag">{{ portfolio.tags?.[0] || '自定义' }}</text>
           </view>
           
           <text class="total-value">{{ formatCurrency(portfolio.currentValue) }}</text>
           
           <view class="stats-row">
               <view class="stat-item">
                   <text class="stat-label">日收益</text>
                   <text :class="[
                     'stat-num',
                     {
                       'text-gray': portfolio.dailyReturn === 0,
                       'text-gain': portfolio.dailyReturn > 0,
                       'text-loss': portfolio.dailyReturn < 0
                     }
                   ]">
                      {{ portfolio.dailyReturn > 0 ? '+' : '' }}{{ formatNumber(portfolio.dailyReturn) }}
                   </text>
               </view>
               <view class="stat-item">
                   <text class="stat-label">持有收益</text>
                   <text :class="[
                     'stat-num',
                     {
                       'text-gray': portfolio.totalReturn === 0,
                       'text-gain': portfolio.totalReturn > 0,
                       'text-loss': portfolio.totalReturn < 0
                     }
                   ]">
                      {{ portfolio.totalReturn > 0 ? '+' : '' }}{{ formatNumber(portfolio.totalReturn) }}
                   </text>
               </view>
               <view class="stat-item text-right">
                   <text class="stat-label">累计收益</text>
                   <text :class="[
                     'stat-num',
                     {
                       'text-gray': portfolio.totalReturn === 0,
                       'text-gain': portfolio.totalReturn > 0,
                       'text-loss': portfolio.totalReturn < 0
                     }
                   ]">
                      {{ portfolio.totalReturn > 0 ? '+' : '' }}{{ formatNumber(portfolio.totalReturn) }}
                   </text>
               </view>
           </view>
       </view>
        
        <!-- 操作菜单网格 -->
        <view class="action-grid">
            <view 
                v-for="(item, i) in actions" 
                :key="i" 
                @click="handleActionClick(item)" 
                class="action-item"
            >
                <view class="icon-box">
                    <image :src="item.icon" class="menu-icon" />
                </view>
                <text class="action-text">{{ item.name }}</text>
            </view>
        </view>

        <!-- 持仓基金列表 -->
        <view class="holdings-card">
            <view class="list-header">
                <text class="section-title">持仓基金</text>
                <view class="header-labels">
                    <text>日收益</text>
                    <text class="ml-4">持有收益</text>
                </view>
            </view>
            
            <view class="holdings-list">
                <!-- 加载中状态 -->
                <view v-if="loading" class="loading-state">
                    <text class="loading-text">加载数据中...</text>
                </view>

                <!-- 有持仓数据 -->
                <template v-else-if="holdings && holdings.length > 0">
                  <view v-for="h in holdings" :key="h.id" class="holding-item">
                      <view class="item-left">
                          <text class="fund-name">{{ h.name }}</text>
                          <text class="fund-value">市值: {{ formatNumber(h.holdingAmount) }}</text>
                      </view>
                      
                      <view class="item-right">
                          <text :class="['value-text', h.dailyReturn >= 0 ? 'text-gain' : 'text-loss']">
                              {{ h.dailyReturn > 0 ? '+' : '' }}{{ formatNumber(h.dailyReturn) }}
                          </text>
                          <text :class="['rate-text', h.dailyReturn >= 0 ? 'sub-gain' : 'sub-loss']">
                              {{ h.dailyReturnRate > 0 ? '+' : '' }}{{ formatPercent(h.dailyReturnRate) }}
                          </text>
                      </view>
                      
                      <view class="item-right ml-2">
                          <text :class="['value-text', h.returnValue >= 0 ? 'text-gain' : 'text-loss']">
                              {{ h.returnValue > 0 ? '+' : '' }}{{ formatNumber(h.returnValue) }}
                          </text>
                          <text :class="['rate-text', h.returnValue >= 0 ? 'sub-gain' : 'sub-loss']">
                              {{ h.returnRate > 0 ? '+' : '' }}{{ formatPercent(h.returnRate) }}
                          </text>
                      </view>
                  </view>
                </template>
                
                <!-- 空状态 -->
                <view v-else class="empty-state">
                    <view class="empty-icon-bg">
                        <image src="/static/icons/wallet.png" class="empty-icon" />
                    </view>
                    <text class="empty-text">暂无持仓，该组合为空</text>
                    <view class="buy-now-btn" @click="handleBuy">
                        一键买入
                    </view>
                </view>
            </view>
        </view>

    <!-- 底部固定栏 -->
    <view class="bottom-bar">
        <view class="tool-btn" @click="handleSell">
            <image src="/static/images/sell.png" class="bar-icon" />
            <text class="bar-text">卖出</text>
        </view>
        <view class="tool-btn" @click="handleRebalance">
            <image src="/static/images/rebalance.png" class="bar-icon" />
            <text class="bar-text">调仓</text>
        </view>
        
        <view class="main-actions">
            <view class="action-btn btn-outline" @click="handleDCA">
                <image src="/static/images/dca-blue.png" class="btn-icon" />
                <text class="btn-text">定投</text>
            </view>
            <view class="action-btn btn-primary" @click="handleBuy">
                <image src="/static/images/buy-inverse.png" class="btn-icon" />
                <text class="btn-text">买入</text>
            </view>
        </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { formatCurrency, formatNumber, formatPercent } from '../../services/formatUtil';
// 引入你的 API 服务
import {  getPortfolioItems } from '../../services/portfolioService';

const portfolioId = ref('');
const loading = ref(true);
const portfolio = ref({
    id: '',
    name: '',
    tags: [],
    totalValue: 0,
    dailyReturn: 0,
    totalReturn: 0
});
const holdings = ref([]);

const actions = [
    { id: 'dca_plan', name: '定投计划', icon: '/static/images/dca-plan.png', path: '/components/portfolio/dca' },
    { id: 'records', name: '交易记录', icon: '/static/images/transaction.png', path: '/components/portfolio/transactions' },
    { id: 'allocation', name: '组合配置', icon: '/static/images/allocation.png', path: '/components/portfolio/allocation' },
    { id: 'analysis', name: '资产分析', icon: '/static/images/analysis.png', path: '/components/portfolio/analysis' },
];

// 页面加载
onLoad((options) => {
    if (options.id) {
        portfolioId.value = options.id;
        loadData();
    }
});

// 每次显示时刷新数据（保证金额最新）
onShow(() => {
    if (portfolioId.value) {
        loadData();
    }
});

const loadData = async () => {
    loading.value = true;
    try {
        const [infoRes, itemsRes] = await Promise.all([
            uni.getStorageSync('cachedPortfolio'),

            getPortfolioItems(portfolioId.value)
        ]);

        if (infoRes) {
            portfolio.value = infoRes;
        }

        console.log("portfolio:", portfolio.value)
        
        if (itemsRes.success) {
            holdings.value = itemsRes.data;
        }
    } catch (error) {
        console.error('加载组合详情失败:', error);
        uni.showToast({ title: '加载失败', icon: 'none' });
    } finally {
        loading.value = false;
    }
};

// 菜单点击处理
const handleActionClick = (item) => {
    if (item.path) {
        uni.navigateTo({
            url: `${item.path}?id=${portfolioId.value}`
        });
    } else {
        uni.showToast({ title: '功能开发中', icon: 'none' });
    }
};

// 底部按钮操作
const handleBuy = () => {
    uni.navigateTo({ 
      url: `/components/trade/buy?portfolioId=${portfolioId.value}` 
    });
};

const handleSell = () => {
    uni.navigateTo({ 
      url: `/components/trade/sell?portfolioId=${portfolioId.value}` 
    });
};

const handleRebalance = () => {
    uni.navigateTo({ 
      url: `/components/trade/rebalance?portfolioId=${portfolioId.value}` 
    });
};

const handleDCA = () => {
    uni.navigateTo({ 
      url: `/components/trade/dca?portfolioId=${portfolioId.value}` 
    });
};
</script>

<style lang="scss" scoped>

.detail-page {
  min-height: 100vh;
  background-color: $bg-cream;
  display: flex;
  flex-direction: column;
  width: 100%;
  padding-bottom: calc(96px + env(safe-area-inset-bottom)); /* pb-24 equivalent */
}

/* 头部卡片 */
.header-card {
  margin: 16px 16px 0 16px;
  background-color: $primary;
  border-radius: 24px;
  padding: 24px;
  color: #ffffff;
  box-shadow: 0 10px 15px -3px rgba(42, 128, 108, 0.3);
  margin-bottom: 16px;
}

.title-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 8px;
}

.portfolio-name {
  font-size: 18px;
  font-weight: 500;
  opacity: 0.9;
}

.tag {
  font-size: 10px;
  background-color: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.8);
  margin-left: 8px;
}

.total-value {
  font-size: 36px;
  font-weight: bold;
  font-family: monospace;
  margin-bottom: 24px;
  display: block;
}

.stats-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.text-right { text-align: right; }

.stat-label {
  font-size: 12px;
  opacity: 0.6;
  margin-bottom: 4px;
  display: block;
}

.stat-num {
  font-family: monospace;
  font-weight: 500;
  display: block;

  &.text-gain { color: $text-gain-light; }
  &.text-loss { color: $text-loss-light; }
  &.text-gray { color: $text-muted; }
}

/* 操作网格 */
.action-grid {
  background-color: $bg-white;
  border-radius: 24px;
  margin: 0 16px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: transform 0.1s;
  
  &:active {
    transform: scale(0.95);
  }
}

.icon-box {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: $bg-cream;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-sub;
}

.menu-icon {
  width: 20px;
  height: 20px;
}

.action-text {
  font-size: 12px;
  color: $text-sub;
  margin-top: 8px;
}

/* 持仓列表卡片 */
.holdings-card {
  margin: 16px 16px 0 16px;
  background-color: $bg-white;
  border-radius: 24px;
  padding: 16px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  min-height: 300px;
}

.list-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 8px;
}

.section-title {
  font-weight: bold;
  color: $text-main;
}

.header-labels {
  font-size: 12px;
  color: $text-muted;
  display: flex;
  flex-direction: row;
}

.ml-4 { margin-left: 16px; }
.ml-2 { margin-left: 8px; }

.holdings-list {
  display: flex;
  flex-direction: column;
}

/* 列表项 */
.holding-item {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid $bg-subtle;

  &:last-child {
    border-bottom: 0;
    padding-bottom: 0;
    margin-bottom: 0;
  }
}

.item-left {
  flex: 1;
  padding-right: 16px;
}

.fund-name {
  font-size: 14px;
  font-weight: 500;
  color: $text-main;
  margin-bottom: 4px;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.fund-value {
  font-size: 12px;
  color: $text-muted;
  font-family: monospace;
  display: block;
}

.item-right {
  text-align: right;
  min-width: 80px;
}

.value-text {
  display: block;
  font-size: 14px; /* text-sm */
  font-weight: 500;
  
  &.text-gain { color: $color-gain; }
  &.text-loss { color: $color-loss; }
}

.rate-text {
  display: block;
  font-size: 10px;
  margin-top: 2px;
  opacity: 0.8;

  &.sub-gain { color: $color-gain; }
  &.sub-loss { color: $color-loss; }
  &.text-gray { color: $text-muted; }
}

.text-gain { color: $color-gain; }
.text-loss { color: $color-loss; }

/* 状态样式 */
.loading-state {
    padding: 40px 0;
    text-align: center;
    color: $text-muted;
    font-size: 12px;
}

.empty-state {
  text-align: center;
  padding: 48px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.empty-icon-bg {
  width: 48px;
  height: 48px;
  background-color: $bg-subtle;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.empty-icon { width: 24px; height: 24px; opacity: 0.3; }

.empty-text {
  color: $text-muted;
  font-size: 12px;
  margin-bottom: 16px;
}

.buy-now-btn {
  padding: 6px 16px;
  border-radius: 9999px;
  border: 1px solid $primary;
  color: $primary;
  font-size: 12px;
  font-weight: bold;
}

/* 底部栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $bg-white;
  padding: 12px 24px;
  /* 适配安全区 */
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid $bg-subtle;
  box-shadow: 0 -4px 10px rgba(0,0,0,0.03);
  z-index: 50;
}

.tool-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: $text-muted;
  min-width: 48px;
  
  &:active {
    color: $text-main;
  }
}

.bar-icon {
  width: 24px;
  height: 24px;
}

.bar-text {
  font-size: 12px;
  margin-top: 4px;
}

.main-actions {
  display: flex;
  flex-direction: row;
  flex: 1;
  margin-left: 16px;
  gap: 12px;
}

.action-btn {
  flex: 1;
  padding: 10px 0;
  border-radius: 9999px;
  font-weight: bold;
  font-size: 14px;
  display: flex;
  flex-direction: column; /* 垂直布局：图标在上，文字在下 */
  align-items: center;
  justify-content: center;
  /* 修正：如果是左右布局，改为 row */
  flex-direction: row; 
}

.btn-outline {
  border: 1px solid $primary;
  color: $primary;
  background-color: transparent;
  
  &:active {
    background-color: rgba(42, 128, 108, 0.05);
  }
}

.btn-primary {
  background-color: $primary;
  color: #ffffff;
  box-shadow: 0 10px 15px -3px rgba(42, 128, 108, 0.2);
  
  &:active {
    transform: scale(0.98);
  }
}

.btn-icon {
  width: 20px;
  height: 20px;
  margin-right: 4px;
}

.btn-text {
    font-size: 14px;
}
</style>