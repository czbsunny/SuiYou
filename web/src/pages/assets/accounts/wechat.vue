<template>
  <view class="account-detail-page">
    <!-- 1. 顶部总览卡片 (保持不变) -->
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
              <text class="stat-label">可用资产</text>
              <text class="stat-val num-font">{{ isAssetHidden ? '****' : formatMoney(availableAssets) }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">已分配资产</text>
              <text class="stat-val num-font">{{ isAssetHidden ? '****' : formatMoney(allocatedAssets) }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 修改后的快捷操作区 -->
    <view class="action-bar">
      <!-- 记一笔 -->
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('record')">
        <view class="icon-box icon-income">
          <image src="/static/assets/actions/plus.png" class="action-icon"></image>
        </view>
        <text class="action-label">记一笔</text>
      </view>
      
      <!-- 转账 -->
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('transfer')">
        <view class="icon-box icon-transfer">
          <image src="/static/assets/actions/transfer.png" class="action-icon"></image>
        </view>
        <text class="action-label">转账</text>
      </view>
      
      <!-- 校准 (对应资产对账图标) -->
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('reconcile')">
        <view class="icon-box icon-liquid">
          <image src="/static/assets/actions/scale.png" class="action-icon"></image>
        </view>
        <text class="action-label">校准</text>
      </view>
    </view>

    <!-- 3. 资产列表 (保持不变) -->
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
            </view>
          </view>
          
          <view class="item-right">
            <view class="amount-box">
              <text class="item-amount num-font">{{ isAssetHidden ? '****' : formatMoney(item.totalBalance) }}</text>
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
// ... script 逻辑保持不变 ...
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
      accountInfo.value = res.account;
      assets.value = accountInfo.value.assets || [];
    }
  } catch (e) {
    console.error('获取账户详情失败:', e);
  }
});

const totalBalance = computed(() => assets.value.reduce((sum, item) => sum + (item.totalBalance || 0), 0));
const availableAssets = computed(() => assets.value.reduce((sum, item) => sum + (item.availableBalance || 0), 0));
const allocatedAssets = computed(() => assets.value.reduce((sum, item) => sum + (item.frozenBalance || 0), 0));

const getAssetIconUrl = (item) => {
  const subCategories = configStore.getSubCategoriesByCode(item.category);
  const subCategory = subCategories.find(s => s.categoryCode === item.subCategory);
  return subCategory?.iconUrl || '';
};

const formatMoney = (val) => Math.abs(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
const toggleVisibility = () => { isAssetHidden.value = !isAssetHidden.value; uni.vibrateShort(); };
const handleEditAccount = () => uni.navigateTo({ url: `/pages/accounts/edit?id=${accountInfo.value.id}` });

const handleAction = (type) => {
  if (type === 'transfer') {
    const data = encodeURIComponent(JSON.stringify({ account: accountInfo.value }));
    uni.navigateTo({ url: `/pages/home/transfer?data=${data}` });
  } else if (type === 'record') {
    const data = encodeURIComponent(JSON.stringify({ account: accountInfo.value }));
    uni.navigateTo({ url: `/pages/home/record?data=${data}` });
  } else {
    const data = encodeURIComponent(JSON.stringify({ account: accountInfo.value, recordAsProfit: false }));
    uni.navigateTo({ url: `/pages/home/reconcile?data=${data}` });
  }
};

const navToItemDetail = (item) => {
  // 保持原有跳转逻辑
  const routes = { 'CHANGE': 'cash', 'MINIFUND': 'cashPlus', 'FUND': 'fund' };
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
      .stat-val { font-size: 30rpx; font-weight: $fw-semibold; color: $text-sub; }
    }
  }
}

/* 修改后的 Action Bar 样式 */
.action-bar {
  display: flex;
  justify-content: space-around;
  padding: $spacing-md $spacing-md;

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
  width: 96rpx; 
  height: 96rpx; 
  border-radius: 32rpx; /* 更圆润的矩形 */
  @include flex-center; 
  box-shadow: $shadow-card;
  transition: transform 0.1s;
  
  .action-icon { 
    width: 64rpx; 
    height: 64rpx; 
  }
}

/* 语义化背景色 */
.icon-income { background-color: $bg-income; }
.icon-transfer { background-color: $bg-transfer; }
.icon-liquid { background-color: $bg-liquid; }

/* 资产列表部分 (保持原有逻辑) */
.items-section {
  padding: 0 $spacing-base;
  .section-header {
    margin-bottom: $spacing-base;
    .title { font-size: 32rpx; font-weight: $fw-semibold; color: $text-main; }
  }
}

.asset-item-card {
  padding: 24rpx; display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-base;
  .item-left {
    display: flex; align-items: center; gap: 24rpx;
    .item-icon-rect {
      width: 88rpx; height: 88rpx; background-color: $bg-page; border-radius: $radius-base;
      @include flex-center; .item-icon { width: 64rpx; height: 64rpx; }
    }
    .item-name { font-size: 28rpx; font-weight: $fw-semibold; color: $text-main; }
  }
  .item-right {
    display: flex; align-items: center; gap: 12rpx;
    .amount-box {
      text-align: right;
      .item-amount { font-size: 30rpx; font-weight: $fw-bold; color: $text-main; }
    }
    .arrow-icon { width: 24rpx; height: 24rpx; opacity: 0.2; }
  }
}

.item-active { background-color: $bg-subtle; transform: scale(0.98); }
.animate-fade-in { animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10rpx); } to { opacity: 1; transform: translateY(0); } }

.hover-opacity:active {
  opacity: 0.7;
  .icon-box { transform: scale(0.92); }
}
</style>