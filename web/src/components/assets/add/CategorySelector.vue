<template>
  <view class="section-card">
    <view class="card-title">资产归类</view>
    <view class="card-body">
      <!-- 大类选择 -->
      <scroll-view scroll-x class="type-scroll" :show-scrollbar="false">
        <view class="type-scroll-inner">
          <view 
            v-for="type in categories" :key="type.code"
            class="type-item" :class="{ active: modelValue === type.code }"
            @click="$emit('update:modelValue', type.code)"
          >
            <view class="icon-circle" :class="{ active: modelValue === type.code }">
              <image :src="modelValue === type.code ? type.iconGray : type.icon" class="type-icon" mode="aspectFit" />
            </view>
            <text class="type-text">{{ type.name }}</text>
          </view>
        </view>
      </scroll-view>

      <view class="divider" v-if="subcategories.length > 0"></view>

      <!-- 子类标签 -->
      <view class="tags-container" v-if="subcategories.length > 0">
        <view class="tags-label">选择类型</view>
        <view class="tags-wrapper">
          <view 
            v-for="sub in subcategories" :key="sub.categoryCode"
            class="tag-item" :class="{ active: subValue === sub.categoryCode }"
            @click="$emit('update:subValue', sub.categoryCode)"
          >
            {{ sub.name }}
          </view>
        </view>
      </view>
      
      <slot name="footer"></slot>
    </view>
  </view>
</template>

<script setup>
defineProps(['categories', 'modelValue', 'subcategories', 'subValue']);
defineEmits(['update:modelValue', 'update:subValue']);
</script>

<style lang="scss" scoped>
$primary-light: rgba(42, 128, 108, 0.08);

$border-light: rgba(0, 0, 0, 0.04);
$tag-inactive: #F5F7FA;  // 未选中标签背景

.section-card {
  background-color: $bg-white;
  border-radius: 16px;
  padding: 20px 16px;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03); // 轻柔阴影
  
  .card-title {
    font-size: 14px;
    font-weight: 700;
    color: $text-main;
    margin-bottom: 16px;
    padding-left: 10px;
    border-left: 3px solid $primary; // 金色装饰条
    line-height: 1;
  }
}

.type-scroll {
  width: 100%;
  white-space: nowrap;
  margin-bottom: 4px;
}

.type-scroll-inner {
  display: flex;
  padding-bottom: 8px; 
}

.type-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  margin-right: 20px;
  position: relative;
  
  .icon-circle {
    width: 48px;
    height: 48px;
    border-radius: 16px;
    background-color: $tag-inactive; // 默认浅色背景
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 8px;
    transition: all 0.3s;
    
    .type-icon {
      width: 28px;
      height: 28px;
      opacity: 0.6;
    }
    
    /* 选中状态的图标容器 */
    &.active {
      background-color: $primary; // 选中后使用主题色
      
      .type-icon {
        opacity: 1;
      }
    }
  }
  
  .type-text {
    font-size: 12px;
    color: $text-sub;
    font-weight: 500;
  }

  /* 选中状态 */
  &.active {
    .type-text {
      color: $primary;
      font-weight: 700;
    }
  }
}

.divider {
  height: 1px;
  background-color: $border-light;
  margin: 12px 0 16px 0;
}

.inner-divider {
  height: 1px;
  background-color: $border-light;
  margin: 16px 0;
  border-top: 1px dashed rgba(0,0,0,0.05);
  background-color: transparent;
}

.tags-container {
  .tags-label {
    font-size: 12px;
    color: $text-sub;
    margin-bottom: 10px;
  }
}

.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  padding: 6px 16px;
  border-radius: 8px;
  background-color: $tag-inactive;
  color: $text-sub;
  font-size: 13px;
  font-weight: 400;
  transition: all 0.2s;
  
  &.active {
    background-color: $primary;
    color: $bg-white;
    font-weight: 600;
    box-shadow: 0 4px 8px rgba($primary, 0.25);
  }
}

</style>