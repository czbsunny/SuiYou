<template>
  <view class="keyboard-container">
    <!-- 1. 键盘遮罩层：透明度调轻，色调偏暖 -->
    <view class="keyboard-mask" @tap="$emit('close')"></view>

    <!-- 2. 自定义数字键盘区 -->
    <view class="keyboard-fixed">
      <!-- 提交汇总条：通常用于显示当前输入总额 -->
      <view class="submit-bar" v-if="showSubmitBar && submitBarData">
        <view class="total-info">
          {{ submitBarData.label }}: 
          <text class="total-val num-font">{{ submitBarData.value }}</text>
        </view>
        <view class="submit-btn" @tap="$emit('submit')" hover-class="hover-opacity">
          {{ submitBarData.submitText }}
        </view>
      </view>
      
      <!-- 键盘按键网格 -->
      <view class="keys-grid">
        <!-- 左侧：数字区 -->
        <view class="num-keys">
          <view v-for="key in ['1','2','3','4','5','6','7','8','9','0']" 
            :key="key" 
            class="key-btn num-font" 
            @touchstart.stop.prevent="handleKeyPress(key)" 
            hover-class="key-hover-bg"
          >
            {{ key }}
          </view>
          <!-- 小数点与负号 -->
          <view class="key-btn num-font" :class="{ disabled: disabledKeys.includes('.') }" @touchstart.stop.prevent="handleKeyPress('.')" hover-class="key-hover-bg">
            .
          </view>
          <view class="key-btn num-font" :class="{ disabled: disabledKeys.includes('-') }" @touchstart.stop.prevent="handleKeyPress('-')" hover-class="key-hover-bg">
            -
          </view>
        </view>

        <!-- 右侧：功能区 -->
        <view class="side-keys">
          <!-- 删除键 -->
          <view class="key-btn del-key" @touchstart.stop.prevent="handleKeyPress('delete')" hover-class="key-hover-bg">
            <image src="/static/images/del-key.png" mode="aspectFit" class="del-icon" />
          </view>
          <!-- 下一步键 -->
          <view class="key-btn next-key" @tap="handleNextField" hover-class="next-key-hover">
            {{ nextText }}
          </view>
          <!-- 完成键 -->
          <view class="key-btn done-key" @tap="handleDone" hover-class="done-key-hover">
            {{ doneText }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  activeField: { type: String, default: 'amount' },
  disabledKeys: { type: Array, default: () => [] },
  showSubmitBar: { type: Boolean, default: false },
  submitBarData: { type: Object, default: null },
  nextText: { type: String, default: '下一步' },
  doneText: { type: String, default: '完成' }
});

const emit = defineEmits(['keyPress', 'nextField', 'done', 'close', 'submit']);

const handleKeyPress = (key) => {
  // 增加触感反馈，金融级体验
  uni.vibrateShort();
  emit('keyPress', key);
};

const handleNextField = () => {
  uni.vibrateShort();
  emit('nextField');
};

const handleDone = () => {
  uni.vibrateShort();
  emit('done');
};
</script>

<style lang="scss" scoped>
/* 遮罩层：配合环境色 */
.keyboard-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba($text-main, 0.1);
  z-index: 998;
}

.keyboard-fixed {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: $bg-white; 
  border-radius: $radius-lg $radius-lg 0 0; // 32rpx 圆角
  padding: $spacing-base; 
  padding-bottom: $spacing-lg;
  box-shadow: 0 -10rpx 40rpx rgba(189, 182, 175, 0.2); // 环境色投影
  z-index: 999;
  animation: slideUp 0.3s cubic-bezier(0.25, 1, 0.5, 1);

  /* 汇总条重构 */
  .submit-bar {
    display: flex; justify-content: space-between; align-items: center;
    padding: 0 10rpx 30rpx;
    
    .total-info { 
      font-size: 26rpx; 
      color: $text-placeholder; 
      .total-val { 
        font-size: 36rpx; 
        color: $text-main; 
        font-weight: $fw-semibold; 
        margin-left: 10rpx;
      } 
    }
    
    .submit-btn { 
      background: linear-gradient(to right, $primary, $primary-pressed);
      color: $text-inverse; 
      padding: 18rpx 50rpx; 
      border-radius: 100rpx; 
      font-size: 28rpx; 
      font-weight: $fw-semibold;
      box-shadow: $shadow-button;
    }
  }

  .keys-grid { display: flex; gap: 20rpx; }
  .num-keys { flex: 3; display: grid; grid-template-columns: repeat(3, 1fr); gap: 20rpx; }
  .side-keys { flex: 1; display: flex; flex-direction: column; gap: 20rpx; }

  /* 按键基础样式 */
  .key-btn {
    height: 108rpx; 
    background: $bg-page; // 暖米白背景
    border-radius: $radius-base;
    @include flex-center;
    font-size: 44rpx; 
    font-weight: $fw-medium; 
    color: $text-main; // 深咖啡黑
    transition: all 0.1s;
    
    /* 交互反馈 */
    &.key-hover-bg { 
      background: $bg-subtle-hover; 
      transform: scale(0.96); 
    }
    
    /* 功能键：下一步 */
    &.next-key { 
      flex: 1; 
      background: $primary-subtle;
      color: $primary; 
      font-size: 32rpx; 
      font-weight: $fw-semibold;
      
      &.next-key-hover {
        background: rgba($primary, 0.15);
      }
    }
    
    /* 功能键：完成 */
    &.done-key { 
      flex: 1; 
      background: $primary;
      color: $text-inverse; 
      font-size: 32rpx; 
      font-weight: $fw-semibold;
      
      &.done-key-hover {
        background: $primary-pressed;
      }
    }
    
    &.disabled { 
      opacity: 0.2; 
      pointer-events: none; 
    }
  }

  .del-icon {
    width: 48rpx;
    height: 48rpx;
    // 如果图标是黑色的，给它一点暖色调处理
    opacity: 0.8;
  }
}

@keyframes slideUp { 
  from { transform: translateY(100%); } 
  to { transform: translateY(0); } 
}
</style>