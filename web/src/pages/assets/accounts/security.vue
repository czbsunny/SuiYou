<template>
  <view class="account-detail-page">
    <!-- 1. 顶部总览卡片 -->
    <view class="summary-section animate-fade-in">
      <view class="summary-card card-warm">
        <view class="balance-content">
          <view class="header-row">
            <view class="label-group" @tap="toggleVisibility">
              <text class="label">总资产 (元)</text>
              <image 
                :src="isAssetHidden ? '/static/images/eye-close.png' : '/static/images/eye-open.png'" 
                class="eye-icon"
              ></image>
            </view>
            <image 
              src="/static/images/settings-gray.png" 
              class="settings-icon" 
              @tap="handleEditAccount"
            ></image>
          </view>

          <view class="amount-row">
            <text class="amount num-font" :class="{ 'is-masked': isAssetHidden }">
              {{ isAssetHidden ? '******' : formatMoney(totalBalance) }}
            </text>
          </view>

          <view class="stats-row">
            <view class="stat-item">
              <text class="stat-label">今日盈亏</text>
              <text class="stat-val num-font" :class="todayProfit >= 0 ? 'profit' : 'loss'">
                {{ isAssetHidden ? '****' : (todayProfit >= 0 ? '+' : '') + formatMoney(todayProfit) }}
              </text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
              <text class="stat-label">累计盈亏</text>
              <text class="stat-val num-font" :class="totalProfit >= 0 ? 'profit' : 'loss'">
                {{ isAssetHidden ? '****' : (totalProfit >= 0 ? '+' : '') + formatMoney(totalProfit) }}
              </text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 快捷操作区 -->
    <view class="action-bar">
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('buy')">
        <view class="icon-box icon-buy">
          <image src="/static/assets/actions/buy.png" class="action-icon"></image>
        </view>
        <text class="action-label">买入</text>
      </view>
      
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('sell')">
        <view class="icon-box icon-sell">
          <image src="/static/assets/actions/sell.png" class="action-icon"></image>
        </view>
        <text class="action-label">卖出</text>
      </view>
      
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('transfer')">
        <view class="icon-box icon-transfer">
          <image src="/static/assets/actions/transfer.png" class="action-icon"></image>
        </view>
        <text class="action-label">转账</text>
      </view>
      
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('query')">
        <view class="icon-box icon-query">
          <image src="/static/assets/actions/query.png" class="action-icon"></image>
        </view>
        <text class="action-label">查询</text>
      </view>
    </view>

    <!-- 3. 资产列表 -->
    <view class="items-section">
      <view class="section-header">
        <text class="title">资产列表</text>
      </view>

      <view class="item-list">
        <view 
          v-for="(item, index) in assets" 
          :key="index" 
          class="asset-item-card card-warm" 
          hover-class="item-active"
          @tap="navToItemDetail(item)"
        >
          <view class="item-left">
            <view class="item-icon-rect">
              <image :src="getAssetIconUrl(item)" class="item-icon"></image>
            </view>
            <view class="item-info">
              <text class="item-name">{{ item.name }}</text>
              <text class="item-subtitle">{{ item.subtitle }}</text>
            </view>
          </view>
          
          <view class="item-right">
            <view class="amount-box">
              <text class="item-amount num-font">{{ isAssetHidden ? '****' : formatMoney(item.balance) }}</text>
              <text v-if="item.profit !== undefined" class="item-profit" :class="item.profit >= 0 ? 'profit' : 'loss'">
                {{ isAssetHidden ? '' : (item.profit >= 0 ? '+' : '') + formatMoney(item.profit) }}
              </text>
            </view>
            <image src="/static/images/arrow-right.png" class="arrow-icon"></image>
          </view>
        </view>
      </view>
    </view>

    <view class="safe-area-bottom" style="height: 40rpx;"></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getAccountById } from '@/services/accountService.js';
import { useConfigStore } from '@/stores/config.js';

const isAssetHidden = ref(false);
const accountInfo = ref(null);
const assets = ref([]);
const configStore = useConfigStore();

onLoad(async (options) => {
  try {
    const id = options.id;
    const res = await getAccountById(id);
    if (res && res.account) {
      const instInfo = configStore.getInstitutionByCode(res.account.institution);
      accountInfo.value = {
        ...res.account,
        accountName: res.account.accountName || instInfo.instName + '(' + res.account.institutionIdentifier + ')',
        logoUrl: instInfo.logoUrl,
      };
      initAssets();
    }
  } catch (e) {
    console.error('获取账户详情失败:', e);
    initAssets();
  }
});

const initAssets = () => {
  assets.value = [
    {
      id: 'available',
      name: '可用余额',
      subtitle: '可用于交易的资金',
      balance: 15000.50,
      category: 'CASH',
      subCategory: 'AVAILABLE'
    },
    {
      id: 'stock',
      name: '股票',
      subtitle: '持仓市值',
      balance: 85000.00,
      profit: 3200.50,
      category: 'SECURITY',
      subCategory: 'STOCK'
    },
    {
      id: 'fund',
      name: '基金',
      subtitle: '持仓市值',
      balance: 32000.00,
      profit: -850.00,
      category: 'SECURITY',
      subCategory: 'FUND'
    }
  ];
};

const totalBalance = computed(() => assets.value.reduce((sum, item) => sum + (item.balance || 0), 0));
const todayProfit = computed(() => 1250.30);
const totalProfit = computed(() => assets.value.reduce((sum, item) => sum + (item.profit || 0), 0));

const getAssetIconUrl = (item) => {
  const iconMap = {
    'AVAILABLE': '/static/assets/icons/available.png',
    'STOCK': '/static/assets/icons/stock.png',
    'FUND': '/static/assets/icons/fund.png'
  };
  return iconMap[item.subCategory] || '/static/assets/icons/default.png';
};

const formatMoney = (val) => Math.abs(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
const toggleVisibility = () => { isAssetHidden.value = !isAssetHidden.value; uni.vibrateShort(); };
const handleEditAccount = () => uni.navigateTo({ url: `/pages/accounts/edit?id=${accountInfo.value?.id || ''}` });

const handleAction = (type) => {
  const data = encodeURIComponent(JSON.stringify({ account: accountInfo.value }));
  switch (type) {
    case 'buy':
      uni.navigateTo({ url: `/pages/security/buy?data=${data}` });
      break;
    case 'sell':
      uni.navigateTo({ url: `/pages/security/sell?data=${data}` });
      break;
    case 'transfer':
      uni.navigateTo({ url: `/pages/security/transfer?data=${data}` });
      break;
    case 'query':
      uni.navigateTo({ url: `/pages/security/query?data=${data}` });
      break;
    default:
      uni.showToast({ title: '功能开发中', icon: 'none' });
  }
};

const navToItemDetail = (item) => {
  const routes = { 'AVAILABLE': 'available', 'STOCK': 'stock', 'FUND': 'fund' };
  const page = routes[item.subCategory];
  const data = encodeURIComponent(JSON.stringify(item));
  if (page) {
    uni.navigateTo({ url: `/pages/assets/items/${page}?data=${data}` });
  } else {
    uni.showToast({ title: item.name + '在开发中', icon: 'none' });
  }
};
</script>

<style lang="scss" scoped>
.account-detail-page {
  min-height: 100vh;
  background-color: $bg-page;
}

.summary-section {
  padding: $spacing-sm $spacing-base;
}

.summary-card {
  padding: $spacing-base;
  
  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .label-group {
      display: flex;
      align-items: center;
      gap: 12rpx;
      .label { font-size: 28rpx; color: $text-muted; }
      .eye-icon { width: 32rpx; height: 32rpx; opacity: 0.4; }
    }
    
    .settings-icon {
      width: 40rpx;
      height: 40rpx;
      opacity: 0.5;
    }
  }

  .amount-row {
    margin-bottom: 32rpx;
    .amount { 
      font-size: 64rpx; 
      font-weight: $fw-bold; 
      color: $text-main; 
      &.is-masked { color: $text-placeholder; letter-spacing: 4rpx; }
    }
  }

  .stats-row {
    display: flex;
    align-items: center;
    padding-top: 30rpx;
    
    .stat-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 6rpx;
      .stat-label { font-size: 24rpx; color: $text-muted; }
      .stat-val { 
        font-size: 30rpx; 
        font-weight: $fw-semibold; 
        &.profit { color: $text-gain; }
        &.loss { color: $text-loss; }
      }
    }
    
    .stat-divider {
      width: 2rpx;
      height: 48rpx;
      background-color: $bg-subtle;
    }
  }
}

.items-section {
  padding: $spacing-base;
  .section-header {
    margin-bottom: $spacing-base;
    .title { font-size: 32rpx; font-weight: $fw-semibold; color: $text-main; }
  }
}

.action-bar {
  display: flex;
  justify-content: space-around;
  padding: $spacing-md $spacing-md;
  margin: 0 $spacing-base;
  background-color: $bg-white;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    
    .action-label { 
      font-size: 24rpx; 
      color: $text-regular; 
      font-weight: 500; 
    }
  }
}

.icon-box {
  width: 88rpx; 
  height: 88rpx; 
  border-radius: 28rpx;
  @include flex-center; 
  box-shadow: $shadow-card;
  transition: transform 0.1s;
  
  .action-icon { 
    width: 56rpx; 
    height: 56rpx; 
  }
}

.icon-buy { background-color: $bg-buy; }
.icon-sell { background-color: $bg-sell; }
.icon-transfer { background-color: $bg-transfer; }
.icon-query { background-color: $bg-query; }

.items-section {
  padding: $spacing-base;
  .section-header {
    margin-bottom: $spacing-base;
    .title { font-size: 32rpx; font-weight: $fw-semibold; color: $text-main; }
  }
}

.asset-item-card {
  padding: 28rpx; 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: $spacing-base;
  background-color: $bg-white;
  border-radius: $radius-lg;
  
  .item-left {
    display: flex; 
    align-items: center; 
    gap: 24rpx;
    .item-icon-rect {
      width: 88rpx; 
      height: 88rpx; 
      background-color: $bg-page; 
      border-radius: $radius-base;
      @include flex-center; 
      .item-icon { width: 64rpx; height: 64rpx; }
    }
    .item-info {
      display: flex;
      flex-direction: column;
      gap: 6rpx;
      .item-name { font-size: 28rpx; font-weight: $fw-semibold; color: $text-main; }
      .item-subtitle { font-size: 22rpx; color: $text-muted; }
    }
  }
  
  .item-right {
    display: flex; 
    align-items: center; 
    gap: 12rpx;
    .amount-box {
      text-align: right;
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 4rpx;
      .item-amount { font-size: 30rpx; font-weight: $fw-bold; color: $text-main; }
      .item-profit { 
        font-size: 22rpx; 
        font-weight: $fw-medium;
        &.profit { color: $text-gain; }
        &.loss { color: $text-loss; }
      }
    }
    .arrow-icon { width: 24rpx; height: 24rpx; opacity: 0.2; }
  }
}

.item-active { 
  background-color: $bg-subtle; 
  transform: scale(0.98); 
}
.animate-fade-in { animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10rpx); } to { opacity: 1; transform: translateY(0); } }

.hover-opacity:active {
  opacity: 0.7;
  .icon-box { transform: scale(0.92); }
}
</style>