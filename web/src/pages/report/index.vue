<template>
  <view class="report-container">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">家庭财务体检报告</text>
      <text class="page-subtitle">更新于今日</text>
    </view>

    <!-- 财务体检指标 -->
    <HealthGrid />

    <!-- 资产结构分析 -->
    <view class="structure-section">
      <view class="section-header">
        <text class="section-title">资产结构分析</text>
      </view>
      <AssetStructure :isPrivacyOn="isPrivacyOn" />
    </view>

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
  padding: 20px;
  box-sizing: border-box;
}

/* 页面标题 */
.page-header {
  padding: 20px 0;
  text-align: center;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #2C3E50;
  display: block;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 14px;
  color: #9ca3af;
}

/* 通用区块样式 */
.structure-section {
  margin-bottom: 24px;
}

.section-header {
  padding: 0 8px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #2C3E50;
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