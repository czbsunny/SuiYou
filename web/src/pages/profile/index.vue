<template>
  <!-- Profile Page -->
  <view class="profile-page">
    <view class="content-wrapper animate-fade-in">
        <!-- 用户卡片 -->
        <view 
            @click="handleUserCardClick"
            class="user-card"
        >
           <view class="avatar-box">
               <text class="avatar-text">{{ user?.name?.[0] || '未' }}</text>
           </view>
           <view class="user-info">
               <text class="user-name">{{ user?.name || '点击登录' }}</text>
               <view class="user-phone-row">
                   <image src="/static/images/user.png" class="icon-sm" />
                   <text class="phone-text">{{ user?.phone || user?.phoneNumber || '登录同步资产数据' }}</text>
               </view>
           </view>
           <view v-if="!user" class="arrow-box">
                <image src="/static/images/chevron-right.png" class="icon-md" />
           </view>
        </view>

        <!-- AI 测评入口 -->
        <view 
            @click="goToAssessment"
            class="assessment-card"
        >
            <view class="card-left">
                <view class="icon-circle">
                    <image src="/static/images/trending-up-blue.png" class="icon-md" />
                </view>
                <view class="card-text">
                    <text class="card-title">理财知识测评</text>
                    <text class="card-desc">基于 AI 生成测评</text>
                </view>
            </view>
            <view class="arrow-box">
                <image src="/static/images/chevron-right.png" class="icon-md" />
            </view>
        </view>

        <!-- 菜单列表 -->
        <view class="menu-group">
            <view v-for="(item, idx) in menuItems" :key="idx" class="menu-item">
                <view class="menu-left">
                    <view class="menu-icon-box">
                        <image :src="item.icon" class="icon-md" />
                    </view>
                    <text class="menu-label">{{ item.label }}</text>
                </view>
                <view class="menu-right">
                    <text v-if="item.badge && user" class="badge">{{ item.badge }}</text>
                    <image src="/static/images/chevron-right.png" class="icon-sm opacity-50" />
                </view>
            </view>
        </view>

        <!-- 退出按钮 -->
        <button 
          v-if="user"
          @click="handleLogout"
          class="logout-btn"
        >
            退出登录
        </button>
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

const menuItems = [
    { icon: "/static/images/message.png", label: "消息通知", badge: 2 },
    { icon: "/static/images/settings-gray.png", label: "设置" },
    { icon: "/static/images/info-gray.png", label: "关于我们" },
];

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

const handleUserCardClick = () => {
    if (!user.value) {
        goToLogin();
    }
};

const goToLogin = () => {
    uni.navigateTo({ url: '/pages/auth/login' });
};

const goToAssessment = () => {
    uni.showToast({ title: '测评页面准备中，敬请期待', icon: 'none' });
};
</script>

<style lang="scss" scoped>

.profile-page {
  min-height: 100vh;
  background-color: $bg-page;
  padding-top: 24px; /* pt-6 */
  /* padding-bottom: 32 + safe-area */
  padding-bottom: calc(128px + env(safe-area-inset-bottom));
  padding-left: 16px; /* px-4 */
  padding-right: 16px;
  box-sizing: border-box;
}

.content-wrapper {
  max-width: 448px; /* max-w-md */
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px; /* space-y-4 */
}

/* 动画 */
.animate-fade-in {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 通用图标大小 */
.icon-sm { width: 12px; height: 12px; }
.icon-md { width: 20px; height: 20px; }
.opacity-50 { opacity: 0.5; }

/* 用户卡片 */
.user-card {
  background-color: $bg-white;
  padding: 24px; /* p-6 */
  border-radius: 24px; /* rounded-3xl */
  box-shadow: 0 1px 2px rgba(0,0,0,0.05); /* shadow-sm */
  border: 1px solid $border-color;
  display: flex;
  flex-direction: row;
  align-items: center;
  transition: transform 0.1s;

  &:active {
    transform: scale(0.98);
  }
}

.avatar-box {
  width: 64px; /* w-16 */
  height: 64px;
  border-radius: 50%;
  background-color: rgba(42, 128, 108, 0.1); /* bg-primary/10 */
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid $bg-white;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  margin-right: 16px; /* space-x-4 */
}

.avatar-text {
  font-size: 24px; /* text-2xl */
  font-weight: bold;
  color: $primary;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 20px; /* text-xl */
  font-weight: bold;
  color: $text-main;
  display: block;
}

.user-phone-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-top: 2px; /* mt-0.5 */
}

.phone-text {
  font-size: 14px; /* text-sm */
  color: $text-muted;
  margin-left: 4px; /* mr-1 */
}

.arrow-box {
  color: $text-placeholder;
}

/* 测评卡片 */
.assessment-card {
  background-color: $bg-white;
  padding: 24px; /* p-6 */
  border-radius: 24px; /* rounded-3xl */
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  border: 1px solid $border-color;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  transition: transform 0.1s;

  &:active {
    transform: scale(0.98);
  }
}

.card-left {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.icon-circle {
  width: 40px; /* w-10 */
  height: 40px;
  border-radius: 50%;
  background-color: rgba(42, 128, 108, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px; /* mr-4 */
  color: $primary;
}

.card-title {
  font-size: 16px; /* text-base */
  font-weight: bold;
  color: $text-main;
  display: block;
}

.card-desc {
  font-size: 12px; /* text-xs */
  color: $text-muted;
  margin-top: 4px; /* mt-1 */
  display: block;
}

/* 菜单列表 */
.menu-group {
  background-color: $bg-white;
  border-radius: 24px; /* rounded-3xl */
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  border: 1px solid $border-color;
  overflow: hidden;
}

.menu-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 20px; /* p-5 */
  border-bottom: 1px solid $bg-subtle;
  transition: background-color 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: $bg-subtle;
  }
}

.menu-left {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.menu-icon-box {
  width: 32px; /* w-8 */
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-muted; /* text-gray-400 */
}

.menu-label {
  font-size: 14px; /* text-sm */
  font-weight: 500;
  color: $text-sub; /* text-gray-700 */
  margin-left: 12px; /* ml-3 */
}

.menu-right {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.badge {
  background-color: $red;
  color: $text-inverse;
  font-size: 10px; /* text-[10px] */
  padding: 2px 6px; /* px-1.5 py-0.5 */
  border-radius: 9999px;
  font-weight: bold;
  margin-right: 8px; /* mr-2 */
}

/* 退出按钮 */
.logout-btn {
  width: 100%;
  background-color: $bg-white;
  color: $red;
  font-weight: bold;
  padding: 16px 0; /* py-4 */
  border-radius: 24px; /* rounded-3xl */
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  border: 1px solid $border-color;
  transition: background-color 0.2s;

  &:active {
    background-color: $bg-gain-light;
  }
  
  /* UniApp button reset */
  &::after {
    border: none;
  }
}
</style>