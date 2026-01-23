<template>
  <view class="page-container">
    <!-- 1. 顶部净值总览卡片 -->
    <NetWorthCard :assets="allAssets" />

    <!-- 2. 财务体检 -->
    <HealthGrid :assets="allAssets" />

    <!-- 3. 视角切换开关 (只保留 category | institution 切换) -->
    <AssetViewToggle v-model="viewMode" />

    <!-- 加载状态 -->
    <view v-if="loading && !allAssets.length" class="loading-container">
      <uni-load-more status="loading" />
    </view>

    <!-- 4. 核心内容区 -->
    <view class="content-view">
      <!-- 视角 A：按资产类别 (5个折叠大块) -->
      <CategoryListView 
        v-if="viewMode === 'category'"
        :list="categoryGroupedList"
        @item-click="handleItemClick"
        @add-click="handleAddAsset"
      />

      <!-- 视角 B：按机构卡包 (一张张独立的账户卡片) -->
      <InstitutionListView 
        v-else
        :list="accountFlatList"
        @account-click="handleAccountClick"
        @add-account-click="handleAddAccount"
      />
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { getAssets } from '../../services/assetService.js';
import { ASSET_INSTITUTION_DISPLAY } from '@/configs/assets.js';

// 引入组件
import NetWorthCard from '../../components/assets/NetWorthCard.vue';
import HealthGrid from '../../components/assets/HealthGrid.vue';
import AssetViewToggle from '@/components/assets/AssetViewToggle.vue';
import CategoryListView from '@/components/assets/CategoryListView.vue';
import InstitutionListView from '@/components/assets/InstitutionListView.vue';

const configStore = useConfigStore();

// === 核心状态 ===
const allAssets = ref([]); // 所有的资产原子项
const loading = ref(true);
const viewMode = ref('category'); // 'category' | 'institution'

// --- 逻辑 A：聚合为“5大类”数据结构 ---
const categoryGroupedList = computed(() => {
  return configStore.topCategories.map(cat => {
    // 找出属于该大类的资产项
    const items = allAssets.value
      .filter(asset => asset.category === cat.categoryCode)
      .map(asset => ({
        ...asset,
        // 补充机构 Logo 信息给明细行使用
        instInfo: configStore.getInstitutionByCode(asset.institution)
      }));
    
    const total = items.reduce((sum, i) => sum + (Number(i.totalBalance) || 0), 0);

    return {
      ...cat,
      totalBalance: total,
      items: items
    };
  });
});

// --- 逻辑 B：聚合为“账户卡片”扁平列表 (Wallet Stream) ---
const accountFlatList = computed(() => {
  const accountMap = {};
  const instMap = configStore.institutionMap;

  allAssets.value.forEach(asset => {
    const accId = asset.accountId;
    if (!accountMap[accId]) {
      const instInfo = instMap[asset.institution] || { instName: '其他', instType: 'OTHER' };
      const instType = instInfo.instType || 'OTHER';
      
      accountMap[accId] = {
        id: accId,
        instName: instInfo.instName,
        instCode: asset.institution,
        accountName: asset.accountName || '默认账户',
        identifier: asset.institutionIdentifier || '',
        logoUrl: instInfo.logoUrl,
        // 颜色从业态配置中获取，作为卡片背景
        bgColor: ASSET_INSTITUTION_DISPLAY[instType]?.color || '#4b5563',
        totalBalance: 0,
        itemCount: 0,
        subText: asset.visibleScope === 'PRIVATE' ? '私有账户' : '家庭共享'
      };
    }
    accountMap[accId].totalBalance += Number(asset.totalBalance);
    accountMap[accId].itemCount += 1;
  });

  // 返回拍平后的数组，并按金额排序
  return Object.values(accountMap).sort((a, b) => b.totalBalance - a.totalBalance);
});

// --- 数据加载 ---
const loadData = async () => {
  loading.value = true;
  try {
    const res = await getAssets();
    allAssets.value = res.assets || [];
  } catch (err) {
    console.error('加载资产失败:', err);
  } finally {
    loading.value = false;
  }
};

onShow(() => {
  loadData();
});

// --- 路由跳转 ---
const handleAddAsset = (catCode = '') => {
  uni.navigateTo({ url: `/pages/assets/add?category=${catCode}` });
};

const handleAddAccount = () => {
  uni.navigateTo({ url: `/pages/assets/add-account` });
};

const handleItemClick = (item) => {
  uni.navigateTo({ url: `/pages/assets/item?id=${item.id}` });
};

const handleAccountClick = (account) => {
  uni.navigateTo({ url: `/pages/assets/account-detail?id=${account.id}` });
};
</script>

<style lang="scss">
page {
  background-color: #F9F8F4;
}

.page-container {
  min-height: 100vh;
  background-color: #F9F8F4;
  padding-bottom: env(safe-area-inset-bottom); // 适配全面屏底部
}

.loading-container {
  padding: 100rpx 0;
}

.content-view {
  padding: 0 32rpx 40rpx;
}
</style>