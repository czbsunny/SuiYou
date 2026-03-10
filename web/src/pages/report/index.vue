<template>
  <view class="report-container">
    <!-- 财务体检指标 -->
    <HealthGrid />

    <!-- 资产结构分析 -->
    <AssetStructure :isPrivacyOn="isPrivacyOn" />

    <!-- 底部提示 -->
    <view class="footer-tip">
      <text class="tip-text">数据仅供参考，具体以实际为准</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import HealthGrid from '@/components/statistics/HealthGrid.vue';
import AssetStructure from '@/components/statistics/AssetStructure.vue';

const isPrivacyOn = ref(false);
</script>

<style lang="scss" scoped>
.report-container {
  min-height: 100vh;
  background-color: #FAF9F6;
  box-sizing: border-box;
  padding-top: 20px;
}

.page-subtitle {
  font-size: 14px;
  color: #9ca3af;
}

/* 底部提示 */
.footer-tip {
  text-align: center;
  padding: 20px 0;
}

.tip-text {
  font-size: 12px;
  color: #d1d5db;
}
</style>