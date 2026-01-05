<template>
  <view class="page-container">
    <!-- 1. 净值趋势 (传入所有资产计算后的净值) -->
    <NetWorthCard :assets="allAssets" />

    <!-- 2. 财务体检 -->
    <HealthGrid :assets="allAssets" />

    <!-- 3. 资产概览 -->
    <AssetViewToggle v-model="viewMode" />

    <view v-if="loading && !allAssets.length" class="loading-container">
      <uni-load-more status="loading" />
    </view>

    <view class="content-view">
      <!-- 视角 A：按类别 (折叠块) -->
      <CategoryListView 
        v-if="viewMode === 'category'" 
        :list="categoryGroupedList"
        @item-click="handleItemClick"
        @add-click="handleAddAsset"
      />

      <!-- 视角 B：按账户/机构 -->
      <InstitutionListView 
        v-else 
        :list="institutionGroupedList"
        @item-click="handleItemClick"
        @add-click="handleAddAsset"
      />
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import NetWorthCard from '../../components/assets/NetWorthCard.vue';
import HealthGrid from '../../components/assets/HealthGrid.vue';
import { getAssets } from '../../services/assetService.js';
import { useConfigStore } from '@/stores/config.js'

import AssetViewToggle from '@/components/assets/AssetViewToggle.vue';
import CategoryListView from '@/components/assets/CategoryListView.vue';
import InstitutionListView from '@/components/assets/InstitutionListView.vue';

const configStore = useConfigStore();

// === 状态变量 ===
const allAssets = ref([]); // 存储后端返回的原始数组
const allAccounts = ref([]); // 存储后端返回的原始数组
const loading = ref(true);
const viewMode = ref('category'); // 'category' | 'institution'

// --- 数据聚合：视角 A (按 5 大类) ---
const categoryGroupedList = computed(() => {
  return configStore.topCategories.map(cat => {
    const items = allAssets.value
      .filter(acc => acc.category === cat.categoryCode)
      .map(acc => ({
        ...acc,
        instInfo: configStore.getInstitutionByCode(acc.institution)
      }));
    
    const total = items.reduce((sum, i) => sum + (Number(i.totalBalance) || 0), 0);
    return {
      ...cat,
      totalBalance: total,
      items: items
    };
  });
});

// --- 数据聚合：视角 B (按机构平台) ---
const institutionGroupedList = computed(() => {
  const groups = {};
  const instMap = configStore.institutionMap;

  allAccounts.value.forEach(acc => {
    const instCode = acc.institution || 'OTHER';
    if (!groups[instCode]) {
      const instInfo = instMap[instCode] || { instName: '其他', logoUrl: '/static/icons/default.png' };
      groups[instCode] = { ...instInfo, total: 0, accounts: [] };
    }
    groups[instCode].total += Number(acc.totalBalance);
    groups[instCode].accounts.push(acc);
  });

  return Object.values(groups).sort((a, b) => b.total - a.total);
});

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const res = await getAssets();
    // 此时 res 就是你刚才给我的那个包含 assets 数组的对象
    allAssets.value = res.assets || [];
  } catch (err) {
    console.error('加载资产项失败:', err);
  } finally {
    loading.value = false;
  }
};

onShow(() => {
  loadData();
});

const handleAddAsset = () => {
  uni.navigateTo({
    url: `/pages/assets/add`
  });
};

const handleItemClick = (item) => {
  uni.navigateTo({
    url: `/pages/assets/detail?id=${item.id}`
  });
};
</script>

<style lang="scss">
page {
  background-color: #F9F8F4; // 统一使用你预览页的米色
}
.page-container {
  min-height: 100vh;
  background-color: #F9F8F4;
  padding-bottom: 40rpx;
}

.loading-container {
  padding: 40rpx 0;
}

.global-add-btn {
  width: 64rpx; height: 64rpx; background: #fff; border-radius: 20rpx;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
  .plus-icon { width: 28rpx; height: 28rpx; opacity: 0.6; }
  &:active { transform: scale(0.9); }
}

.content-view { padding: 0 32rpx; }

</style>