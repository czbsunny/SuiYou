<template>
  <view class="add-account-container">
    <!-- 1. 实时预览区：卡片工坊 (已根据最新需求重构布局) -->
    <view class="preview-hero">
      <view 
        class="live-card" 
        :style="{ background: cardBackground, color: contrastTextColor }"
      >
        <view class="glass-texture"></view>
        
        <view class="card-inner">
          <!-- 第一部分：账户头部信息 (Logo + 机构-别名) -->
          <view class="header-section">
            <view class="wc-row-top">
              <view class="brand-group">
                <view class="logo-white-box">
                  <image v-if="form.logoUrl" :src="form.logoUrl" mode="aspectFit" class="logo" />
                  <i v-else class="fa-solid fa-building-columns" :style="{ color: form.bgColor }"></i>
                </view>
                <view class="name-area">
                  <text class="inst-name">{{ form.instName || '选择机构' }}</text>
                  <text v-if="form.accountName" class="acc-alias"> - {{ form.accountName }}</text>
                </view>
              </view>
            </view>

            <!-- 第二行：识别码 (带间距) -->
            <view class="wc-row-id">
              <text class="id-text">**** **** {{ form.identifier || '0000' }}</text>
            </view>
          </view>

          <!-- 第二部分：财务数据与页脚 (强制推到底部) -->
          <view class="footer-section">
            <!-- 第三行：初始化金额 -->
            <view class="wc-row-balance">
              <view class="balance-left">
                <text class="symbol">¥</text>
                <text class="num">0.00</text>
              </view>
            </view>

            <!-- 第四行：页脚项 -->
            <view class="wc-row-footer">
              <text class="footer-info">待录入资产</text>
              <view class="item-tag">0 项资产</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 配置表单区 (保持原样) -->
    <scroll-view scroll-y class="form-scroll">
      <view class="form-group">
        <view class="form-item" @tap="goToSelectInstitution">
          <text class="item-label">所属机构</text>
          <view class="item-value">
            <text :class="{ 'placeholder': !form.instName }">{{ form.instName || '点击选择' }}</text>
            <uni-icons type="right" size="14" color="#ccc"></uni-icons>
          </view>
        </view>

        <view class="form-item">
          <text class="item-label">账户别名</text>
          <input 
            v-model="form.accountName" 
            placeholder="如：工资卡 / 常用微信" 
            class="item-input" 
            placeholder-class="placeholder"
          />
        </view>

        <view class="form-item">
          <view class="item-label-group">
            <text class="item-label">识别码</text>
          </view>
          <input 
            v-model="form.identifier" 
            type="number" 
            maxlength="4"
            placeholder="如：卡末4位 / 手机末4位" 
            class="item-input" 
            placeholder-class="placeholder"
          />
        </view>
      </view>

      <!-- 3. 自定义卡面颜色 -->
      <view class="color-section">
        <view class="section-title">卡面配色</view>
        <view class="color-grid">
          <view 
            v-for="color in presetColors" 
            :key="color" 
            class="color-dot-box"
            :class="{ 'active': form.bgColor === color }"
            @tap="form.bgColor = color"
          >
            <view class="color-dot" :style="{ backgroundColor: color }"></view>
          </view>
        </view>
      </view>

      <!-- 4. 高级设置 -->
      <view class="form-group">
        <view class="form-item">
          <view class="item-label-group">
            <text class="item-label">计入总资产</text>
            <text class="item-desc">开启后金额将纳入净值统计</text>
          </view>
          <switch :checked="form.includeInNetWorth" color="#2D7A68" @change="onSwitchChange" />
        </view>
      </view>

      <view class="bottom-spacer"></view>
    </scroll-view>

    <!-- 5. 底部固定按钮 -->
    <view class="footer-action">
      <button class="save-btn" @tap="handleSave">确认添加该账户</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { createAccount } from '@/services/accountService.js';

const form = ref({
  instCode: '',
  instName: '',
  logoUrl: '',
  accountName: '',
  identifier: '',
  bgColor: '#1F2937', 
  includeInNetWorth: true
});

const presetColors = [
  '#1F2937', '#2D7A68', '#E72D2D', '#003B8F', '#1677FF', 
  '#07C160', '#5F27CD', '#D97706', '#FA8231', '#0FB9B1'
];

const cardBackground = computed(() => form.value.bgColor);

const contrastTextColor = computed(() => {
  const hex = form.value.bgColor.replace('#', '');
  const r = parseInt(hex.substr(0, 2), 16);
  const g = parseInt(hex.substr(2, 2), 16);
  const b = parseInt(hex.substr(4, 2), 16);
  const brightness = (r * 299 + g * 587 + b * 114) / 1000;
  return brightness > 185 ? '#1C1C1E' : '#FFFFFF';
});

onLoad(() => {
  uni.$on('institutionSelected', (inst) => {
    form.value.instCode = inst.instCode;
    form.value.instName = inst.instName;
    form.value.logoUrl = inst.logoUrl;
    if (inst.themeColor) {
      form.value.bgColor = inst.themeColor;
      uni.vibrateShort();
    }
  });
});

onUnmounted(() => {
  uni.$off('institutionSelected');
});

const goToSelectInstitution = () => {
  uni.navigateTo({ url: '/pages/assets/institution-select' });
};

const onSwitchChange = (e) => {
  form.value.includeInNetWorth = e.detail.value;
};

const handleSave = async () => {
  if (!form.value.instCode) {
    uni.showToast({ title: '请选择机构', icon: 'none' });
    return;
  }
  if (!form.value.accountName) {
    uni.showToast({ title: '请输入账户别名', icon: 'none' });
    return;
  }
  if (!form.value.identifier) {
    uni.showToast({ title: '请输入识别码', icon: 'none' });
    return;
  }
  if (form.value.identifier.length !== 4) {
    uni.showToast({ title: '识别码必须为4位数', icon: 'none' });
    return;
  }

  uni.showLoading({ title: '正在创建...' });
  try {
    const response = await createAccount({
      instCode: form.value.instCode,
      identifier: form.value.identifier,
      accountName: form.value.accountName,
      includeInNetWorth: form.value.includeInNetWorth,
      themeColor: form.value.bgColor,
    });
    
    uni.hideLoading();
    
    console.log('创建账户响应:', response);
    // 判断状态码是否为201（创建成功）
    if (response && response.status === 201) {
      uni.showToast({ title: '账户创建成功', icon: 'success' });
    
      // 获取上一个页面实例，通知其刷新数据
      setTimeout(() => {
        const pages = getCurrentPages();
        if (pages.length > 1) {
          const prevPage = pages[pages.length - 2];
          // 通知上一个页面刷新数据
          if (prevPage && typeof prevPage.refreshData === 'function') {
            prevPage.refreshData();
          }
        }
        uni.navigateBack();
      }, 3000);
    } else {
      // 状态码不是201，创建失败
      const errorMessage = response?.data?.message || response?.message || '未知错误';
      uni.showToast({ 
        title: '账户创建失败：' + errorMessage, 
        icon: 'none' 
      });
    }
  } catch (error) {
    uni.hideLoading();
    const errorMessage = error.response?.data?.message || error.message || '创建账户失败';
    uni.showToast({ title: errorMessage, icon: 'none' });
  }
};
</script>

<style lang="scss" scoped>
.add-account-container {
  min-height: 100vh; background-color: #F9F8F4; display: flex; flex-direction: column;
}

/* 🟢 实时预览区样式优化 */
.preview-hero {
  padding: 28rpx 32rpx; background-color: #F9F8F4;
  
  .live-card {
    width: 100%; height: 390rpx; border-radius: 52rpx; position: relative; overflow: hidden;
    box-shadow: 0 20rpx 40rpx rgba(0,0,0,0.12); transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
    
    .glass-texture {
      position: absolute; top: -50%; right: -20%; width: 120%; height: 120%;
      background: linear-gradient(135deg, rgba(255,255,255,0.18) 0%, rgba(255,255,255,0) 50%);
      border-radius: 50%; pointer-events: none;
    }
    
    .card-inner {
      padding: 44rpx; height: 100%; display: flex; flex-direction: column;
      position: relative; z-index: 2; box-sizing: border-box;
    }
  }
}

.header-section { margin-bottom: auto; }

.wc-row-top {
  display: flex; justify-content: space-between; align-items: center;
  .brand-group {
    display: flex; align-items: center; gap: 20rpx; flex: 1; min-width: 0;
    .logo-white-box {
      width: 64rpx; height: 64rpx; background: #fff; border-radius: 18rpx;
      display: flex; align-items: center; justify-content: center; flex-shrink: 0;
      .logo { width: 44rpx; height: 44rpx; }
      i { font-size: 32rpx; }
    }
    .name-area {
      display: flex; flex-direction: row; align-items: center; flex: 1; min-width: 0;
      .inst-name { font-size: 30rpx; font-weight: 800; line-height: 1.2; white-space: nowrap; flex-shrink: 0; }
      .acc-alias { font-size: 30rpx; opacity: 0.7; font-weight: 500; line-height: 1.2; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    }
  }
}

.wc-row-id {
  margin-top: 12rpx;
  .id-text { font-size: 24rpx; font-family: 'Courier New', monospace; opacity: 0.6; letter-spacing: 4rpx; font-weight: 600; }
}

.wc-row-balance {
  display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 24rpx;
  .balance-left {
    display: flex; align-items: baseline;
    .symbol { font-size: 40rpx; font-weight: 700; margin-right: 12rpx; }
    .num { font-size: 72rpx; font-weight: 800; font-family: 'DIN Alternate', sans-serif; letter-spacing: -1rpx; }
  }
}

.wc-row-footer {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 22rpx; font-weight: 600; border-top: 1rpx solid rgba(255,255,255,0.15);
  padding-top: 24rpx;
  .footer-info { opacity: 0.8; }
  .item-tag { background: rgba(0,0,0,0.1); padding: 6rpx 16rpx; border-radius: 10rpx; }
}

/* 表单与颜色选择器保持原样 */
.form-group { background: #fff; margin: 0 32rpx 32rpx; border-radius: 32rpx; padding: 0 32rpx; }
.form-item {
  min-height: 110rpx; display: flex; align-items: center; justify-content: space-between;
  border-bottom: 1rpx solid #F3F4F6;
  &:last-child { border-bottom: none; }
  .item-label { font-size: 28rpx; color: #1F2937; font-weight: 600; }
  .item-label-group { display: flex; flex-direction: column; .item-desc { font-size: 22rpx; color: #9CA3AF; margin-top: 4rpx; } }
  .item-input { flex: 1; text-align: right; font-size: 28rpx; color: #1F2937; margin-left: 40rpx; font-weight: 500; }
  .item-value { display: flex; align-items: center; gap: 8rpx; font-size: 28rpx; color: #1F2937; font-weight: 500; }
  .placeholder { color: #D1D5DB; font-weight: 400; }
}
.color-section {
  padding: 0 48rpx 40rpx;
  .section-title { font-size: 28rpx; font-weight: 700; color: #9CA3AF; text-transform: uppercase; margin-bottom: 24rpx; letter-spacing: 2rpx; }
  .color-grid { display: flex; flex-wrap: wrap; gap: 24rpx; }
  .color-dot-box {
    width: 80rpx; height: 80rpx; border-radius: 50%; padding: 0rpx;
    border: 4rpx solid transparent; transition: all 0.2s;
    &.active { border-color: #2D7A68; transform: scale(1.1); }
    .color-dot { width: 100%; height: 100%; border-radius: 50%; }
  }
}
.footer-action {
  padding: 32rpx 48rpx calc(48rpx + env(safe-area-inset-bottom));
  .save-btn {
    height: 110rpx; background: #2D7A68; color: #fff; border-radius: 30rpx;
    display: flex; align-items: center; justify-content: center;
    font-size: 32rpx; font-weight: 700; box-shadow: 0 20rpx 40rpx rgba(0,0,0,0.1);
    &:active { transform: scale(0.97); opacity: 0.9; }
  }
}
.bottom-spacer { height: 40rpx; }
</style>