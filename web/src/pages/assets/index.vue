<template>
  <view class="page-container">
    <!-- 1. 顶部净值总览卡片 (由 assets 计算结果驱动) -->
    <NetWorthCard :assets="allAssets" />

    <!-- 2. 财务体检指标 (由后端接口计算结果驱动) -->
    <HealthGrid :assets="allAssets" />

    <!-- 3. 加载状态：仅在初次进入且无缓存数据时显示 -->
    <view v-if="loading && !allAccounts.length" class="loading-container">
      <uni-load-more status="loading" />
    </view>

    <!-- 4. 核心内容区：唯一的机构/卡包视图 -->
    <view class="content-view">
      <InstitutionListView 
        :list="accountFlatList"
        @manage-click="handleManageAccount"
        @account-click="handleAccountClick"
        @add-account-click="handleAddAccount"
        @add-asset-click="handleAddAssetWithAccount"
      />
    </view>
    
    <!-- 底部安全区占位 -->
    <view class="safe-area-bottom"></view>
  </view>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue';
import { onShow, onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { getAssets } from '../../services/assetService.js';
import { getAccounts } from '../../services/accountService.js';
import { ASSET_INSTITUTION_DISPLAY } from '@/configs/assets.js';

// 引入核心组件
import NetWorthCard from '../../components/assets/NetWorthCard.vue';
import HealthGrid from '../../components/assets/HealthGrid.vue';
import InstitutionListView from '../../components/assets/InstitutionListView.vue';

const configStore = useConfigStore();

// === 核心状态 ===
const allAssets = ref([]);   // 逻辑资产原子项 (如：某卡下的余额、某理财)
const allAccounts = ref([]); // 物理账户容器 (如：工行卡、支付宝账号)
const loading = ref(true);

const accountFlatList = computed(() => {
  const instMap = configStore.institutionMap;

  return allAccounts.value
    .filter(acc => acc.status === 1)
    .map(acc => {
      const instConfig = instMap[acc.institution] || {};
      const instType = instConfig.instType || 'OTHER';
      const subItems = allAssets.value.filter(asset => String(asset.accountId) === String(acc.id));

      return {
        id: acc.id,
        sortOrder: acc.sortOrder, 
        
        instName: instConfig.instName || '未知机构',
        instCode: acc.institution,
        accountName: acc.accountName,
        identifier: acc.institutionIdentifier,
        logoUrl: instConfig.logoUrl || '/static/icons/default-bank.png',
        
        // 视觉与金额逻辑
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

const fetchAssets = async () => {
  try {
    const res = await getAssets();
    allAssets.value = res.assets || [];
  } catch (err) {
    console.error('加载资产项失败:', err);
  }
};

const fetchAccounts = async () => {
  try {
    const res = await getAccounts();
    allAccounts.value = res.accounts || [];
  } catch (err) {
    console.error('加载账户失败:', err);
  }
};

const loadData = async () => {
  loading.value = true;
  // 并行请求，减少白屏时间
  await Promise.all([fetchAssets(), fetchAccounts()]);
  loading.value = false;
};

// --- 生命周期与全局监听 ---
onLoad(() => {
  // 监听跨页面刷新事件（如添加账户后返回）
  uni.$on('refreshAccountList', () => {
    fetchAccounts();
    fetchAssets();
  });
});

onUnmounted(() => {
  uni.$off('refreshAccountList');
});

onShow(() => {
  loadData();
});

// --- 路由跳转逻辑 ---

// 点击卡片右上角“小加号”，直接在该账户下新增资产
const handleAddAssetWithAccount = (account) => {
  uni.navigateTo({ 
    url: `/pages/assets/add?accountId=${account.id}&instCode=${account.instCode}` 
  });
};

// 创建新的账户容器（卡片）
const handleAddAccount = () => {
  uni.navigateTo({ url: `/pages/assets/add-account` });
};

// 查看卡片内资产明细（账户详情）
const handleAccountClick = (account) => {
  uni.navigateTo({ url: `/pages/assets/account-detail?id=${account.id}` });
};

// 查看具体某个资产原子的详情
const handleItemClick = (item) => {
  uni.navigateTo({ url: `/pages/assets/item-detail?id=${item.id}` });
};

// 进入账户管理（排序、归档、彻底删除）
const handleManageAccount = () => {
  uni.navigateTo({ url: `/pages/assets/manage-account` });
};
</script>

<style lang="scss">
page {
  background-color: #F9F8F4; // iOS 温暖白
}

.page-container {
  min-height: 100vh;
  background-color: #F9F8F4;
  /* 适配不同端的底部，留出呼吸空间 */
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
}

.loading-container {
  padding: 100rpx 0;
}

.content-view {
  /* 保持与之前一致的左右间距 */
  padding: 0 32rpx;
}

.safe-area-bottom {
  height: env(safe-area-inset-bottom);
}
</style>