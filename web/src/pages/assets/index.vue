<template>
  <view class="page-container">
    <!-- 1. 净值趋势 -->
    <NetWorthCard />

    <!-- 2. 财务体检 -->
    <HealthGrid />

    <!-- 3. 资产概览 (3D 轮播) -->
    <view class="section-header">
      <text class="title">资产概览</text>
      <text class="subtitle">滑动切换</text>
    </view>
    
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text class="loading-text">加载中...</text>
    </view>
    
    <!-- 空状态 -->
    <view v-else-if="assets.length === 0" class="empty-container">
      <text class="empty-text">暂无资产数据</text>
      <button class="add-btn" @click="handleAddAsset">添加资产</button>
    </view>
    
    <!-- 轮播组件：双向绑定当前索引 -->
    <AssetCarousel 
      v-else
      :list="assets" 
      v-model:current="currentIndex" 
    />

    <!-- 4. 底部明细列表 -->
    <AssetList 
      v-if="!loading && assets.length > 0"
      :data="currentAssetData" 
      @add-asset="handleAddAsset"
    />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import NetWorthCard from '../../components/assets/NetWorthCard.vue';
import HealthGrid from '../../components/assets/HealthGrid.vue';
import AssetCarousel from '../../components/assets/AssetCarousel.vue';
import AssetList from '../../components/assets/AssetList.vue';
import { getAccounts } from '../../services/accountService.js';

// === 数据源 ===
const assets = ref([]);
const loading = ref(true);
const error = ref(null);

const currentIndex = ref(0);

// 计算当前选中的资产数据
const currentAssetData = computed(() => {
  return assets.value[currentIndex.value] || null;
});

// 格式化金额显示
const formatCurrency = (value) => {
  if (value === null || value === undefined) return '¥ 0';
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(value);
};

// 转换账户数据为页面所需格式
const transformAccountData = (accounts) => {
  if (!accounts || !accounts.accounts || accounts.accounts.length === 0) {
    return [];
  }

  // 按账户组类型分组
  const groupMap = new Map();
  const groupConfig = {
    'ASSET': { id: 'ASSET', name: '资产', color1: '#4facfe', color2: '#00f2fe', icon: 'fa-wallet' },
    'LIABILITY': { id: 'LIABILITY', name: '负债', color1: '#ff512f', color2: '#dd2476', icon: 'fa-file-invoice-dollar' }
  };

  accounts.accounts.forEach(account => {
    const groupType = account.groupType || 'ASSET';
    const category = account.category || 'OTHER';
    const groupKey = groupType;
    
    if (!groupMap.has(groupKey)) {
      const config = groupConfig[groupType] || groupConfig['ASSET'];
      groupMap.set(groupKey, {
        id: groupKey,
        name: config.name,
        total: 0,
        color1: config.color1,
        color2: config.color2,
        icon: config.icon,
        list: []
      });
    }

    const group = groupMap.get(groupKey);
    group.total += account.balance || 0;
    
    // 获取机构名称
    let institutionName = account.institution || '';
    let subInfo = '';
    
    if (category === 'LIQUID') {
      subInfo = account.subCategory === 'BANK_CARD' ? '银行卡' : '电子账户';
    } else if (category === 'INVEST') {
      subInfo = '投资账户';
    } else if (category === 'FIXED') {
      subInfo = '固定资产';
    } else {
      subInfo = category;
    }
    
    group.list.push({
      id: account.id,
      n: account.name,
      s: subInfo + (institutionName ? ` · ${institutionName}` : ''),
      a: account.balance,
      i: getCategoryIcon(category)
    });
  });

  // 转换总金额格式
  return Array.from(groupMap.values()).map(group => ({
    ...group,
    total: formatCurrency(group.total)
  }));
};

// 获取分类图标
const getCategoryIcon = (category) => {
  const iconMap = {
    'LIQUID': 'fa-wallet',
    'INVEST': 'fa-chart-line',
    'FIXED': 'fa-house',
    'OTHER': 'fa-cubes',
    'LOAN': 'fa-file-invoice-dollar'
  };
  return iconMap[category] || 'fa-wallet';
};

// 加载资产数据
const loadAssets = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    const result = await getAccounts();
    console.log('获取资产数据成功:', result);
    assets.value = transformAccountData(result);
  } catch (err) {
    console.error('获取资产数据失败:', err);
    error.value = err.message || '加载资产数据失败';
    // 如果加载失败，使用空数组
    assets.value = [];
  } finally {
    loading.value = false;
  }
};

// 页面显示时刷新数据
onShow(() => {
  loadAssets();
});

// 添加资产按钮点击事件处理函数
const handleAddAsset = () => {
  const currentAsset = assets.value[currentIndex.value];
  const assetType = currentAsset?.id || 'ASSET';
  
  console.log(`添加${currentAsset?.name || '资产'}类型的资产`);
  
  // 跳转到添加资产页面并传递资产类型
  uni.navigateTo({
    url: `/pages/assets/add?type=${assetType}`
  });
};
</script>

<style lang="scss">

/* 全局变量与基础样式 */
page {
  background-color: $bg-page;
}
.page-container {
  padding-bottom: 100px;
  min-height: 100vh;
  background-color: $bg-page;
  font-family: -apple-system, BlinkMacSystemFont, Helvetica, sans-serif;
}

.section-header {
  padding: 0 24px;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .title {
    font-size: 12px;
    font-weight: 700;
    color: #9ca3af;
    text-transform: uppercase;
    letter-spacing: 1px;
  }
  .subtitle {
    font-size: 10px;
    color: #d1d5db;
  }
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
  
  .loading-text {
    color: #9ca3af;
    font-size: 14px;
  }
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  
  .empty-text {
    color: #9ca3af;
    font-size: 14px;
    margin-bottom: 20px;
  }
  
  .add-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 20px;
    padding: 10px 30px;
    font-size: 14px;
  }
}


</style>