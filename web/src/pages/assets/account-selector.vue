<template>
  <view class="selector-page">
    <!-- 1. 账户列表区 -->
    <scroll-view class="list-scroll" scroll-y>
      <view class="list-inner">
        <view 
          v-for="account in sortedAccounts" 
          :key="account.id"
          class="account-card card-warm"
        >
          <!-- 账户头部 -->
          <view class="card-header" @click="toggleAccountExpand(account.id)">
            <view class="card-left">
              <view class="icon-wrapper">
                <image :src="account.logoUrl" mode="aspectFit" class="bank-icon" />
              </view>
              <view class="name-box">
                <view class="name-row">
                  <text class="main-name">{{ account.institutionName }}</text>
                  <text v-if="account.accountName" class="sub-name">{{ account.accountName }}</text>
                </view>
                <text class="id-text num-font">**** {{ account.institutionIdentifier || '0000' }}</text>
              </view>
            </view>
            
            <view class="card-right">
              <view class="balance-box">
                <text class="currency">￥</text>
                <text class="amount num-font">{{ formatMoney(account.totalBalance) }}</text>
              </view>
              <uni-icons 
                :type="expandedAccountId === account.id ? 'arrowup' : 'arrowdown'" 
                size="14" 
                color="#ccd4d2"
              ></uni-icons>
            </view>
          </view>
          
          <!-- 资产项列表 -->
          <view v-if="expandedAccountId === account.id" class="asset-list animate-slide-down">
            <view 
              v-for="asset in account.assets" 
              :key="asset.id"
              class="asset-item"
              @click.stop="selectAsset(account.id, asset.id)"
            >
              <view class="asset-left">
                <view class="radio-circle" :class="{ 'radio-active': selectedAssetId === asset.id }">
                  <view class="radio-inner" v-if="selectedAssetId === asset.id"></view>
                </view>
                <text class="asset-name" :class="{ 'text-active': selectedAssetId === asset.id }">
                  {{ asset.name }}
                </text>
              </view>
              <view class="asset-balance">
                <text class="currency">￥</text>
                <text class="amount num-font">{{ formatMoney(asset.totalBalance) }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 2. 底部确认区 -->
    <view class="footer-action card-warm">
      <!-- 动态显示：仅在传参标识为可设置默认时显示 -->
      <view class="default-setting" v-if="canSetDefault" @click="isSetDefault = !isSetDefault">
        <view class="checkbox-box" :class="{ 'checkbox-active': isSetDefault }">
          <uni-icons v-if="isSetDefault" type="checkmarkempty" size="14" color="#FFFFFF"></uni-icons>
        </view>
        <text class="default-text">设为记账默认账户</text>
      </view>
      
      <button class="btn-primary" @click="confirmSelection">确定选择</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getAccounts } from '@/services/accountService.js';
import { useConfigStore } from '@/stores/config.js';

const configStore = useConfigStore();
const allAccounts = ref([]);

// 状态控制
const expandedAccountId = ref(null);
const selectedAccountId = ref(null);
const selectedAssetId = ref(null);
const isSetDefault = ref(false);
const canSetDefault = ref(false);

onLoad(async (options) => {
  if (options.data) {
    const data = JSON.parse(decodeURIComponent(options.data));
    canSetDefault.value = data.canSetDefault === 'true';
    const initAccId = data.initAccountId ? parseInt(data.initAccountId) : null;
    const initAssetId = data.initAssetId ? parseInt(data.initAssetId) : null;
    const assignAccountId = data.assignAccountId ? parseInt(data.assignAccountId) : null;
    
    await loadAccounts(initAccId, initAssetId, assignAccountId);
  }
});

const loadAccounts = async (initAccId, initAssetId, assignAccountId) => {
  try {
    const res = await getAccounts();
    const instMap = configStore.institutionMap;
    if (assignAccountId) {
      res.accounts = res.accounts.filter(a => a.id === assignAccountId);
    }

    const processed = res.accounts
      .filter(a => a.assets?.length > 0 && a.assets.some(asset => asset.category === 'LIQUID'))
      .map(a => {
        const instConfig = instMap[a.institution] || {};
        const liquidAssets = a.assets
          .filter(asset => asset.category === 'LIQUID')
          .sort((a, b) => a.sortOrder - b.sortOrder);
        return {
          ...a,
          accountName: instConfig.instName +'('+a.institutionIdentifier +')',
          institutionName: instConfig.instName,
          logoUrl: instConfig.logoUrl,
          assets: liquidAssets,
          totalBalance: liquidAssets.reduce((acc, asset) => acc + asset.totalBalance, 0)
        };
      }) || [];
    
    allAccounts.value = processed;

    // 3. 处理回显逻辑
    if (initAccId && initAssetId) {
      const targetAcc = processed.find(a => a.id === initAccId);
      if (targetAcc) {
        selectedAccountId.value = initAccId;
        selectedAssetId.value = initAssetId;
        // 如果有匹配的回显项，自动展开该账户
        expandedAccountId.value = initAccId;
      }
    }
  } catch (e) {
    console.error('加载账户失败:', e);
  }
};

const sortedAccounts = computed(() => {
  return [...allAccounts.value].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0));
});

const toggleAccountExpand = (accountId) => {
  expandedAccountId.value = expandedAccountId.value === accountId ? null : accountId;
  uni.vibrateShort({ style: 'light' });
};

const selectAsset = (accountId, assetId) => {
  selectedAccountId.value = accountId;
  selectedAssetId.value = assetId;
  uni.vibrateShort();
};

const formatMoney = (val) => Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });

const confirmSelection = () => {
  if (!selectedAssetId.value) {
    return uni.showToast({ title: '请选择具体资产', icon: 'none' });
  }

  const account = allAccounts.value.find(a => a.id === selectedAccountId.value);
  const asset = account.assets.find(as => as.id === selectedAssetId.value);

  uni.$emit('acceptAccountFromSelector', { 
    account,
    asset,
    isSetDefault: isSetDefault.value
  });
  
  uni.navigateBack();
};
</script>

<style lang="scss" scoped>
.selector-page {
  height: 100vh;
  background-color: $bg-page;
  display: flex;
  flex-direction: column;
}

.list-scroll {
  flex: 1;
  .list-inner { padding: $spacing-md; }
}

.account-card {
  margin-bottom: $spacing-base;
  border-radius: $radius-lg;
  overflow: hidden;
  padding: 0 !important;

  .card-header {
    padding: 32rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: $bg-white;
    &:active { background-color: $bg-subtle; }
  }

  .card-left {
    display: flex; align-items: center; gap: 24rpx;
    .icon-wrapper {
      width: 72rpx; height: 72rpx; background-color: $bg-page;
      border-radius: 20rpx; @include flex-center;
      .bank-icon { width: 48rpx; height: 48rpx; }
    }
    .name-box {
      .name-row {
        display: flex; align-items: baseline; gap: 12rpx;
        .main-name { font-size: 28rpx; font-weight: $fw-bold; color: $text-main; }
        .sub-name { font-size: 22rpx; color: $text-sub; }
      }
      .id-text { font-size: 20rpx; color: $text-muted; margin-top: 4rpx; }
    }
  }

  .card-right {
    display: flex; align-items: center; gap: 16rpx;
    .balance-box {
      text-align: right;
      .currency { font-size: 18rpx; color: $text-muted; }
      .amount { font-size: 30rpx; color: $text-sub; font-weight: $fw-bold; }
    }
  }

  .asset-list {
    padding: 0 32rpx 24rpx;
    background-color: $bg-white;
    .asset-item {
      display: flex; justify-content: space-between; align-items: center;
      padding: 24rpx 0; border-top: 1rpx solid $gray-100;
      .asset-left {
        display: flex; align-items: center; gap: 20rpx;
        .radio-circle {
          width: 32rpx; height: 32rpx; border: 3rpx solid $gray-300;
          border-radius: 50%; @include flex-center; transition: all 0.2s;
          &.radio-active { border-color: $primary; background-color: $primary; }
          .radio-inner { width: 12rpx; height: 12rpx; background-color: $white; border-radius: 50%; }
        }
        .asset-name { font-size: 26rpx; color: $text-main; &.text-active { color: $primary; font-weight: $fw-semibold; } }
      }
      .asset-balance {
        .currency { font-size: 18rpx; color: $text-muted; }
        .amount { font-size: 26rpx; color: $text-regular; font-weight: $fw-medium; }
      }
    }
  }
}

/* 底部操作区 */
.footer-action {
  background-color: $bg-white;
  padding: $spacing-sm $spacing-base;
  border-top-left-radius: $radius-lg;
  border-top-right-radius: $radius-lg;
  box-shadow: 0 -12rpx 32rpx rgba(50, 46, 43, 0.06);

  .default-setting {
    display: flex; align-items: center; gap: 16rpx;
    margin-bottom: 32rpx; padding-left: 8rpx;
    .checkbox-box {
      width: 36rpx; height: 36rpx; border: 3rpx solid $gray-300;
      border-radius: 10rpx; @include flex-center;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      &.checkbox-active { background-color: $primary; border-color: $primary; }
    }
    .default-text { font-size: 24rpx; color: $text-sub; font-weight: $fw-medium; }
  }
}

.safe-area-spacer { height: 180rpx; }

.animate-slide-down {
  animation: slideDown 0.25s ease-out forwards;
}
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10rpx); max-height: 0; }
  to { opacity: 1; transform: translateY(0); max-height: 1000rpx; }
}
</style>