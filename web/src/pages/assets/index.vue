<template>
  <view class="page-container">
    <!-- 1. 顶部净值总览卡片 (由 assets 决定) -->
    <NetWorthCard :assets="allAssets" />

    <!-- 2. 财务体检 -->
    <HealthGrid :assets="allAssets" />

    <!-- 3. 视角切换开关 -->
    <AssetViewToggle v-model="viewMode" />

    <!-- 加载状态 -->
    <view v-if="loading && !allAssets.length && !allAccounts.length" class="loading-container">
      <uni-load-more status="loading" />
    </view>

    <!-- 4. 核心内容区 -->
    <view class="content-view">
      <!-- 视角 A：按资产类别 (数据源: allAssets) -->
      <CategoryListView 
        v-if="viewMode === 'category'"
        :list="categoryGroupedList"
        @item-click="handleItemClick"
        @add-click="handleAddAsset"
      />

      <!-- 视角 B：按机构卡包 (数据源: allAccounts) -->
      <InstitutionListView 
        v-else
        :list="accountFlatList"
        @manage-click="handleManageAccount"
        @account-click="handleAccountClick"
        @add-account-click="handleAddAccount"
        @add-asset-click="handleAddAssetWithAccount"
      />
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue';
import { onShow, onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { getAssets } from '../../services/assetService.js';
import { getAccounts } from '../../services/accountService.js'; // 🟢 导入账户接口
import { ASSET_INSTITUTION_DISPLAY } from '@/configs/assets.js';

// 引入组件
import NetWorthCard from '../../components/assets/NetWorthCard.vue';
import HealthGrid from '../../components/assets/HealthGrid.vue';
import AssetViewToggle from '@/components/assets/AssetViewToggle.vue';
import CategoryListView from '@/components/assets/CategoryListView.vue';
import InstitutionListView from '@/components/assets/InstitutionListView.vue';

const configStore = useConfigStore();

// === 核心状态 ===
const allAssets = ref([]);   // 逻辑资产项
const allAccounts = ref([]); // 物理账户卡片
const loading = ref(true);
const viewMode = ref('category'); 

// 类别视图聚合 (基于 allAssets) ---
const categoryGroupedList = computed(() => {
  return configStore.topCategories.map(cat => {
    const items = allAssets.value
      .filter(asset => asset.category === cat.categoryCode)
      .map(asset => ({
        ...asset,
        instInfo: configStore.getInstitutionByCode(asset.institution)
      }));
    
    const total = items.reduce((sum, i) => sum + (Number(i.totalBalance) || 0), 0);
    return { ...cat, totalBalance: total, items: items };
  });
});

// 机构视图聚合 (基于 allAccounts) ---
// --- 数据聚合：视角 B (按账户卡片扁平列表) ---
const accountFlatList = computed(() => {
  const instMap = configStore.institutionMap;

  return allAccounts.value.map(acc => {
    // 1. 匹配机构基础信息 (获取 Logo 和 业态类型)
    const instConfig = instMap[acc.institution] || {};
    const instType = instConfig.instType || 'OTHER';
    
    // 2. 计算该账户下挂载的资产项数量 (从全量 assets 中过滤)
    const subItems = allAssets.value.filter(asset => asset.accountId === acc.id);

    return {
      id: acc.id,
      instName: instConfig.instName || '未知机构', // 机构名：如 中国建设银行
      instCode: acc.institution,                 // 机构代码：CCB
      accountName: acc.accountName,              // 账户别名：如 消费卡
      identifier: acc.institutionIdentifier,     // 标识码：如 3212
      logoUrl: instConfig.logoUrl || '/static/icons/default-bank.png',
      
      bgColor: acc.themeColor || ASSET_INSTITUTION_DISPLAY[instType]?.color || '#4b5563',
      
      // 后端目前返回的账户结构中若不带总额，前端可从子项累加，或者直接取 acc.totalBalance
      totalBalance: acc.totalBalance || subItems.reduce((sum, i) => sum + (Number(i.totalBalance) || 0), 0), 
      itemCount: subItems.length,
      
      // 权限与描述处理
      subText: acc.visibleScope === 'PRIVATE' ? '私有账户' : '家庭共享',
      yesterdayProfit: acc.yesterdayProfit || 0
    };
  }).sort((a, b) => b.totalBalance - a.totalBalance); // 按金额从高到低排序
});

// 加载资产项
const fetchAssets = async () => {
  try {
    const res = await getAssets();
    allAssets.value = res.assets || [];
  } catch (err) {
    console.error('加载资产项失败:', err);
  }
};

// 加载物理账户
const fetchAccounts = async () => {
  try {
    const res = await getAccounts();
    // 根据你之前提供的后端结构: { count: 2, accounts: [...] }
    allAccounts.value = res.accounts || [];
  } catch (err) {
    console.error('加载账户失败:', err);
  }
};

// 初始化与全量刷新
const loadData = async () => {
  loading.value = true;
  // 并行请求，提高效率
  await Promise.all([fetchAssets(), fetchAccounts()]);
  loading.value = false;
};

// 生命周期管理
onLoad(() => {
  // 🟢 注册全局事件：当添加账户成功后，可以通过 uni.$emit('refreshAccountList') 触发
  uni.$on('refreshAccountList', () => {
    fetchAccounts();
    fetchAssets(); // 通常账户变动也会影响资产关系
  });
});

onUnmounted(() => {
  uni.$off('refreshAccountList');
});

onShow(() => {
  loadData();
});

// --- 路由跳转 ---
const handleAddAsset = (catCode = '') => {
  uni.navigateTo({ url: `/pages/assets/add?category=${catCode}` });
};

// 在特定账户下添加资产
const handleAddAssetWithAccount = (account) => {
  uni.navigateTo({ 
    url: `/pages/assets/add?accountId=${account.id}&instCode=${account.instCode}` 
  });
};

const handleAddAccount = () => {
  uni.navigateTo({ url: `/pages/assets/add-account` });
};

const handleItemClick = (item) => {
  uni.navigateTo({ url: `/pages/assets/item-detail?id=${item.id}` });
};

const handleAccountClick = (account) => {
  uni.navigateTo({ url: `/pages/assets/account-detail?id=${account.id}` });
};

const handleManageAccount = () => {
  uni.navigateTo({ url: `/pages/assets/manage-account` });
};

</script>

<style lang="scss">
page { background-color: #F9F8F4; }
.page-container {
  min-height: 100vh; background-color: #F9F8F4;
  padding-bottom: env(safe-area-inset-bottom);
}
.loading-container { padding: 100rpx 0; }
.content-view { padding: 0 32rpx 40rpx; }
</style>