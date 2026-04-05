<template>
  <view class="main-container">
    <scroll-view 
      class="main-scroll" 
      scroll-y 
      enable-back-to-top
      refresher-enabled 
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onPullDownRefresh"
    >
      <view class="content-wrapper">
        
        <!-- 1. 资产总览卡片 (card-warm) -->
        <view class="card-warm summary-card">
          <view class="stats-row">
            <!-- 余额 -->
            <view class="stats-item">
              <view class="label-box" @tap="toggleVisibility">
                <text class="label">当前余额/市值</text>
                <image :src="isAssetHidden ? '/static/images/eye-close.png' : '/static/images/eye-open.png'" class="icon-sm" mode="aspectFit" />
              </view>
              <text class="value num-font" :class="{ 'is-blur': isAssetHidden }">
                {{ isAssetHidden ? '******' : formatMoney(totalBalance) }}
              </text>
            </view>
            
            <!-- 收益 -->
            <view class="stats-item align-right">
              <view class="label-box">
                <text class="label">今日收益</text>
              </view>
              <text class="value num-font" :class="getReturnClass(dailyProfit)">
                {{ dailyProfit >= 0 ? '+' : '' }}{{ isAssetHidden ? '****' : formatMoney(dailyProfit) }}
              </text>
            </view>
          </view>
        </view>

        <!-- 2. 快捷操作区 (仅保留校准和收益) -->
        <view class="action-bar-container">
          <!-- 更新收益 -->
          <view class="action-item" @tap="assets.length > 0 && navigateToReconcile('INVESTMENT_RETURN', assets[0])">
            <view class="icon-box icon-profit">
              <image src="/static/assets/actions/chart-trending.png" class="action-icon" mode="aspectFit" />
            </view>
            <text class="action-label">录入盈亏</text>
          </view>
          
          <!-- 校准 -->
          <view class="action-item" @tap="assets.length > 0 && navigateToReconcile('ADJUSTMENT', assets[0])">
            <view class="icon-box icon-liquid">
              <image src="/static/assets/actions/scale.png" class="action-icon" mode="aspectFit" />
            </view>
            <text class="action-label">资产校准</text>
          </view>
        </view>

        <!-- 3. 资产明细卡片 -->
        <view class="card-warm list-card">
          <view class="list-header">
            <text class="title">对应资产</text>
            <!-- <image src="/static/images/settings-gray.png" class="icon-settings" @tap="handleEditAccount" mode="aspectFit" /> -->
          </view>

          <view class="asset-item" v-for="(asset, index) in assets" :key="index">
            <view class="item-left">
              <view class="icon-rect">
                <image :src="getAssetIcon(asset)" class="icon-asset" mode="aspectFit" />
              </view>
              <view class="item-info">
                <text class="i-name">{{ asset.name }}</text>
                <text class="i-type">{{ asset.subCategoryName || '通用资产' }}</text>
              </view>
            </view>
            <text class="item-amount num-font">{{ isAssetHidden ? '****' : formatMoney(asset.totalBalance) }}</text>
          </view>
        </view>

        <view class="safe-area-bottom"></view>
      </view>
    </scroll-view>


  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { formatCurrency } from '@/utils/formatUtil';
import { getAccountById } from '@/services/accountService.js';
import { useConfigStore } from '@/stores/config.js';

const accountInfo = ref(null);
const assets = ref([]);
const isAssetHidden = ref(false);
const isRefreshing = ref(false);
const configStore = useConfigStore();

// 导航到对账页面相关
const navigateToReconcile = (type, asset) => {
  const data = {
    ...asset,
    recordAsProfit: type === 'INVESTMENT_RETURN'
  };
  uni.navigateTo({
    url: `/pages/home/reconcile?data=${encodeURIComponent(JSON.stringify(data))}`
  });
};

// 计算总余额：根据assets数组求和
const totalBalance = computed(() => {
  return assets.value.reduce((sum, asset) => sum + (asset.totalBalance || 0), 0);
});

// 计算日收益：暂时默认为0，因为JSON数据中没有提供
const dailyProfit = computed(() => 0);

const fetchAccountData = async (id) => {
  const res = await getAccountById(id);
  accountInfo.value = res.account;
  assets.value = res.account.assets || [];
  uni.setNavigationBarTitle({ title: accountInfo.value.name || '资产账户' });
};

onLoad(async (options) => {
  await fetchAccountData(options.id);
});

// 页面显示时刷新数据（从子页面返回时触发）
onShow(async () => {
  if (accountInfo.value?.id) {
    await fetchAccountData(accountInfo.value.id);
  }
});

const onPullDownRefresh = async () => {
  isRefreshing.value = true;
  await fetchAccountData(accountInfo.value.id);
  isRefreshing.value = false;
  uni.vibrateShort();
};

const formatMoney = (val) => formatCurrency(val === null || val === undefined ? 0 : val, '');

const getReturnClass = (val) => (val > 0 ? 'text-gain' : (val < 0 ? 'text-loss' : 'text-main'));
const toggleVisibility = () => { isAssetHidden.value = !isAssetHidden.value; uni.vibrateShort(); };
const getAssetIcon = (asset) => {
  const subCategories = configStore.getSubCategoriesByCode(asset.category);
  const subCategory = subCategories.find(s => s.categoryCode === asset.subCategory);
  return subCategory?.iconUrl || '/static/images/default-asset.png';
};
const handleEditAccount = () => uni.navigateTo({ url: `/pages/accounts/edit?id=${accountInfo.value.id}` });
</script>

<style lang="scss" scoped>
.main-container { height: 100vh; background-color: $bg-page; }
.main-scroll { height: 100%; }
.content-wrapper { padding: $spacing-base; }

/* 1. 顶部汇总卡片 */
.summary-card {
  margin-bottom: $spacing-sm; /* 卡片间距紧凑 */
  
  .stats-row {
    display: flex; justify-content: space-between; align-items: center;
  }
  
  .stats-item {
    display: flex; flex-direction: column;
    &.align-right { align-items: flex-end; }
    
    .label-box {
      display: flex; align-items: center; margin-bottom: 8rpx; height: 40rpx;
      .label { font-size: 24rpx; color: $text-muted; }
      .icon-sm { width: 32rpx !important; height: 32rpx !important; margin-left: 10rpx; }
    }
    
    .value {
      font-size: 48rpx; font-weight: $fw-bold; color: $text-main;
      &.is-blur { filter: blur(6px); opacity: 0.3; }
    }
  }
}

/* 2. 操作栏 */
.action-bar-container {
  display: flex; justify-content: space-around; padding: $spacing-md 0;
  
  .action-item {
    display: flex; flex-direction: column; align-items: center; gap: 12rpx;
    
    .icon-box {
      width: 90rpx; height: 90rpx; border-radius: 28rpx; @include flex-center;
      box-shadow: $shadow-card;
      
      .action-icon { width: 52rpx !important; height: 52rpx !important; }
    }
    
    .action-label { font-size: 24rpx; color: $text-regular; font-weight: 500; }
  }
}

.icon-profit { background-color: $bg-profit; }
.icon-liquid { background-color: $bg-liquid; }

/* 3. 资产列表卡片 */
.list-card {
  .list-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 30rpx;
    .title { font-size: 28rpx; font-weight: $fw-bold; color: $text-main; }
    .icon-settings { width: 32rpx !important; height: 32rpx !important; opacity: 0.4; }
  }

  .asset-item {
    display: flex; justify-content: space-between; align-items: center; padding: 20rpx 0;
    
    .item-left {
      display: flex; align-items: center; gap: 20rpx;
      .icon-rect { 
        width: 72rpx; height: 72rpx; border-radius: 12rpx; 
        background-color: $bg-page; @include flex-center;
        .icon-asset { width: 44rpx !important; height: 44rpx !important; }
      }
      .item-info {
        display: flex; flex-direction: column;
        .i-name { font-size: 26rpx; color: $text-main; font-weight: $fw-semibold; }
        .i-type { font-size: 18rpx; color: $text-placeholder; }
      }
    }
    .item-amount { font-size: 30rpx; font-weight: $fw-bold; color: $text-main; }
  }
}

/* 工具 */
.num-font { font-family: $font-family-money; @include tabular-nums; }
.text-gain { color: $text-gain; }
.text-loss { color: $text-loss; }
.text-main { color: $text-main; }
.truncate { @include text-ellipsis; }
.safe-area-bottom { height: calc(env(safe-area-inset-bottom) + 30rpx); }
</style>