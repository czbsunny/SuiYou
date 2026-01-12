<!-- components/assets/AssetCategorySelector.vue -->
<template>
  <!-- 🟢 根节点绑定动态颜色变量 -->
  <view class="section-card" :style="{ '--active-color': currentActiveColor }">
    <view class="card-title">资产归类</view>
    <view class="card-body">
      <!-- 1. 大类选择 (L1) -->
      <scroll-view scroll-x class="type-scroll" :show-scrollbar="false">
        <view class="type-scroll-inner">
          <view 
            v-for="type in categories" 
            :key="type.code"
            class="type-item" 
            :class="{ active: modelValue === type.code }"
            @click="$emit('update:modelValue', type.code)"
          >
            <!-- 磁贴式图标容器 -->
            <view class="icon-circle" :class="{ active: modelValue === type.code }">
              <image :src="type.icon" class="type-icon" mode="aspectFit" />
            </view>
            <text class="type-text">{{ type.name }}</text>
          </view>
        </view>
      </scroll-view>

      <view class="divider" v-if="subcategories && subcategories.length > 0"></view>

      <!-- 2. 二级分类标签 (L2) -->
      <view class="tags-container" v-if="subcategories && subcategories.length > 0">
        <view class="tags-label">选择类型</view>
        <view class="tags-wrapper">
          <view 
            v-for="sub in subcategories" 
            :key="sub.categoryCode"
            class="tag-item" 
            :class="{ active: subValue === sub.categoryCode }"
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
import { computed } from 'vue';

/**
 * categories 数组对象示例：
 * { code: 'LIQUID', name: '流动资产', color: '#2A806C', iconUrl: '...' }
 */
const props = defineProps({
  categories: Array,
  modelValue: String,      // 选中的大类 code
  subcategories: Array,
  subValue: String         // 选中的子类 code
});

const emit = defineEmits(['update:modelValue', 'update:subValue']);

// 🟢 计算属性：根据当前 modelValue 提取该大类的专属色
const currentActiveColor = computed(() => {
  if (!props.categories || props.categories.length === 0) return '#2A806C';
  const activeCat = props.categories.find(c => c.code === props.modelValue);
  return activeCat ? activeCat.color : props.categories[0].color;
});
</script>

<style lang="scss" scoped>
// 基础变量定义（仅限中性色）
$bg-white: #FFFFFF;
$tag-inactive-bg: #F5F7FA;
$text-main: #1F2937;
$text-sub: #9CA3AF;

.section-card {
  background-color: $bg-white;
  border-radius: 20px;
  padding: 24px 18px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
  transition: all 0.3s;
  
  .card-title {
    font-size: 15px;
    font-weight: 800;
    color: $text-main;
    margin-bottom: 20px;
    padding-left: 12px;
    // 🟢 引导条颜色同步大类色
    border-left: 4px solid var(--active-color); 
    line-height: 1;
    transition: border-color 0.4s ease;
  }
}

.type-scroll {
  width: 100%;
  white-space: nowrap;
}

.type-scroll-inner {
  display: flex;
  padding-bottom: 4px;
}

.type-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  margin-right: 22px;
  flex-shrink: 0;
  
  .icon-circle {
    width: 52px; height: 52px;
    border-radius: 18px;
    background-color: $tag-inactive-bg;
    display: flex; align-items: center; justify-content: center;
    margin-bottom: 10px;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    
    .type-icon {
      width: 28px; height: 28px;
      // 🟢 非选中态：置灰 + 变淡
      filter: grayscale(1);
      opacity: 0.3;
      transition: all 0.4s ease;
    }
    
    &.active {
      // 🟢 选中态：使用分类本色
      background-color: var(--active-color);
      // 带有本色的微弱光晕
      box-shadow: 0 8px 16px -4px rgba(0, 0, 0, 0.12);
      
      .type-icon {
        // 🟢 选中态：图标利用滤镜强制转为纯白色
        filter: brightness(0) invert(1);
        opacity: 1;
        transform: scale(1.05);
      }
    }
  }
  
  .type-text {
    font-size: 12px;
    color: $text-sub;
    font-weight: 500;
    transition: all 0.3s;
  }

  /* 大类选中的文字样式 */
  &.active {
    .type-text {
      color: var(--active-color);
      font-weight: 800;
      transform: translateY(-2px);
    }
  }
}

.divider {
  height: 1px;
  background-color: rgba(0, 0, 0, 0.04);
  margin: 16px 4px 20px;
}

.tags-container {
  .tags-label {
    font-size: 12px;
    color: $text-sub;
    margin-bottom: 12px;
    font-weight: 500;
  }
}

.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  padding: 8px 18px;
  border-radius: 10px;
  background-color: $tag-inactive-bg;
  color: #6B7280;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    opacity: 0.7;
  }
  
  &.active {
    // 🟢 选中的二级标签同步分类色背景
    background-color: var(--active-color);
    color: $bg-white;
    font-weight: 600;
    box-shadow: 0 6px 12px -2px rgba(0, 0, 0, 0.15);
  }
}
</style>