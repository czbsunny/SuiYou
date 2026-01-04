<template>
  <view class="add-goal-container">
    <!-- 顶部金额输入区 -->
    <view class="amount-card">
      <text class="label">预期目标金额</text>
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

      <!-- 选择图标项 -->
      <view class="form-item" @click="toggleIconPicker">
        <text class="item-label">选择图标</text>
        <view class="item-value">
          <!-- 图片图标：增加了具体的 class 和限制 -->
          <image v-if="isUrlIcon" :src="form.iconUrl" class="selected-icon-img" mode="aspectFit" />
          <!-- Emoji 图标 -->
          <text v-else class="selected-icon-emoji">{{ form.iconUrl }}</text>
          <uni-icons type="chevron-right" size="14" color="#ccc"></uni-icons>
        </view>
      </view>

      <view class="divider"></view>

      <picker mode="date" :value="form.deadline" @change="onDateChange">
        <view class="form-item">
          <text class="item-label">达成日期</text>
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
          <text class="item-label">设为主线目标</text>
          <text class="item-desc">开启后将置顶展示并可设置背景图</text>
        </view>
        <switch :checked="form.isPrimary" color="#2A806C" @change="onPrimaryChange" />
      </view>
    </view>

    <!-- 图标选择弹窗 (补全了缺失的 HTML) -->
    <view class="icon-picker-mask" v-if="showIconPicker" @click="toggleIconPicker">
      <view class="icon-picker-content" @click.stop>
        <view class="picker-header">选择一个好意头</view>
        <view class="icon-grid">
          <view 
            v-for="emoji in emojiList" 
            :key="emoji" 
            class="emoji-item"
            :class="{ active: form.iconUrl === emoji }"
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
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { goalService } from '@/services/goalService.js';

const configStore = useConfigStore();

const form = ref({
  title: '',
  targetAmount: '',
  iconUrl: '💰',
  deadline: '',
  isPrimary: false,
  categoryCode: '',
  templateCode: '',
  bgUrl: ''
});

const showIconPicker = ref(false);
const emojiList = ['💰', '🏠', '🚗', '🏔️', '📷', '💻', '🎓', '✈️', '💍', '🎁', '👶', '🏥'];

const isUrlIcon = computed(() => {
  return form.value.iconUrl && (form.value.iconUrl.startsWith('http') || form.value.iconUrl.startsWith('/static'));
});

onLoad((options) => {
  if (options.tpl) {
    const tplData = configStore.goalTemplates.find(t => t.code === options.tpl);
    if (tplData) {
      form.value.templateCode = tplData.templateCode;
      form.value.categoryCode = tplData.categoryCode;
      form.value.title = tplData.name;
      form.value.targetAmount = tplData.defaultAmount;
      form.value.iconUrl = tplData.iconUrl;

      if (tplData.defaultPeriodDays) {
        const date = new Date();
        date.setDate(date.getDate() + tplData.defaultPeriodDays);
        form.value.deadline = date.toISOString().split('T')[0];
      }
    }
  }
});

const toggleIconPicker = () => {
  showIconPicker.value = !showIconPicker.value;
};

const selectIcon = (emoji) => {
  form.value.iconUrl = emoji;
  showIconPicker.value = false;
};

const onDateChange = (e) => {
  form.value.deadline = e.detail.value;
};

const onPrimaryChange = (e) => {
  form.value.isPrimary = e.detail.value;
};

const handleSave = async () => {
  if (!form.value.title || !form.value.targetAmount) {
    uni.showToast({ title: '请填写名称和金额', icon: 'none' });
    return;
  }

  uni.showLoading({ title: '创建中...', mask: true });

  try {
    // 1. 调用 Service
    await goalService.createGoal(form.value);

    // 2. 成功反馈
    uni.hideLoading();
    uni.showToast({ title: '目标已开启！', icon: 'success' });
    
    // 3. 延时返回并刷新列表
    setTimeout(() => {
      // 🟢 方案 A：如果是返回 TabBar 页面，用 switchTab 最稳
      uni.switchTab({
        url: '/pages/goals/index',
        success: () => {
          // 这里的小技巧：通过全局事件或标志位通知目标页刷新
          // 虽然目标页 onShow 也会执行，但发个事件更精准
          uni.$emit('refreshGoalList'); 
        }
      });
    }, 1500);
  } catch (error) {
    uni.hideLoading();
    uni.showModal({
      title: '提示',
      content: error.message || '系统繁忙，请稍后再试',
      showCancel: false
    });
  }
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
  border-radius: 40rpx;
  padding: 50rpx 40rpx;
  margin-bottom: 40rpx;
  color: #fff;
  box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.05);
  transition: all 0.3s ease;
  background-color: $primary;
  
  .label {
    font-size: 24rpx;
    opacity: 0.8;
    margin-bottom: 20rpx;
    display: block;
  }

  .input-row {
    display: flex;
    align-items: baseline;
    .symbol { font-size: 60rpx; font-weight: bold; margin-right: 16rpx; }
    .amount-input {
      font-size: 80rpx;
      height: 100rpx;
      font-family: 'DIN Alternate', sans-serif;
      font-weight: bold;
      flex: 1;
    }
    .amount-placeholder { color: rgba(255, 255, 255, 0.3); }
  }
}

/* 表单组 */
.form-group {
  background-color: #fff;
  border-radius: 32rpx;
  padding: 0 30rpx;
  margin-bottom: 30rpx;
}

.form-item {
  min-height: 110rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 0;

  .item-label { font-size: 28rpx; color: #333; font-weight: 500; }
  .item-label-group {
    display: flex; flex-direction: column;
    .item-desc { font-size: 20rpx; color: #999; margin-top: 4rpx; }
  }

  .item-input { text-align: right; font-size: 28rpx; color: #333; flex: 1; margin-left: 40rpx; }

  .item-value {
    display: flex; align-items: center; gap: 10rpx;
    
    /* 图片 Icon 尺寸限制修复 */
    .selected-icon-img {
      width: 60rpx;
      height: 60rpx;
      border-radius: 12rpx;
    }
    .selected-icon-emoji { font-size: 44rpx; }
    .placeholder { color: #ccc; font-size: 28rpx; }
  }
}

.divider { height: 1rpx; background-color: #f5f5f5; }

/* 图标选择弹窗样式 */
.icon-picker-mask {
  position: fixed; inset: 0; background-color: rgba(0,0,0,0.5); z-index: 999;
  display: flex; align-items: flex-end;
}
.icon-picker-content {
  width: 100%; background-color: #fff; border-radius: 40rpx 40rpx 0 0;
  padding: 40rpx; padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  .picker-header { font-size: 30rpx; font-weight: bold; margin-bottom: 40rpx; text-align: center; }
  .icon-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 30rpx; }
  .emoji-item {
    height: 100rpx; display: flex; align-items: center; justify-content: center;
    font-size: 50rpx; background-color: #f8fafc; border-radius: 24rpx;
    &.active { border: 4rpx solid $primary; background-color: #eefdf5; }
  }
}

.footer-btn-box {
  padding: 40rpx 20rpx;
  .save-btn {
    color: #fff; height: 100rpx; line-height: 100rpx; border-radius: 50rpx; background-color: $primary;
    font-size: 32rpx; font-weight: bold; box-shadow: 0 10rpx 20rpx rgba(0,0,0,0.1);
    &:active { transform: scale(0.98); }
  }
}
</style>