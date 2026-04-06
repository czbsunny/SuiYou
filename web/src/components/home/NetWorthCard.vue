<template>
  <view class="hero-section">
    <view class="net-worth-card">
      <view class="card-content">
        <view class="card-header">
          <view class="card-label">家庭净资产</view>
          <!-- 修正：小程序建议使用 view 而不是 div，image 而不是 img -->
          <view class="privacy-toggle" @click="togglePrivacy">
            <image 
              :src="isPrivacyOn ? '/static/images/eye-close.png' : '/static/images/eye.png'" 
              style="width: 24px; height: 24px;" 
            />
          </view>
        </view>
        
        <view class="main-number">{{ displayAmount }}</view>
        
        <view class="trend-row">
          <view class="trend-pill">
            <text>{{ displayTrend }}</text>
          </view>
          <text class="trend-desc">{{ trendDesc }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue';
import { getNetWorth } from '@/services/assetService';

const emit = defineEmits(['update:isPrivacyOn']);
const props = defineProps({
  isPrivacyOn: Boolean
});

// 数据状态
const netWorthData = ref({
  currentNetWorth: '0',
  lastMonthNetWorth: '0',
  growthRatio: 0,
  growthRatioFormatted: '0.00%'
});

const loading = ref(true);
const error = ref(null);

/**
 * 核心：封装刷新方法
 * 返回 Promise 方便父组件 await
 */
const fetchNetWorthData = async () => {
  try {
    loading.value = true;
    const data = await getNetWorth();
    netWorthData.value = data;
    error.value = null;
  } catch (err) {
    console.error('加载净资产数据失败:', err);
    error.value = '加载失败';
  } finally {
    loading.value = false;
  }
};

// 初始化加载
onMounted(() => {
  fetchNetWorthData();
});

/**
 * 关键点：暴露给父组件使用
 */
defineExpose({
  fetchNetWorth: fetchNetWorthData
});

const formatAmount = (amount) => {
  if (!amount || isNaN(amount)) return '¥ 0';
  const num = Number(amount);
  return `¥ ${num.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}`;
};

const displayAmount = computed(() => {
  if (loading.value) return '...';
  return props.isPrivacyOn ? '****' : formatAmount(netWorthData.value.currentNetWorth);
});

const growthAmount = computed(() => {
  const current = Number(netWorthData.value.currentNetWorth || 0);
  const lastMonth = Number(netWorthData.value.lastMonthNetWorth || 0);
  return current - lastMonth;
});

const growthRatio = computed(() => {
  const current = Number(netWorthData.value.currentNetWorth || 0);
  const lastMonth = Number(netWorthData.value.lastMonthNetWorth || 0);
  if (lastMonth === 0) return current > 0 ? 100 : 0;
  return ((current - lastMonth) / lastMonth) * 100;
});

const displayTrend = computed(() => {
  if (loading.value) return '...';
  if (props.isPrivacyOn) return '****';
  const trend = growthAmount.value >= 0 ? '+' : '';
  return `${trend}¥${Math.abs(growthAmount.value).toLocaleString('zh-CN')} (当月)`;
});

const trendDesc = computed(() => {
  if (loading.value) return '...';
  if (props.isPrivacyOn) return '****';
  const lastMonthNetWorth = Number(netWorthData.value.lastMonthNetWorth || 0);
  if (lastMonthNetWorth === 0) return '较上月增长 ∞';
  const trendType = growthRatio.value >= 0 ? '增长' : '下滑';
  return `较上月${trendType} ${Math.abs(growthRatio.value).toFixed(2)}%`;
});

const togglePrivacy = () => {
  emit('update:isPrivacyOn', !props.isPrivacyOn);
};
</script>

<style lang="scss" scoped>
.hero-section { 
  padding: $spacing-sm $spacing-base $spacing-base; 
}
.net-worth-card {
  background: #ffffff;
  border-radius: $radius-lg; padding: $spacing-base; color: #2C3E50; position: relative; overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
}
.card-content { position: relative; z-index: 2; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}
.card-label { font-size: 16px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.5px; }
.privacy-toggle {
  cursor: pointer;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: $gray-200;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.1s;
}
.privacy-toggle:hover {
  background: #e5e7eb;
}
.main-number { font-size: 36px; font-weight: 700; margin: 8px 0 16px 0; font-family: monospace; letter-spacing: -1px; color: #2C3E50; }
.trend-row { display: flex; align-items: center; gap: 10px; }
.trend-pill {
  background: #f3f4f6; padding: 4px 10px; border-radius: 20px;
  font-size: 12px; color: #6b7280; font-weight: 600; display: flex; align-items: center; gap: 4px;
}
.trend-desc { font-size: 11px; color: #9ca3af; }
</style>