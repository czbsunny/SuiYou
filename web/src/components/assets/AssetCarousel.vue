<template>
  <view class="card-stage">
    <view 
      v-for="(item, index) in list" 
      :key="item.id"
      class="apple-card"
      :class="getCardClass(index)"
      :style="getDynamicStyle(item)"
      @click="handleCardClick(index)"
    >
      <!-- 增加一层磨砂纹理光泽，解决颜色太亮眼的问题 -->
      <view class="card-gloss"></view>

      <view class="card-content" :style="{ color: getTextColor(item.color) }">
        <view class="card-top">
          <view class="icon-circle" :style="{ backgroundColor: getIconBgColor(item.color) }">
             <image class="icon-img" :src="item.iconUrl" :style="{ color: getTextColor(item.color) }"></image>
          </view>
          <text class="card-name">{{ item.name }}</text>
        </view>
        <view class="card-bottom">

          <text class="card-total">{{ item.total || '¥ 0.00' }}</text>
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

// 动态样式：注入渐变层和动态阴影
const getDynamicStyle = (item) => {
  const baseColor = item.color || '#2a806c';
  return {
    // 叠加一层145度角的微弱白光，增加哑光质感
    background: `linear-gradient(145deg, rgba(255,255,255,0.2) 0%, rgba(0,0,0,0) 100%), ${baseColor}`,
    boxShadow: `0 12px 30px ${hexToRgba(baseColor, 0.3)}`
  };
};

// 感知亮度算法：决定文字用深色还是白色
const getTextColor = (hexcolor) => {
  if (!hexcolor) return '#FFFFFF';
  const hex = hexcolor.replace("#", "");
  const r = parseInt(hex.substr(0, 2), 16);
  const g = parseInt(hex.substr(2, 2), 16);
  const b = parseInt(hex.substr(4, 2), 16);
  const brightness = (r * 299 + g * 587 + b * 114) / 1000;
  // 阈值调高到180，确保浅色背景下文字极清晰，深色/中性背景下一律白字
  return brightness > 180 ? '#1f2937' : '#FFFFFF';
};

const getIconBgColor = (hexcolor) => {
  const isLight = getTextColor(hexcolor) === '#1f2937';
  return isLight ? 'rgba(0,0,0,0.08)' : 'rgba(255,255,255,0.25)';
};

const hexToRgba = (hex, alpha) => {
  const r = parseInt(hex.replace('#','').substr(0, 2), 16);
  const g = parseInt(hex.replace('#','').substr(2, 2), 16);
  const b = parseInt(hex.replace('#','').substr(4, 2), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
};

const getLoopIndex = (idx) => {
  const len = props.list.length;
  return (idx % len + len) % len;
};

const getCardClass = (index) => {
  const len = props.list.length;
  const current = props.current;
  let diff = (index - current) % len;
  if (diff < -len/2) diff += len;
  if (diff > len/2) diff -= len;

  if (diff === 0) return 'active';
  if (diff === 1) return 'next';
  if (diff === -1) return 'prev';
  return 'hidden-card';
};

const handleCardClick = (clickedIndex) => {
  const len = props.list.length;
  const current = props.current;
  let diff = (clickedIndex - current) % len;
  if (diff < -len/2) diff += len;
  if (diff > len/2) diff -= len;

  if (diff === 1) emit('update:current', getLoopIndex(current + 1));
  else if (diff === -1) emit('update:current', getLoopIndex(current - 1));
};
</script>

<style lang="scss" scoped>
.card-stage {
  height: 240px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  perspective: 1000px; // 激活3D空间感
}

.apple-card {
  position: absolute;
  width: 300px;
  height: 180px;
  border-radius: 24px; // 调大圆角更显精致
  transition: all 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
  transform-origin: center center;
  overflow: hidden;
  border: none; // 纯色渐变不需要边框
}

.card-gloss {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  // 增加极其微弱的噪点纹理
  background-image: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0IiBoZWlnaHQ9IjQiPgo8cmVjdCB3aWR0aD0iMSIgaGVpZ2h0PSIxIiBmaWxsPSJyZ2JhKDI1NSwyNTUsMjU1LDAuMDUpIj48L3JlY3Q+Cjwvc3ZnPg==');
  pointer-events: none;
}

.card-content {
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  z-index: 10;
  box-sizing: border-box;
  // 关键：给文字加微影，解决模糊问题
  text-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.card-top {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-circle {
  width: 40px; height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;

  .icon-img {
    width: 36rpx; 
    height: 36rpx;
  }
}

.card-name {
  font-weight: 700;
  font-size: 19px;
  letter-spacing: 0.5px;
}

.card-bottom {
  display: flex;
  flex-direction: column;
}

.card-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 4px;
}

.card-total {
  // 建议在App.vue中引入 DIN Alternate 字体，如果没有则回退
  font-family: 'DIN Alternate', 'Courier New', monospace;
  font-size: 34px;
  font-weight: 700;
  letter-spacing: -1px;
}

/* 核心动画：增加 translate3d 的 Z 轴位移，产生前后堆叠感 */
.apple-card.active {
  transform: translate3d(0, 0, 0) scale(1);
  z-index: 20;
  opacity: 1;
}
.apple-card.next {
  transform: translate3d(85px, 0, -100px) scale(0.85);
  z-index: 10;
  opacity: 0.6;
}
.apple-card.prev {
  transform: translate3d(-85px, 0, -100px) scale(0.85);
  z-index: 10;
  opacity: 0.6;
}
.apple-card.hidden-card {
  transform: translate3d(0, 0, -250px) scale(0.7);
  z-index: 0;
  opacity: 0;
}
</style>