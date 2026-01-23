<template>
  <view class="add-account-container">
    <!-- 1. 实时预览区：卡片工坊 -->
    <view class="preview-hero">
      <view 
        class="live-card" 
        :style="{ background: cardBackground, color: contrastTextColor }"
      >
        <!-- 玻璃光影纹理 -->
        <view class="glass-texture"></view>
        
        <view class="card-inner">
          <view class="card-top">
            <view class="brand-box">
              <view class="logo-white-box">
                <image v-if="form.logoUrl" :src="form.logoUrl" mode="aspectFit" class="card-logo" />
                <i v-else class="fa-solid fa-building-columns" :style="{ color: form.bgColor }"></i>
              </view>
              <text class="inst-name">{{ form.instName || '选择金融机构' }}</text>
            </view>
            <view v-if="form.identifier" class="id-badge">**** {{ form.identifier }}</view>
          </view>
          
          <view class="card-mid">
            <text class="alias-label">账户别名</text>
            <text class="alias-name">{{ form.accountName || '未命名账户' }}</text>
          </view>
          
          <view class="card-bottom">
            <text class="status-text">数字化资产账户</text>
            <view class="safe-icon"><i class="fa-solid fa-shield-check"></i></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 配置表单区 -->
    <scroll-view scroll-y class="form-scroll">
      <view class="form-group">
        <!-- 机构选择 -->
        <view class="form-item" @tap="goToSelectInstitution">
          <text class="item-label">所属机构</text>
          <view class="item-value">
            <text :class="{ 'placeholder': !form.instName }">{{ form.instName || '点击选择' }}</text>
            <uni-icons type="chevron-right" size="14" color="#ccc"></uni-icons>
          </view>
        </view>

        <!-- 账户别名 -->
        <view class="form-item">
          <text class="item-label">账户别名</text>
          <input 
            v-model="form.accountName" 
            placeholder="如：工资卡 / 常用微信" 
            class="item-input" 
            placeholder-class="placeholder"
          />
        </view>

        <!-- 识别码 -->
        <view class="form-item">
          <view class="item-label-group">
            <text class="item-label">末四位</text>
            <text class="item-desc">用于快速区分同机构账户</text>
          </view>
          <input 
            v-model="form.identifier" 
            type="number" 
            maxlength="4" 
            placeholder="选填" 
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
            <text class="item-desc">开启后该账户金额将计入净值</text>
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
import { useConfigStore } from '@/stores/config.js';

const configStore = useConfigStore();

const form = ref({
  instCode: '',
  instName: '',
  logoUrl: '',
  accountName: '',
  identifier: '',
  bgColor: '#1F2937', // 默认深炭灰
  includeInNetWorth: true
});

// 推荐的高级配色方案
const presetColors = [
  '#1F2937', '#2D7A68', '#E72D2D', '#003B8F', '#1677FF', 
  '#07C160', '#5F27CD', '#D97706', '#FA8231', '#0FB9B1'
];

// 计算属性：生成渐变背景
const cardBackground = computed(() => {
  return `linear-gradient(135deg, ${form.value.bgColor} 0%, ${adjustColor(form.value.bgColor, -15)} 100%)`;
});

// 🟢 核心算法：自适应文字颜色（亮背景黑字，暗背景白字）
const contrastTextColor = computed(() => {
  const hex = form.value.bgColor.replace('#', '');
  const r = parseInt(hex.substr(0, 2), 16);
  const g = parseInt(hex.substr(2, 2), 16);
  const b = parseInt(hex.substr(4, 2), 16);
  const brightness = (r * 299 + g * 587 + b * 114) / 1000;
  return brightness > 185 ? '#1C1C1E' : '#FFFFFF';
});

onLoad((options) => {
  // 监听机构选择回传
  uni.$on('institutionSelected', (inst) => {
    form.value.instCode = inst.instCode;
    form.value.instName = inst.instName;
    form.value.logoUrl = inst.logoUrl;
    
    // 智能联动：如果该机构有品牌色，自动染色
    if (inst.brandColor) {
      form.value.bgColor = inst.brandColor;
      uni.vibrateShort(); // 触感反馈
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

  uni.showLoading({ title: '正在创建...' });
  
  // 这里对接你的后端 API
  console.log('提交账户数据:', form.value);
  
  setTimeout(() => {
    uni.hideLoading();
    uni.showToast({ title: '账户已就绪' });
    setTimeout(() => uni.navigateBack(), 1500);
  }, 800);
};

// 辅助工具：颜色深浅微调
function adjustColor(hex, amt) {
  return hex; // 简化版，直接返回原色。实际可接入颜色加深函数。
}
</script>

<style lang="scss" scoped>
.add-account-container {
  min-height: 100vh;
  background-color: #F9F8F4;
  display: flex;
  flex-direction: column;
}

/* 顶部预览区 */
.preview-hero {
  padding: 40rpx 48rpx;
  background-color: #F9F8F4;
  
  .live-card {
    width: 100%;
    height: 380rpx;
    border-radius: 48rpx;
    position: relative;
    overflow: hidden;
    box-shadow: 0 20rpx 40rpx rgba(0,0,0,0.1);
    transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
    
    .glass-texture {
      position: absolute; top: -50%; right: -20%; width: 120%; height: 120%;
      background: linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0) 50%);
      border-radius: 50%; pointer-events: none;
    }
    
    .card-inner {
      padding: 44rpx;
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      position: relative;
      z-index: 2;
      box-sizing: border-box;
    }
  }
}

.brand-box {
  display: flex; align-items: center; gap: 20rpx;
  .logo-white-box {
    width: 64rpx; height: 64rpx; background: #fff; border-radius: 16rpx;
    display: flex; align-items: center; justify-content: center;
    .card-logo { width: 44rpx; height: 44rpx; }
    i { font-size: 32rpx; }
  }
  .inst-name { font-size: 32rpx; font-weight: 700; }
}

.id-badge {
  font-size: 20rpx; background: rgba(0,0,0,0.1); padding: 4rpx 16rpx;
  border-radius: 100rpx; border: 1rpx solid rgba(255,255,255,0.2); font-family: monospace;
}

.card-mid {
  .alias-label { font-size: 20rpx; opacity: 0.7; display: block; margin-bottom: 4rpx; font-weight: 500; }
  .alias-name { font-size: 44rpx; font-weight: 800; }
}

.card-bottom {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 22rpx; opacity: 0.8; font-weight: 500;
}

/* 表单区 */
.form-group {
  background: #fff; margin: 0 32rpx 32rpx; border-radius: 32rpx;
  padding: 0 32rpx;
}

.form-item {
  min-height: 110rpx; display: flex; align-items: center; justify-content: space-between;
  border-bottom: 1rpx solid #F3F4F6;
  &:last-child { border-bottom: none; }
  
  .item-label { font-size: 28rpx; color: #1F2937; font-weight: 600; }
  .item-label-group {
    display: flex; flex-direction: column;
    .item-desc { font-size: 22rpx; color: #9CA3AF; margin-top: 4rpx; }
  }
  .item-input { flex: 1; text-align: right; font-size: 28rpx; color: #1F2937; margin-left: 40rpx; font-weight: 500; }
  .item-value { display: flex; align-items: center; gap: 8rpx; font-size: 28rpx; color: #1F2937; font-weight: 500; }
  .placeholder { color: #D1D5DB; font-weight: 400; }
}

/* 颜色选择器 */
.color-section {
  padding: 0 48rpx 40rpx;
  .section-title { font-size: 24rpx; font-weight: 700; color: #9CA3AF; text-transform: uppercase; margin-bottom: 24rpx; letter-spacing: 2rpx; }
  .color-grid { display: flex; flex-wrap: wrap; gap: 24rpx; }
  .color-dot-box {
    width: 64rpx; height: 64rpx; border-radius: 50%; padding: 4rpx;
    border: 4rpx solid transparent; transition: all 0.2s;
    &.active { border-color: #2D7A68; transform: scale(1.1); }
    .color-dot { width: 100%; height: 100%; border-radius: 50%; }
  }
}

.footer-action {
  padding: 32rpx 48rpx calc(48rpx + env(safe-area-inset-bottom));
  .save-btn {
    height: 110rpx; background: #1F2937; color: #fff; border-radius: 30rpx;
    display: flex; align-items: center; justify-content: center;
    font-size: 32rpx; font-weight: 700; box-shadow: 0 20rpx 40rpx rgba(0,0,0,0.1);
    &:active { transform: scale(0.97); opacity: 0.9; }
  }
}

.bottom-spacer { height: 40rpx; }
</style>