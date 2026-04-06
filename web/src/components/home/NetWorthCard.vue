<template>
  <view class="hero-section">
    <view class="net-worth-card">
      <view class="card-content">
        <view class="card-header">
          <view class="card-label">家庭净资产</view>
          <div class="privacy-toggle" @click="togglePrivacy">
            <img :src="isPrivacyOn ? '/static/images/eye-close.png' : '/static/images/eye.png'" alt="隐私切换" style="width: 24px; height: 24px;" />
          </div>
        </view>
        <view class="main-number">{{ displayAmount }}</view>
        
        <view class="trend-row">
          <view class="trend-pill">
            <uni-icons :type="growthAmount > 0 ? 'arrow-up' : growthAmount < 0 ? 'arrow-down' : 'arrow-right'" size="12" :color="growthAmount > 0 ? '#4ADE80' : growthAmount < 0 ? '#EF4444' : '#9CA3AF'"></uni-icons>
            <span>{{ displayTrend }}</span>
          </view>
          <span class="trend-desc">较上月增长 {{ trendDesc }}</span>
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
  currentNetWorth: '0.00',
  lastMonthNetWorth: '0.00',
  growthRatio: 0,
  growthRatioFormatted: '0.00%'
});

const loading = ref(true);
const error = ref(null);

// 加载数据
onMounted(async () => {
  try {
    loading.value = true;
    const data = await getNetWorth();
    netWorthData.value = data;
  } catch (err) {
    console.error('加载净资产数据失败:', err);
    error.value = '加载数据失败';
  } finally {
    loading.value = false;
  }
});

// 格式化金额显示
const formatAmount = (amount) => {
  if (!amount) return '¥ 0';
  
  // 转换为数字
  const num = Number(amount);
  
  // 格式化数字为货币格式
  return `¥ ${num.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}`;
};

// 格式化百分比显示
const formatPercentage = (value) => {
  return `${value.toFixed(2)}%`;
};

// 使用 computed 自动处理隐私脱敏
const displayAmount = computed(() => {
  if (loading.value) return '加载中...';
  return props.isPrivacyOn ? '****' : formatAmount(netWorthData.value.currentNetWorth);
});

// 计算增长金额
const growthAmount = computed(() => {
  if (loading.value) return 0;
  const current = Number(netWorthData.value.currentNetWorth);
  const lastMonth = Number(netWorthData.value.lastMonthNetWorth);
  return current - lastMonth;
});

// 计算增长比例
const growthRatio = computed(() => {
  if (loading.value) return 0;
  const current = Number(netWorthData.value.currentNetWorth);
  const lastMonth = Number(netWorthData.value.lastMonthNetWorth);
  
  if (lastMonth === 0) {
    // 上月净资产为0，增长率为100%
    return current > 0 ? 100 : 0;
  }
  
  return ((current - lastMonth) / lastMonth) * 100;
});

// 计算增长趋势显示
const displayTrend = computed(() => {
  if (loading.value) return '加载中...';
  if (props.isPrivacyOn) return '****';
  
  const trend = growthAmount.value > 0 ? '+' : '';
  return `${trend}¥${growthAmount.value.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })} (当月)`;
});

// 计算趋势描述
const trendDesc = computed(() => {
  if (loading.value) return '加载中...';
  if (props.isPrivacyOn) return '****';
  
  // 检查上月净资产是否为0
  const lastMonthNetWorth = Number(netWorthData.value.lastMonthNetWorth);
  if (lastMonthNetWorth === 0) {
    // 上月资产为空，显示无穷大标识
    return '较上月增长 ∞';
  }
  
  // 根据增长情况显示增长或下滑
  const trendType = growthRatio.value >= 0 ? '增长' : '下滑';
  return `较上月${trendType} ${formatPercentage(Math.abs(growthRatio.value))}`;
});

// 切换隐私模式
const togglePrivacy = () => {
  const newState = !props.isPrivacyOn;
  emit('update:isPrivacyOn', newState);
};
</script>

<style lang="scss" scoped>
.hero-section { padding: $spacing-base; }
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