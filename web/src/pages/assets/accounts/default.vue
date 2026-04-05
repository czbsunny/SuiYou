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
                {{ isAssetHidden ? '******' : formatMoney(accountInfo?.balance) }}
              </text>
            </view>
            
            <!-- 收益 -->
            <view class="stats-item align-right">
              <view class="label-box">
                <text class="label">今日收益</text>
              </view>
              <text class="value num-font" :class="getReturnClass(accountInfo?.dailyProfit)">
                {{ accountInfo?.dailyProfit >= 0 ? '+' : '' }}{{ isAssetHidden ? '****' : formatMoney(accountInfo?.dailyProfit) }}
              </text>
            </view>
          </view>
        </view>

        <!-- 2. 快捷操作区 (仅保留校准和收益) -->
        <view class="action-bar-container">
          <!-- 更新收益 -->
          <view class="action-item" @tap="openModal('INVESTMENT_RETURN')">
            <view class="icon-box icon-profit">
              <image src="/static/assets/actions/profit.png" class="action-icon" mode="aspectFit" />
            </view>
            <text class="action-label">录入盈亏</text>
          </view>
          
          <!-- 校准余额 -->
          <view class="action-item" @tap="openModal('ADJUSTMENT')">
            <view class="icon-box icon-liquid">
              <image src="/static/assets/actions/scale.png" class="action-icon" mode="aspectFit" />
            </view>
            <text class="action-label">余额校准</text>
          </view>
        </view>

        <!-- 3. 资产明细卡片 -->
        <view class="card-warm list-card">
          <view class="list-header">
            <text class="title">对应资产</text>
            <image src="/static/images/settings-gray.png" class="icon-settings" @tap="handleEditAccount" mode="aspectFit" />
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

    <!-- 录入弹窗 -->
    <uni-popup ref="inputPopup" type="dialog">
      <uni-popup-dialog 
        mode="input" 
        :title="modalTitle" 
        placeholder="请输入金额" 
        :value="tempValue"
        @confirm="submitTransaction"
      ></uni-popup-dialog>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { formatCurrency } from '@/utils/formatUtil';
import { getAccountById } from '@/services/accountService.js';
import { createTransaction } from '@/services/transactionService.js';

const accountInfo = ref(null);
const assets = ref([]);
const isAssetHidden = ref(false);
const isRefreshing = ref(false);

// 弹窗相关
const inputPopup = ref(null);
const modalType = ref(''); // INVESTMENT_RETURN or ADJUSTMENT
const modalTitle = ref('');
const tempValue = ref('');

onLoad(async (options) => {
  await fetchAccountData(options.id);
});

const fetchAccountData = async (id) => {
  const res = await getAccountById(id);
  accountInfo.value = res.account;
  assets.value = res.account.assets || [];
  uni.setNavigationBarTitle({ title: accountInfo.value.name });
};

const onPullDownRefresh = async () => {
  isRefreshing.value = true;
  await fetchAccountData(accountInfo.value.id);
  isRefreshing.value = false;
  uni.vibrateShort();
};

const openModal = (type) => {
  modalType.value = type;
  modalTitle.value = type === 'ADJUSTMENT' ? '校准最新余额' : '录入盈亏金额';
  // 校准时默认带入当前余额，收益默认置空
  tempValue.value = type === 'ADJUSTMENT' ? accountInfo.value.balance.toString() : '';
  inputPopup.value.open();
};

// 核心逻辑：创建交易记录
const submitTransaction = async (val) => {
  const amountNum = parseFloat(val);
  if (isNaN(amountNum)) return;

  const transactionDTO = {
    type: modalType.value,
    targetAssetId: assets.value[0]?.id || accountInfo.value.id, // 优先绑定具体资产项
    transTime: new Date().toISOString(),
    description: modalType.value === 'ADJUSTMENT' ? '余额校准' : '手动录入投资收益'
  };

  if (modalType.value === 'ADJUSTMENT') {
    transactionDTO.targetAmount = amountNum; // 传入目标金额
    transactionDTO.amount = 0; // 校准通常 delta 由后端算，或者这里传 0
  } else {
    transactionDTO.amount = amountNum; // 收益金额
  }

  try {
    await createTransaction(transactionDTO);
    uni.showToast({ title: '记录已生成', icon: 'success' });
    await fetchAccountData(accountInfo.value.id);
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const formatMoney = (val) => formatCurrency(val, '');
const getReturnClass = (val) => (val > 0 ? 'text-gain' : (val < 0 ? 'text-loss' : 'text-main'));
const toggleVisibility = () => { isAssetHidden.value = !isAssetHidden.value; uni.vibrateShort(); };
const getAssetIcon = (asset) => '/static/images/default-asset.png'; // 占位
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