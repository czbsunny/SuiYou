<template>
  <view class="report-container">
    <!-- 财务体检指标 -->
    <HealthGrid :items="healthItems" />

    <!-- 资产结构分析 -->
    <AssetStructure :isPrivacyOn="isPrivacyOn" :assetStructure="assetStructure" />

    <!-- 底部提示 -->
    <view class="footer-tip">
      <text class="tip-text">数据仅供参考，具体以实际为准</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import HealthGrid from '@/components/statistics/HealthGrid.vue';
import AssetStructure from '@/components/statistics/AssetStructure.vue';
import { createAssetSnapshots } from '@/services/assetService';
import { getMonthlySummary } from '@/services/transactionService';
import { ASSET_CATEGORY_DISPLAY, ASSET_CATEGORY } from '@/configs/assets.js';

const isPrivacyOn = ref(false);

// 数据状态
const assetData = ref(null);
const monthlySummary = ref(null);
const loading = ref(true);

// 财务指标数据
const healthItems = ref([
  { label: '结余率', status: '强', val: '0%', percent: '0%', color: '#22c55e' },
  { label: '安全垫', status: '足', val: '0', unit: '个月', percent: '0%', color: ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.LIQUID].color },
  { label: '负债率', status: '良', val: '0%', percent: '0%', color: ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.LOAN].color },
  { label: '钱生钱', status: '优', val: '0%', percent: '0%', color: ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.INVEST].color },
]);

// 资产结构数据
const assetStructure = ref({
  totalAssets: 0,
  totalLiabilities: 0,
  equityRatio: 0,
  assetDistribution: {
    liquid: 0,
    invest: 0,
    fixed: 0,
    other: 0
  }
});

// 计算财务指标
const calculateFinancialMetrics = () => {
  if (!assetData.value || !monthlySummary.value) return;
  
  const assetSnapshot = assetData.value;
  const monthlyData = monthlySummary.value;
  
  // 1. 计算结余率：过去12月的收入汇总和12月的支出汇总计算结余率
  const totalIncome12Months = monthlyData.reduce((sum, item) => sum + (item.income || 0), 0);
  const totalExpense12Months = monthlyData.reduce((sum, item) => sum + (item.expense || 0), 0);
  const savingsRate = totalIncome12Months > 0 ? ((totalIncome12Months - totalExpense12Months) / totalIncome12Months * 100).toFixed(0) : 0;
  
  // 2. 计算安全垫：12月支出的平均值用来计算安全垫，用当前流动资产总额除以12月平均支出
  const liquidAssets = assetSnapshot.liquidAssets || 0;
  const monthlyExpenseAvg = totalExpense12Months / monthlyData.length;
  const safetyMargin = monthlyExpenseAvg > 0 ? (liquidAssets / monthlyExpenseAvg).toFixed(1) : 0;
  
  // 3. 计算负债率 (总负债 / 总资产)
  const totalAssets = assetSnapshot.totalAssets || 0;
  const totalLiabilities = assetSnapshot.totalLiabilities || 0;
  const debtRatio = totalAssets > 0 ? (totalLiabilities / totalAssets * 100).toFixed(0) : 0;
  
  // 4. 计算钱生钱：投资资产除以总资产
  const investAssets = assetSnapshot.investmentAssets || 0;
  const investRatio = totalAssets > 0 ? (investAssets / totalAssets * 100).toFixed(0) : 0;
  
  // 更新健康指标
  healthItems.value = [
    { label: '结余率', status: savingsRate >= 30 ? '强' : savingsRate >= 10 ? '良' : '弱', val: `${savingsRate}%`, percent: `${Math.min(savingsRate, 100)}%`, color: '#22c55e' },
    { label: '安全垫', status: safetyMargin >= 6 ? '足' : safetyMargin >= 3 ? '中' : '缺', val: safetyMargin, unit: '个月', percent: `${Math.min(safetyMargin * 10, 100)}%`, color: ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.LIQUID].color },
    { label: '负债率', status: debtRatio <= 30 ? '优' : debtRatio <= 50 ? '良' : '高', val: `${debtRatio}%`, percent: `${Math.min(debtRatio, 100)}%`, color: ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.LOAN].color },
    { label: '钱生钱', status: investRatio >= 50 ? '优' : investRatio >= 30 ? '良' : '低', val: `${investRatio}%`, percent: `${Math.min(investRatio, 100)}%`, color: ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.INVEST].color },
  ];
  
  // 更新资产结构
  const equityRatio = totalAssets > 0 ? ((totalAssets - totalLiabilities) / totalAssets * 100).toFixed(0) : 0;
  
  // 计算资产分布
  const fixedAssets = assetSnapshot.fixedAssets || 0;
  const otherAssets = assetSnapshot.otherAssets || 0;
  
  const totalAssetValue = liquidAssets + investAssets + fixedAssets + otherAssets;
  const liquidRatio = totalAssetValue > 0 ? Math.round((liquidAssets / totalAssetValue) * 100) : 0;
  const investRatioFinal = totalAssetValue > 0 ? Math.round((investAssets / totalAssetValue) * 100) : 0;
  const fixedRatio = totalAssetValue > 0 ? Math.round((fixedAssets / totalAssetValue) * 100) : 0;
  const otherRatio = totalAssetValue > 0 ? Math.round((otherAssets / totalAssetValue) * 100) : 0;
  
  assetStructure.value = {
    totalAssets,
    totalLiabilities,
    equityRatio,
    assetDistribution: {
      liquid: liquidRatio,
      invest: investRatioFinal,
      fixed: fixedRatio,
      other: otherRatio
    }
  };
  console.log(assetStructure.value);
};

// 获取数据
const fetchData = async () => {
  loading.value = true;
  try {
    // 并行调用两个接口
    const [assetSnapshotData, monthlySummaryData] = await Promise.all([
      createAssetSnapshots(),
      getMonthlySummary()
    ]);
    console.log(assetSnapshotData);
    console.log(monthlySummaryData);

    assetData.value = assetSnapshotData;
    monthlySummary.value = monthlySummaryData;
    
    // 计算财务指标
    calculateFinancialMetrics();
  } catch (error) {
    console.error('获取数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 组件挂载时获取数据
onMounted(() => {
  fetchData();
});
</script>

<style lang="scss" scoped>
.report-container {
  min-height: 100vh;
  background-color: #FAF9F6;
  box-sizing: border-box;
  padding-top: $spacing-sm;
}

.page-subtitle {
  font-size: 14px;
  color: #9ca3af;
}

/* 底部提示 */
.footer-tip {
  text-align: center;
  padding: $spacing-sm 0;
}

.tip-text {
  font-size: 12px;
  color: #d1d5db;
}
</style>