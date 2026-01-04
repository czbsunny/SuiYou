<template>
  <view class="add-goal-container">
    <!-- 顶部金额输入区 -->
    <view class="amount-card">
      <text class="label">目标金额</text>
      <view class="input-row">
        <text class="symbol">¥</text>
        <input 
          type="digit" 
          v-model="form.targetAmount" 
          placeholder="0.00" 
          class="amount-input"
          placeholder-class="amount-placeholder"
        />
      </view>
    </view>

    <!-- 基础配置组 -->
    <view class="form-group">
      <view class="form-item">
        <text class="item-label">目标名称</text>
        <input 
          v-model="form.title" 
          placeholder="例如：换台新电脑" 
          class="item-input" 
          placeholder-class="placeholder"
        />
      </view>

      <view class="divider"></view>

      <view class="form-item" @click="toggleIconPicker">
        <text class="item-label">选择图标</text>
        <view class="item-value">
          <text class="selected-icon">{{ form.icon }}</text>
          <uni-icons type="chevron-right" size="14" color="#ccc"></uni-icons>
        </view>
      </view>

      <view class="divider"></view>

      <picker mode="date" :value="form.deadline" @change="onDateChange">
        <view class="form-item">
          <text class="item-label">目标日期</text>
          <view class="item-value">
            <text :class="{ 'placeholder': !form.deadline }">
              {{ form.deadline || '点击选择日期' }}
            </text>
            <uni-icons type="chevron-right" size="14" color="#ccc"></uni-icons>
          </view>
        </view>
      </picker>
    </view>

    <!-- 高级配置组 -->
    <view class="form-group">
      <view class="form-item">
        <view class="item-label-group">
          <text class="item-label">设为主线</text>
          <text class="item-desc">开启后将置顶在主视图</text>
        </view>
        <switch :checked="form.isPrimary" color="#2A806C" @change="onPrimaryChange" />
      </view>
    </view>

    <!-- 简易图标选择面板 (点击图标项弹出) -->
    <view class="icon-picker-mask" v-if="showIconPicker" @click="toggleIconPicker">
      <view class="icon-picker-content" @click.stop>
        <view class="picker-header">选择一个好意头</view>
        <view class="icon-grid">
          <view 
            v-for="emoji in emojiList" 
            :key="emoji" 
            class="emoji-item"
            :class="{ active: form.icon === emoji }"
            @click="selectIcon(emoji)"
          >
            {{ emoji }}
          </view>
        </view>
      </view>
    </view>

    <!-- 底部保存按钮 -->
    <view class="footer-btn-box">
      <button class="save-btn" @click="handleSave">确认开启目标</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';

const form = ref({
  title: '',
  targetAmount: '',
  icon: '💰',
  deadline: '',
  isPrimary: false
});

const showIconPicker = ref(false);
const emojiList = ['💰', '🏠', '🚗', '🏔️', '📷', '💻', '🎓', '✈️', '💍', '🎁', '👶', '🏥'];

const toggleIconPicker = () => {
  showIconPicker.value = !showIconPicker.value;
};

const selectIcon = (emoji) => {
  form.value.icon = emoji;
  showIconPicker.value = false;
};

const onDateChange = (e) => {
  form.value.deadline = e.detail.value;
};

const onPrimaryChange = (e) => {
  form.value.isPrimary = e.detail.value;
};

const handleSave = () => {
  if (!form.value.title || !form.value.targetAmount) {
    uni.showToast({ title: '请填写名称和金额', icon: 'none' });
    return;
  }
  
  uni.showLoading({ title: '创建中...' });
  
  // 模拟保存接口请求
  setTimeout(() => {
    uni.hideLoading();
    uni.showToast({ title: '开启新征程！', icon: 'success' });
    
    // 延迟返回，让用户看一眼成功提示
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  }, 800);
};
</script>

<style lang="scss" scoped>
.add-goal-container {
  min-height: 100vh;
  background-color: #F9F8F4;
  padding: 30rpx;
}

/* 顶部金额卡片 */
.amount-card {
  background-color: #2A806C;
  border-radius: 40rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
  color: #fff;
  box-shadow: 0 10rpx 30rpx rgba(42, 128, 108, 0.2);

  .label {
    font-size: 24rpx;
    opacity: 0.8;
    margin-bottom: 20rpx;
    display: block;
  }

  .input-row {
    display: flex;
    align-items: baseline;
    
    .symbol {
      font-size: 80rpx;
      font-weight: bold;
      margin-right: 16rpx;
    }

    .amount-input {
      font-size: 80rpx;
      height: 100rpx;
      font-family: 'DIN Alternate', sans-serif;
      font-weight: bold;
    }

    .amount-placeholder {
      color: rgba(255, 255, 255, 0.3);
    }
  }
}

/* 表单组 */
.form-group {
  background-color: #fff;
  border-radius: 32rpx;
  padding: 0 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 15rpx rgba(0,0,0,0.02);
}

.form-item {
  height: 110rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .item-label {
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
  }

  .item-label-group {
    display: flex;
    flex-direction: column;
    .item-desc { font-size: 20rpx; color: #999; margin-top: 4rpx; }
  }

  .item-input {
    text-align: right;
    font-size: 28rpx;
    color: #333;
    flex: 1;
    margin-left: 40rpx;
  }

  .item-value {
    display: flex;
    align-items: center;
    gap: 10rpx;
    font-size: 28rpx;
    color: #333;

    .selected-icon {
      font-size: 40rpx;
    }

    .placeholder { color: #ccc; }
  }
}

.divider {
  height: 1rpx;
  background-color: #f5f5f5;
}

/* 图标选择弹窗 */
.icon-picker-mask {
  position: fixed;
  inset: 0;
  background-color: rgba(0,0,0,0.4);
  z-index: 100;
  display: flex;
  align-items: flex-end;

  .icon-picker-content {
    width: 100%;
    background-color: #fff;
    border-top-left-radius: 40rpx;
    border-top-right-radius: 40rpx;
    padding: 40rpx;
    padding-bottom: calc(40rpx + env(safe-area-inset-bottom));

    .picker-header {
      font-size: 32rpx;
      font-weight: bold;
      margin-bottom: 40rpx;
      text-align: center;
    }

    .icon-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 30rpx;
    }

    .emoji-item {
      height: 100rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 50rpx;
      background-color: #f8fafc;
      border-radius: 20rpx;
      transition: all 0.2s;
      
      &.active {
        background-color: #eefdf5;
        border: 2rpx solid #2A806C;
      }
    }
  }
}

/* 底部按钮 */
.footer-btn-box {
  padding: 40rpx 20rpx;
  
  .save-btn {
    background-color: #2A806C;
    color: #fff;
    height: 100rpx;
    line-height: 100rpx;
    border-radius: 50rpx;
    font-size: 30rpx;
    font-weight: bold;
    box-shadow: 0 10rpx 20rpx rgba(42, 128, 108, 0.2);
    
    &:active { transform: scale(0.98); opacity: 0.9; }
  }
}
</style>