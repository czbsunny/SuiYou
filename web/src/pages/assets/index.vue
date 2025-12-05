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
    
    <!-- 轮播组件：双向绑定当前索引 -->
    <AssetCarousel 
      :list="assets" 
      v-model:current="currentIndex" 
    />

    <!-- 4. 底部明细列表 -->
    <AssetList 
      :data="currentAssetData" 
      @add-asset="handleAddAsset"
    />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import NetWorthCard from '../../components/assets/NetWorthCard.vue';
import HealthGrid from '../../components/assets/HealthGrid.vue';
import AssetCarousel from '../../components/assets/AssetCarousel.vue';
import AssetList from '../../components/assets/AssetList.vue';  

// === 数据源 ===
const assets = ref([
  { 
    id: 'LIQUID', 
    name: "流动资产", 
    total: "¥ 50,000", 
    color1: "#4facfe", color2: "#00f2fe",
    icon: "fa-wallet",
    list: [
      { n: "招商银行", s: "工资卡 · 已分配80%", a: "30,000", i: "img" },
      { n: "支付宝", s: "余额 · 闲置", a: "20,000", i: "fa-brands fa-alipay text-blue-500" }
    ]
  },
  { 
    id: 'INVEST', 
    name: "投资理财", 
    total: "¥ 1,200,000", 
    color1: "#1e5c4e", color2: "#2a806c",
    icon: "fa-chart-line",
    list: [
      { n: "长赢养老组合", s: "且慢 · +12.5%", a: "800,000", i: "fa-solid fa-seedling text-green-600" },
      { n: "美股账户", s: "富途 · +5.2%", a: "400,000", i: "fa-solid fa-dollar-sign text-green-600" }
    ]
  },
  { 
    id: 'FIXED', 
    name: "固定资产", 
    total: "¥ 3,000,000", 
    color1: "#e3c46e", color2: "#daa520",
    icon: "fa-house",
    list: [
      { n: "自住房产", s: "杭州 · 估值", a: "2,800,000", i: "fa-solid fa-house text-yellow-600" },
      { n: "住房公积金", s: "国管", a: "200,000", i: "fa-solid fa-building-columns text-gray-500" }
    ]
  },
  { 
    id: 'OTHER_ASSET', 
    name: "其他资产", 
    total: "¥ 150,000", 
    color1: "#833ab4", color2: "#fd1d1d",
    icon: "fa-cubes",
    list: [
      { n: "黄金投资", s: "实物黄金 · 首饰", a: "100,000", i: "fa-solid fa-coins text-yellow-500" },
      { n: "收藏品", s: "邮票 · 1980年猴票", a: "50,000", i: "fa-solid fa-stamp text-blue-500" }
    ]
  },
  { 
    id: 'LOAN', 
    name: "负债贷款", 
    total: "¥ -1,200,000", 
    color1: "#ff512f", color2: "#dd2476",
    icon: "fa-file-invoice-dollar",
    list: [
      { n: "住房贷款", s: "工行 · 剩余208期", a: "-1,200,000", i: "fa-solid fa-file-contract text-red-500" }
    ]
  }
]);

const currentIndex = ref(0);

// 计算当前选中的资产数据
const currentAssetData = computed(() => {
  return assets.value[currentIndex.value];
});

// 添加资产按钮点击事件处理函数
const handleAddAsset = () => {
  const currentAsset = assets.value[currentIndex.value];
  console.log(`添加${currentAsset.name}类型的资产`);
    
  const assetType = currentAsset.id;
  
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


</style>