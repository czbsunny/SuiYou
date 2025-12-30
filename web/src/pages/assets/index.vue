<template>
  <view class="page-container">
    <!-- 1. 净值趋势 (传入所有资产计算后的净值) -->
    <NetWorthCard :accounts="allAccounts" />

    <!-- 2. 财务体检 -->
    <HealthGrid :accounts="allAccounts" />

    <!-- 3. 资产概览 -->
    <view class="section-header">
      <text class="title">资产概览</text>
      <text class="subtitle">左右滑动切换分类</text>
    </view>
    
    <view v-if="loading && !allAccounts.length" class="loading-container">
      <uni-load-more status="loading"></uni-load-more>
    </view>

    <!-- 轮播组件：数据源来自配置仓库 -->
    <AssetCarousel
      :list="displayCategories"
      v-model:current="currentIndex" 
    />
    
    <!-- 4. 底部明细列表：根据当前选中的分类，过滤并展示账户 -->
    <AssetList 
      :categoryName="activeCategory?.name"
      :color="activeCategory?.color"
      :totalAmount="activeCategoryTotal"
      :list="filteredAccountList" 
      @add-click="handleAddAsset"
      @item-click="handleItemClick"
    />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import NetWorthCard from '../../components/assets/NetWorthCard.vue';
import HealthGrid from '../../components/assets/HealthGrid.vue';
import AssetCarousel from '../../components/assets/AssetCarousel.vue';
import AssetList from '../../components/assets/AssetList.vue';
import { getAccounts } from '../../services/accountService.js';
import { useConfigStore } from '@/stores/config.js'

const configStore = useConfigStore();

// === 状态变量 ===
const allAccounts = ref([]); // 存储后端返回的原始数组
const loading = ref(true);
const currentIndex = ref(0);

// 1. 获取轮播用的 5 大类配置，并动态计算每个大类的总金额
const displayCategories = computed(() => {
  return configStore.topCategories.map(cat => {
    // 过滤出属于该分类的账户
    const catAccounts = allAccounts.value.filter(acc => acc.category === cat.categoryCode);
    // 计算总额
    const total = catAccounts.reduce((sum, acc) => sum + (Number(acc.totalBalance) || 0), 0);
    return {
      ...cat,
      total: formatCurrency(total) // 在卡片上显示的格式化金额
    };
  });
});

// 2. 当前选中的分类对象
const activeCategory = computed(() => {
  return displayCategories.value[currentIndex.value];
});

// 3. 当前分类下的账户明细总额（传给 AssetList 头部）
const activeCategoryTotal = computed(() => {
  return activeCategory.value?.total || '¥ 0.00';
});

// 4. 核心：过滤并转换账户数据给 AssetList 组件
const filteredAccountList = computed(() => {
  if (!activeCategory.value) return [];

  return allAccounts.value
    .filter(acc => acc.category === activeCategory.value.categoryCode)
    .map(acc => ({
      id: acc.id,
      name: acc.name,
      platform: acc.institution || '未指定机构',
      balance: acc.totalBalance,
      color: activeCategory.value.color, // 列表Logo背景色跟随大类
      icon: getSubCategoryIcon(acc.category, acc.subCategory),
      trendText: acc.frozenBalance > 0 ? `冻结: ¥${acc.frozenBalance}` : '', 
      type: 'neutral'
    }));
});

// 辅助：金额格式化
const formatCurrency = (value) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(value);
};

const getSubCategoryIcon = (category, sub) => {
  const categoryObj = configStore.topCategories.find(cat => cat.categoryCode === category);
  const subCategories = configStore.getSubCategoriesByCode(category);
  const subCategory = subCategories[sub];
  return subCategory?.iconUrl || categoryObj?.iconUrl; 
};

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const res = await getAccounts();
    // 此时 res 就是你刚才给我的那个包含 accounts 数组的对象
    allAccounts.value = res.accounts || [];
  } catch (err) {
    console.error('加载账户失败:', err);
  } finally {
    loading.value = false;
  }
};

onShow(() => {
  loadData();
});

const handleAddAsset = () => {
  uni.navigateTo({
    url: `/pages/assets/add?category=${activeCategory.value?.categoryCode}`
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
  padding-bottom: 60px;
}

.section-header {
  padding: 0 40rpx;
  margin: 30rpx 0 10rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .title {
    font-size: 24rpx;
    font-weight: 700;
    color: #9ca3af;
    letter-spacing: 2rpx;
  }
  .subtitle {
    font-size: 20rpx;
    color: #d1d5db;
  }
}

.loading-container {
  padding: 40rpx 0;
}
</style>