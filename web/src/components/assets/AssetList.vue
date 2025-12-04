<template>
  <view class="list-section">
    <view class="list-header">
      <text class="list-title">{{ data.name }}明细</text>
      <text class="list-total">{{ data.total }}</text>
    </view>

    <view class="list-wrapper" :class="{ 'fade-in': animateTrigger }">
      <view v-for="(item, idx) in data.list" :key="idx" class="list-item">
        <view class="item-left">
          <!-- 图标处理：判断是 class 还是图片 -->
          <view class="item-icon-box">
             <image v-if="item.i === 'img'" src="https://cdn-icons-png.flaticon.com/512/2534/2534195.png" class="icon-img" />
             <i v-else class="fa-solid text-lg" :class="item.i"></i>
          </view>
          <view>
            <view class="item-name">{{ item.n }}</view>
            <view class="item-desc">{{ item.s }}</view>
          </view>
        </view>
        <text class="item-amount">{{ item.a }}</text>
      </view>
      
      <!-- 添加资产按钮 -->
      <view class="add-button" @click="navigateToAddAsset">
        <image src="/static/images/plus-gray.png" class="plus-icon" />
        <text>添加{{ data.name }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { defineProps, watch, ref, onMounted } from 'vue';

const props = defineProps({
  data: { type: Object, required: true }
});

const navigateToAddAsset = () => {
  uni.navigateTo({
    url: '/pages/assets/add'
  });
};

const animateTrigger = ref(false);

// 组件挂载时触发动画
onMounted(() => {
  animateTrigger.value = true;
});

// 监听数据变化，触发简单动画
watch(() => props.data, () => {
  animateTrigger.value = false;
  setTimeout(() => {
    animateTrigger.value = true;
  }, 50);
});
</script>

<style lang="scss" scoped>
.list-section {
  padding: 0 20px;
  margin-top: 8px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.list-title {
  font-size: 14px;
  font-weight: 700;
  color: #6b7280;
}

.list-total {
  font-size: 12px;
  font-weight: 700;
  font-family: monospace;
  color: #2c3e50;
}

.list-wrapper {
  opacity: 0;
  transform: translateY(10px);
  transition: opacity 0.3s ease, transform 0.3s ease;
  
  &.fade-in {
    opacity: 1;
    transform: translateY(0);
  }
}

.list-item {
  background: white;
  padding: 16px;
  border-radius: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  box-shadow: 0 2px 15px rgba(0,0,0,0.03);
  border: 1px solid white;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-icon-box {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: #f9fafb;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4b5563;
}
.icon-img {
  width: 36px; height: 36px;
}

.item-name {
  font-size: 14px;
  font-weight: 700;
  color: #2c3e50;
}
.item-desc {
  font-size: 10px;
  color: #9ca3af;
}
.item-amount {
  font-family: monospace;
  font-weight: 700;
  color: #2c3e50;
}
/* 添加资产按钮样式 */
.add-account-btn {
  width: 100%;
  padding: 16px 0;
  border: 2px dashed $border-color;
  border-radius: 16px;
  color: $text-muted;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.5);
  transition: all 0.3s;
  box-sizing: border-box;
  margin-top: 12px;
}

.add-account-btn:active {
  background-color: #ffffff;
  border-color: $primary;
  color: $primary;
}

.add-icon {
  width: 18px;
  height: 18px;
  margin-right: 8px;
}
</style>