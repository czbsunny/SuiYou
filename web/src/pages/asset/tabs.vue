<template>
  <view class="tab-bar">
    <!-- 左侧：固定汇总按钮 -->
    <view class="fixed-tab-wrapper">
      <view 
        @click="$emit('select', 'SUMMARY')"
        :class="['tab-item', selectedId === 'SUMMARY' ? 'is-active' : '']"
      >
        <text>汇总</text>
      </view>
    </view>

    <!-- 分割线 -->
    <view class="divider"></view>

    <!-- 中间：可滚动账户列表 -->
    <scroll-view scroll-x class="scroll-tabs" :show-scrollbar="false">
      <view class="tab-list">
        <view 
            v-for="acc in accounts"
            :key="acc.id"
            @click="$emit('select', acc.id)"
            :class="['tab-item', selectedId === acc.id ? 'is-active' : '']"
        >
            <text>{{ acc.name }}</text>
        </view>
        <!-- 右侧留白占位，防止最后一个贴边 -->
        <view class="spacer"></view>
      </view>
    </scroll-view>

    <!-- 右侧：操作按钮 -->
    <view class="actions-wrapper">
      <view class="actions-group">
          <view @click="$emit('add')" class="icon-btn">
              <img src="/static/images/plus.png" class="btn-icon icon-plus" />
          </view>
          <view @click="$emit('manage')" class="icon-btn">
              <img src="/static/images/settings.png" class="btn-icon icon-settings" />
          </view>
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps(['accounts', 'selectedId']);
defineEmits(['select', 'add', 'manage']);
</script>

<style lang="scss" scoped>
.tab-bar {
  display: flex;
  flex-direction: row;
  align-items: center;
  width: 100%;
  height: 48px; /* h-12 */
  padding: 8px 0; /* py-2 */
  position: relative;
  z-index: 10;
  user-select: none;
}

/* 通用 Tab 样式 */
.tab-item {
  white-space: nowrap;
  padding: 0 4px 4px 4px; /* px-1 pb-1 */
  font-size: 14px; /* text-sm */
  font-weight: 500;
  color: rgba($text-inverse, 0.6);
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px; /* space-x-1 */

  /* Hover 效果 (小程序 hover-class 更好，这里模拟 hover) */
  &:active {
    color: rgba($text-inverse, 0.9);
  }

  /* 选中状态 */
  &.is-active {
    color: $text-inverse;
    border-bottom-color: $border-bottom;
    font-weight: bold;
    font-size: 16px; /* text-base */
  }
}

/* 左侧固定区域 */
.fixed-tab-wrapper {
  flex-shrink: 0;
  padding-left: 12px; /* pl-3 */
  padding-right: 4px; /* pr-1 */
  position: relative;
  z-index: 20;
}

/* 分割线 */
.divider {
  flex-shrink: 0;
  margin: 0 8px; /* mx-2 */
  height: 12px;  /* h-3 */
  width: 1px;
  background-color: rgba($bg-white, 0.2);
  z-index: 20;
}

/* 滚动区域 */
.scroll-tabs {
  flex: 1;
  /* 确保 scroll-view 宽度正确计算 */
  width: 0; 
}

.tab-list {
  display: flex;
  flex-direction: row;
  align-items: center;
  height: 100%;
  padding-right: 8px; /* pr-2 */
  /* 替代 space-x-4 */
  gap: 16px; 
}

.spacer {
  width: 16px; /* w-4 */
  flex-shrink: 0;
}

/* 右侧按钮区域 */
.actions-wrapper {
  flex-shrink: 0;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding-left: 8px;  /* pl-2 */
  padding-right: 12px; /* pr-3 */
  position: relative;
  z-index: 20;
}

.actions-group {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px; /* space-x-2 */
}

.icon-btn {
  width: 32px;  /* w-8 */
  height: 32px; /* h-8 */
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: rgba($bg-white, 0.1);
  color: $text-inverse;
  transition: background-color 0.3s;
  backdrop-filter: blur(4px); /* backdrop-blur-sm */

  &:active {
    background-color: rgba($bg-white, 0.2);
  }
}

.btn-icon {
  display: block;
}

.icon-plus {
  width: 18px;
  height: 18px;
}

.icon-settings {
  width: 16px;
  height: 16px;
}
</style>