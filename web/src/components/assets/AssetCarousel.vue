<template>
  <view class="card-stage">
    <view 
      v-for="(item, index) in list" 
      :key="item.id"
      class="apple-card"
      :class="getCardClass(index)"
      @click="handleCardClick(index)"
    >
      <!-- 卡片内容 -->
      <view class="card-content">
        <view class="card-top">
          <view class="icon-circle">
             <i class="fa-solid text-sm" :class="item.icon"></i>
          </view>
          <text class="card-name">{{ item.name }}</text>
        </view>
        <view>
          <text class="card-total">{{ item.total }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  list: { type: Array, required: true },
  current: { type: Number, default: 0 }
});

const emit = defineEmits(['update:current']);

// 循环索引计算辅助函数
const getLoopIndex = (idx) => {
  const len = props.list.length;
  return (idx % len + len) % len;
};

// 计算卡片样式类
const getCardClass = (index) => {
  const len = props.list.length;
  const current = props.current;
  
  // 计算相对位置
  let diff = (index - current) % len;
  if (diff < -len/2) diff += len;
  if (diff > len/2) diff -= len;

  if (diff === 0) return 'active';
  if (diff === 1) return 'next';
  if (diff === -1) return 'prev';
  return 'hidden-card';
};

// 点击处理
const handleCardClick = (clickedIndex) => {
  const len = props.list.length;
  const current = props.current;
  
  let diff = (clickedIndex - current) % len;
  if (diff < -len/2) diff += len;
  if (diff > len/2) diff -= len;

  if (diff === 1) {
    emit('update:current', getLoopIndex(current + 1));
  } else if (diff === -1) {
    emit('update:current', getLoopIndex(current - 1));
  }
};
</script>

<style lang="scss" scoped>
.card-stage {
  height: 220px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  margin-bottom: 10px;
}

.apple-card {
  position: absolute;
  width: 300px;
  height: 190px;
  border-radius: 16px;
  background: white;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  transform-origin: center bottom;
  overflow: hidden;
}

.card-content {
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #1f2937;
  position: relative;
  z-index: 10;
  box-sizing: border-box;
}

.card-top {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-circle {
  width: 32px; height: 32px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #2a806c;
}

.card-name {
  font-weight: 700;
  letter-spacing: 0.5px;
  font-size: 18px;
  color: #4b5563;
}

.card-total {
  font-family: monospace;
  font-size: 30px;
  font-weight: 700;
  letter-spacing: -0.5px;
  display: block;
  color: #2a806c;
}

/* 卡片状态类 */
.apple-card.active {
  transform: translate3d(0, 0, 0) scale(1);
  z-index: 20;
  opacity: 1;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border-color: #2a806c;
}
.apple-card.next {
  transform: translate3d(60px, 0, 0) scale(0.9);
  z-index: 10;
  opacity: 0.8;
}
.apple-card.prev {
  transform: translate3d(-60px, 0, 0) scale(0.9);
  z-index: 10;
  opacity: 0.8;
}
.apple-card.hidden-card {
  transform: translate3d(0, 0, 0) scale(0.8);
  z-index: 0;
  opacity: 0;
}
</style>