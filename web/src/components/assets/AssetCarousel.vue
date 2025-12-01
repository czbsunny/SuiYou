<template>
  <view class="card-stage">
    <view 
      v-for="(item, index) in list" 
      :key="item.id"
      class="apple-card"
      :class="getCardClass(index)"
      :style="{ background: `linear-gradient(135deg, ${item.color1}, ${item.color2})` }"
      @click="handleCardClick(index)"
    >
      <!-- 噪点纹理 -->
      <view class="card-noise"></view>
      
      <!-- 卡片内容 -->
      <view class="card-content">
        <view class="card-top">
          <view class="icon-circle">
            <!-- 这里使用了 text 代替 i 标签，如果是 uniapp 建议用 image 或 uni-icons -->
             <i class="fa-solid text-sm" :class="item.icon"></i>
          </view>
          <text class="card-name">{{ item.name }}</text>
        </view>
        <view>
          <text class="card-total">{{ item.total }}</text>
          <text class="card-mask">**** 8848</text>
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
  perspective: 1200px;
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
  border-radius: 20px;
  background: white;
  box-shadow: 0 15px 35px -5px rgba(0,0,0,0.3), 0 0 0 1px rgba(255,255,255,0.15) inset;
  transition: all 0.5s cubic-bezier(0.19, 1, 0.22, 1);
  transform-origin: center bottom;
  overflow: hidden;
}

.card-noise {
  position: absolute; inset: 0; opacity: 0.05;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
}

.card-content {
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: white;
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
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}

.card-name {
  font-weight: 700;
  letter-spacing: 0.5px;
  font-size: 18px;
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.card-total {
  font-family: monospace;
  font-size: 30px;
  font-weight: 700;
  letter-spacing: -0.5px;
  display: block;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.card-mask {
  font-size: 10px;
  opacity: 0.8;
  margin-top: 8px;
  font-family: monospace;
  display: block;
}

/* 3D 状态类 */
.apple-card.active {
  transform: translate3d(0, 0, 20px) scale(1);
  z-index: 20;
  opacity: 1;
}
.apple-card.next {
  transform: translate3d(60px, 0, -100px) scale(0.9) rotateY(-15deg);
  z-index: 10;
  opacity: 0.7;
  filter: brightness(0.9);
}
.apple-card.prev {
  transform: translate3d(-60px, 0, -100px) scale(0.9) rotateY(15deg);
  z-index: 10;
  opacity: 0.7;
  filter: brightness(0.9);
}
.apple-card.hidden-card {
  transform: translate3d(0, 0, -200px) scale(0.8);
  z-index: 0;
  opacity: 0;
}
</style>