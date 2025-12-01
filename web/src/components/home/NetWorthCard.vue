<template>
  <view class="hero-section">
    <view class="net-worth-card">
      <view class="card-bg-decoration"></view>
      <view class="card-content">
        <view class="card-label">家庭净资产 (Net Worth)</view>
        <view class="main-number">{{ displayAmount }}</view>
        
        <view class="trend-row">
          <view class="trend-pill">
            <uni-icons type="arrow-up" size="12" color="#4ADE80"></uni-icons>
            <span>{{ displayTrend }}</span>
          </view>
          <span class="trend-desc">较上月增长 0.45%</span>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  isPrivacyOn: Boolean
});

// 使用 computed 自动处理隐私脱敏
const displayAmount = computed(() => props.isPrivacyOn ? '****' : '¥ 2,850,200');
const displayTrend = computed(() => props.isPrivacyOn ? '****' : '+¥12,400 (当月)');
</script>

<style scoped>
.hero-section { padding: 0 20px 20px 20px; }
.net-worth-card {
  background: linear-gradient(135deg, #1F2937 0%, #000000 100%);
  border-radius: 24px; padding: 24px; color: white; position: relative; overflow: hidden;
  box-shadow: 0 15px 30px -10px rgba(0,0,0,0.4);
}
.card-bg-decoration {
  position: absolute; top: -50%; right: -20%; width: 300px; height: 300px;
  background: radial-gradient(circle, rgba(42,128,108,0.5) 0%, transparent 70%);
  filter: blur(40px); pointer-events: none;
}
.card-content { position: relative; z-index: 2; }
.card-label { font-size: 12px; opacity: 0.6; text-transform: uppercase; letter-spacing: 0.5px; }
.main-number { font-size: 36px; font-weight: 700; margin: 8px 0 16px 0; font-family: monospace; letter-spacing: -1px; }
.trend-row { display: flex; align-items: center; gap: 10px; }
.trend-pill {
  background: rgba(42,128,108,0.3); padding: 4px 10px; border-radius: 20px;
  font-size: 12px; color: #4ADE80; font-weight: 600; display: flex; align-items: center; gap: 4px;
}
.trend-desc { font-size: 11px; opacity: 0.5; }
</style>