<template>
  <view class="hero-section">
    <view class="net-worth-card">
      <view class="card-content">
        <view class="card-header">
          <view class="card-label">家庭净资产</view>
          <div class="privacy-toggle" @click="togglePrivacy">
            <img :src="isPrivacyOn ? '/static/images/eye-closed.png' : '/static/images/eye.png'" alt="隐私切换" style="width: 24px; height: 24px;" />
          </div>
        </view>
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
import { computed, ref } from 'vue';

const emit = defineEmits(['update:isPrivacyOn']);

const props = defineProps({
  isPrivacyOn: Boolean
});

// 使用 computed 自动处理隐私脱敏
const displayAmount = computed(() => props.isPrivacyOn ? '****' : '¥ 2,850,200');
const displayTrend = computed(() => props.isPrivacyOn ? '****' : '+¥12,400 (当月)');

// 切换隐私模式
const togglePrivacy = () => {
  const newState = !props.isPrivacyOn;
  emit('update:isPrivacyOn', newState);
};
</script>

<style scoped>
.hero-section { padding: 20px; }
.net-worth-card {
  background: #ffffff;
  border-radius: 24px; padding: 24px; color: #2C3E50; position: relative; overflow: hidden;
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
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
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