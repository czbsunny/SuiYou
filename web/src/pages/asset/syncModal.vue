<template>
  <view v-if="isOpen" class="sync-modal-overlay">
    <!-- 背景遮罩 -->
    <view class="modal-backdrop" @click="$emit('close')"></view>
    
    <!-- 模态框主体 -->
    <view class="modal-card">
      <!-- 顶部头部 -->
      <view class="modal-header">
          <view class="icon-wrapper">
              <image src="/static/images/chart-pie-blue.png" class="header-icon" />
          </view>
          <text class="modal-title">同步至投资组合</text>
          <text class="modal-desc">
              是否将【{{ accountName }}】的持仓数据同步至投资组合？
          </text>
      </view>

      <!-- 功能列表 -->
      <view class="feature-box">
          <text class="feature-label">同步后即可开启：</text>
          
          <view class="feature-list">
              <view class="feature-item">
                  <image src="/static/images/scan-line.png" class="feature-icon" />
                  <text>持仓穿透与行业透视</text>
              </view>
              <view class="feature-item">
                  <image src="/static/images/shield-check-blue.png" class="feature-icon" />
                  <text>组合风险与收益归因分析</text>
              </view>
              <view class="feature-item">
                  <image src="/static/images/target.png" class="feature-icon" />
                  <text>智能调仓与再平衡建议</text>
              </view>
          </view>
      </view>

      <!-- 底部按钮 -->
      <view class="action-grid">
          <view 
              @click="$emit('close')"
              class="btn btn-cancel"
          >
              <text>暂不需要</text>
          </view>
          <view 
              @click="$emit('confirm')"
              class="btn btn-confirm"
          >
              <text>立即同步</text>
          </view>
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps(['isOpen', 'accountName']);
defineEmits(['close', 'confirm']);
</script>

<style lang="scss" scoped>

.sync-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 80;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px; /* px-4 */
}

/* 黑色半透明背景 */
.modal-backdrop {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.4);
  transition: opacity 0.3s;
}

/* 卡片主体 */
.modal-card {
  background-color: $bg-white;
  width: 100%;
  max-width: 384px; /* max-w-sm */
  border-radius: 16px; /* rounded-2xl */
  padding: 24px; /* p-6 */
  position: relative;
  z-index: 10;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25); /* shadow-2xl */
  display: flex;
  flex-direction: column;
}

/* 头部区域 */
.modal-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 24px; /* mb-6 */
}

.icon-wrapper {
  width: 64px; /* w-16 */
  height: 64px;
  background-color: $primary-alpha-10;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px; /* mb-4 */
}

.header-icon {
  width: 32px;
  height: 32px;
}

.modal-title {
  font-size: 20px; /* text-xl */
  font-weight: bold;
  color: $text-main;
  margin-bottom: 8px; /* mb-2 */
  display: block;
}

.modal-desc {
  color: $text-regular;
  font-size: 14px; /* text-sm */
  line-height: 1.625; /* leading-relaxed */
  display: block;
  padding: 0 8px; /* px-2 */
}

/* 功能列表盒子 */
.feature-box {
  background-color: $bg-cream;
  border-radius: 12px; /* rounded-xl */
  padding: 16px; /* p-4 */
  margin-bottom: 24px; /* mb-6 */
  border: 1px solid $border-color;
  width: 100%;
  box-sizing: border-box; /* 确保 padding 不撑大宽度 */
}

.feature-label {
  font-size: 12px; /* text-xs */
  color: $text-muted;
  margin-bottom: 4px; /* mb-1 */
  display: block;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 12px; /* space-y-3 */
}

.feature-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  font-size: 14px; /* text-sm */
  color: $text-sub;
}

.feature-icon {
  width: 16px; /* :size="16" */
  height: 16px;
  margin-right: 12px; /* mr-3 */
}

/* 底部按钮组 */
.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px; /* gap-3 */
  width: 100%;
}

.btn {
  padding: 12px 0; /* py-3 */
  border-radius: 12px; /* rounded-xl */
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: background-color 0.2s;
}

.btn-cancel {
  color: $text-regular;
  background-color: $bg-subtle;
  
  &:active {
    background-color: $bg-subtle-hover;
  }
}

.btn-confirm {
  color: #ffffff;
  background-color: $primary;
  box-shadow: 0 10px 15px -3px rgba($primary, 0.2);
  
  &:active {
    opacity: 0.9;
  }
}
</style>