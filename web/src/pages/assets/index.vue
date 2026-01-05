<template>
  <view class="page-container">
    <!-- 1. 净值趋势 (传入所有资产计算后的净值) -->
    <NetWorthCard :assets="allAssets" />

    <!-- 2. 财务体检 -->
    <HealthGrid :assets="allAssets" />

    <!-- 3. 资产概览 -->
    <AssetViewToggle 
      v-model:viewMode="viewMode" 
      v-model:displayMode="displayMode"
      :isAllExpanded="isAllExpanded"
      @toggle-all="handleToggleAll"
    />

    <view v-if="loading && !allAssets.length" class="loading-container">
      <uni-load-more status="loading" />
    </view>

    <view class="content-view">
      <!-- 视角 A：按类别 (折叠块) -->
      <CategoryListView 
        ref="categoryRef"
        v-if="viewMode === 'category'" 
        :list="categoryGroupedList"
        :mode="displayMode"
        :defaultExpandAll="isAllExpanded"
        @sync-expand-status="handleSyncExpand"
        @item-click="handleItemClick"
        @add-click="handleAddAsset"
      />

      <!-- 视角 B：按账户/机构 -->
      <InstitutionListView 
        ref="institutionRef"
        v-else
        :list="institutionGroupedList"
        :mode="displayMode"
        :defaultExpandAll="isAllExpanded"
        @sync-expand-status="handleSyncExpand"
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
const loading = ref(true);
const viewMode = ref('category'); // 'category' | 'institution'

const displayMode = ref('detailed'); // 'detailed' | 'simple'
const categoryRef = ref(null);
const institutionRef = ref(null);

// 切换明细/简略模式
const toggleDisplayMode = () => {
  displayMode.value = displayMode.value === 'detailed' ? 'simple' : 'detailed';
};

// 一键展开/收缩逻辑
const isAllExpanded = ref(true);

// 监听子组件手动点击回传的状态同步
const handleSyncExpand = (status) => {
  isAllExpanded.value = status;
};

// 一键展开/收缩方法
const handleToggleAll = () => {
  isAllExpanded.value = !isAllExpanded.value;
  // 手动调用子组件暴露的 toggleAll
  categoryRef.value?.toggleAll(isAllExpanded.value);
  institutionRef.value?.toggleAll(isAllExpanded.value);
};

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

  allAssets.value.forEach(asset => {
    const instCode = asset.institution || 'OTHER';
    // 1. 机构层级 (L1)
    if (!groups[instCode]) {
      const instInfo = instMap[instCode] || { instName: '其他', logoUrl: '/static/icons/default.png' };
      groups[instCode] = { 
        ...instInfo, 
        instCode, 
        total: 0, 
        accountMap: {} // 用于二级聚合
      };
    }
    groups[instCode].total += Number(asset.totalBalance);

    // 2. 账户层级 (L2) - 以账号标识符或名称作为 Key
    // 注意：如果是微信/支付宝，可以用 accountName；如果是银行，建议用 institutionIdentifier
    const accKey = asset.institutionIdentifier || asset.accountName || 'default';
    
    if (!groups[instCode].accountMap[accKey]) {
      groups[instCode].accountMap[accKey] = {
        name: accKey,
        totalBalance: 0,
        items: [] // 存放具体的资产明细 (L3)
      };
    }
    
    groups[instCode].accountMap[accKey].totalBalance += Number(asset.totalBalance);
    groups[instCode].accountMap[accKey].items.push(asset);
  });

  // 转换为数组结构供 Vue 渲染
  return Object.values(groups).map(inst => ({
    ...inst,
    accounts: Object.values(inst.accountMap)
  })).sort((a, b) => b.total - a.total);
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