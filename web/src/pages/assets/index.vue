<template>
  <view class="page-container">
    <!-- 1. 顶部净值总览卡片 (由 assets 计算结果驱动) -->
    <NetWorthCard :assets="allAssets" />

    <!-- 2. 加载状态：仅在初次进入且无缓存数据时显示 -->
    <view v-if="loading && !allAccounts.length" class="loading-container">
      <uni-load-more status="loading" />
    </view>

    <!-- 3. 核心内容区：唯一的机构/卡包视图 -->
    <view class="content-view">
      <AccountListView 
        :list="accountFlatList"
        @manage-click="handleManageAccount"
        @account-click="handleAccountClick"
        @add-asset-click="handleAddAsset"
      />
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue';
import { onShow, onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { getAccounts } from '../../services/accountService.js';
import { ASSET_INSTITUTION_DISPLAY } from '@/configs/assets.js';

// 引入核心组件
import NetWorthCard from '../../components/assets/NetWorthCard.vue';
import AccountListView from '../../components/assets/AccountListView.vue';


const configStore = useConfigStore();

const allAssets = ref([]);
const allAccounts = ref([]);
const loading = ref(true);
const dataLoaded = ref(false);

const accountFlatList = computed(() => {
  if (!dataLoaded.value) {
    return [];
  }

  const instMap = configStore.institutionMap;
  console.log('[DEBUG] accountFlatList 计算 - instMap:', allAccounts.value);
  return allAccounts.value
    .filter(acc => acc.status === 1)
    .map(acc => {
      const instConfig = instMap[acc.institution] || {};
      const instType = instConfig.instType || 'OTHER';
      const subItems = acc.assets || [];

      return {
        id: acc.id,
        sortOrder: acc.sortOrder, 
        
        instName: instConfig.instName || '未知机构',
        instCode: acc.institution,
        accountName: acc.accountName,
        identifier: acc.institutionIdentifier,
        logoUrl: instConfig.logoUrl || '/static/icons/default-bank.png',
        
        bgColor: acc.themeColor || ASSET_INSTITUTION_DISPLAY[instType]?.color || '#4b5563',
        totalBalance: acc.totalBalance || subItems.reduce((sum, i) => sum + (Number(i.totalBalance) || 0), 0), 
        itemCount: subItems.length,
        
        subText: acc.visibleScope === 'PRIVATE' ? '私有账户' : '家庭共享',
        yesterdayProfit: acc.yesterdayProfit || 0
      };
    })
    .sort((a, b) => {
      return a.sortOrder - b.sortOrder;
    });
});

const fetchAccounts = async () => {
  try {
    const res = await getAccounts();
    allAccounts.value = res.accounts || [];
    
    const assets = [];
    allAccounts.value.forEach(acc => {
      if (acc.assets && acc.assets.length > 0) {
        assets.push(...acc.assets);
      }
    });
    allAssets.value = assets;
    
    dataLoaded.value = true;
  } catch (err) {
    console.error('加载账户失败:', err);
    dataLoaded.value = true;
  }
};

const loadData = async () => {
  loading.value = true;
  await fetchAccounts();
  loading.value = false;
};

// --- 生命周期与全局监听 ---
onLoad(() => {
  uni.$on('refreshAccountList', () => {
    fetchAccounts();
  });
});

onUnmounted(() => {
  uni.$off('refreshAccountList');

});

onShow(() => {
  loadData();
});

// 创建新的资产项
const handleAddAsset = () => {
  uni.navigateTo({ url: `/pages/assets/category-selector?mode=create` });
};

// 查看卡片内资产明细（账户详情）
const handleAccountClick = (account) => {
  uni.navigateTo({ url: `/pages/assets/account-detail?id=${account.id}` });
};

// 进入账户管理（排序、归档、彻底删除）
const handleManageAccount = () => {
  uni.navigateTo({ url: `/pages/assets/manage-account` });
};

</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: 80rpx;
}

.loading-container {
  padding: 100rpx 32rpx;
}

.content-view {
  padding: 0 32rpx;
  margin-bottom: 40rpx;
}
</style>