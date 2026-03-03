<template>
  <view class="profile-container">
    <view class="content-wrapper animate-fade-in">
      
      <!-- 用户卡片 -->
      <view 
        class="user-card" 
        hover-class="item-active"
        @click="handleUserCardClick"
      >
        <view class="avatar-box">
          <text class="avatar-text">{{ user?.username ? user.username[0].toUpperCase() : '👤' }}</text>
        </view>
        
        <view class="user-info">
          <view class="user-name">{{ user?.username || '点击登录' }}</view>
          <view class="user-desc">{{ user?.email || user?.phone || '每一笔个人资产都值得被珍视' }}</view>
        </view>
        
        <view class="arrow-box">
          <image src="/static/images/arrow-right.png" class="arrow-icon" />
        </view>
      </view>

      <!-- 功能列表占位（如果你后续想加，样式已预设） -->
      <view class="menu-section" v-if="user">
         <!-- 未来可以放家庭成员管理、目标管理等 -->
      </view>

      <!-- 退出按钮 -->
      <view v-if="user" class="logout-section">
        <button 
          class="logout-btn" 
          hover-class="btn-hover-grey"
          @click="handleLogout"
        >
          退出当前账号
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { clearToken } from '../../services/apiService';

const user = ref(null);

onShow(() => {
    const uStr = uni.getStorageSync('userInfo');
    if (uStr) {
        try {
            user.value = JSON.parse(uStr);
        } catch (e) { console.error(e); }
    } else {
        user.value = null;
    }
});

const handleUserCardClick = () => {
    if (!user.value) {
        goToLogin();
    }
};

const goToLogin = () => {
    uni.navigateTo({ url: '/pages/auth/login' });
};

const handleLogout = () => {
    uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
            if (res.confirm) {
                user.value = null;
                uni.removeStorageSync('userInfo');
                clearToken();
                uni.showToast({ title: '已退出', icon: 'none' });
            }
        }
    });
};
</script>

<style lang="scss" scoped>
.profile-container {
  min-height: 100vh;
  background-color: $bg-page; // 应用 #FAF9F6
  padding: 48rpx 32rpx;
  box-sizing: border-box;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

/* 用户卡片：通过纯白与米白的微妙差异建立层级 */
.user-card {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 56rpx 40rpx;
  background-color: $bg-white;
  border-radius: $radius-lg;
  box-shadow: $shadow-card; // 极淡的暖咖色阴影
  border: 1rpx solid rgba(42, 128, 108, 0.03); // 极细的墨绿描边
  transition: all 0.25s ease;

  &:active {
    transform: scale(0.99);
    background-color: #fcfcfc;
  }
}

.avatar-box {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  background-color: #f1f4f2; // 极浅的绿灰，比主背景稍深
  border: 4rpx solid $bg-white;
  @include flex-center;
  margin-right: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(42, 128, 108, 0.1);
}

.avatar-text {
  font-size: 56rpx;
  font-weight: bold;
  color: $primary; // 墨绿色
  font-family: $font-family-money;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 40rpx;
  font-weight: 600;
  color: $text-main; // 深咖黑
  margin-bottom: 12rpx;
}

.user-desc {
  font-size: 24rpx;
  color: $text-sub;
  letter-spacing: 0.5rpx;
}

.arrow-box {
  color: $text-muted;
  .arrow-icon {
    width: 32rpx;
    height: 32rpx;

    opacity: 0.3;
  }
}

/* 退出按钮：保持纯净 */
.logout-section {
  margin-top: 24rpx;
}

.logout-btn {
  width: 100%;
  height: 100rpx;
  @include flex-center;
  background-color: $bg-white;
  color: #d32f2f; // 使用稍微稳重的深红
  font-weight: 500;
  font-size: 30rpx;
  border-radius: $radius-base;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.01);

  &::after { border: none; }
}

.btn-hover-grey {
  background-color: #f8f8f8 !important;
}

.animate-fade-in {
  animation: fadeIn 0.6s cubic-bezier(0.25, 1, 0.5, 1);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20rpx); }
  to { opacity: 1; transform: translateY(0); }
}
</style>