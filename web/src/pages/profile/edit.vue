<template>
  <view class="edit-profile-container">
    <!-- 顶部装饰区（可选，增加品牌感） -->
    <view class="header-bg"></view>

    <view class="form-content">
      <!-- 头像预览区 -->
      <view class="avatar-section">
        <view class="avatar-wrapper" @click="chooseAvatar" hover-class="hover-opacity">
          <view class="avatar-preview">
            <text class="avatar-text">{{ userInfo.username ? Array.from(userInfo.username)[0].toUpperCase() : '' }}</text>
            <text v-if="!userInfo.username" class="avatar-icon">👤</text>
          </view>
          <!-- 编辑小标签 -->
          <view class="edit-badge">
            <text class="iconfont">✎</text>
          </view>
        </view>
      </view>
      
      <!-- 表单卡片 -->
      <view class="form-card">
        <view class="form-field">
          <view class="field-label">昵称</view>
          <input 
            type="text" 
            v-model="formData.username" 
            placeholder="请输入昵称" 
            placeholder-class="placeholder-style"
            class="field-input"
            maxlength="12"
          />
        </view>
      </view>
      
      <!-- 提示文字 -->
      <view class="form-tip">
        好名字能让岁有长赢更好地记住您
      </view>
      
      <!-- 保存按钮 -->
      <view class="button-box">
        <button 
          :disabled="isLoading" 
          @tap="saveChanges" 
          class="save-button"
          :class="{ 'btn-disabled': isLoading }"
          hover-class="button-hover"
        >
          <text v-if="isLoading" class="loading-icon">⏳</text>
          {{ isLoading ? '正在保存...' : '保存修改' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import apiService from '../../services/apiService.js';

export default {
  data() {
    return {
      userInfo: {},
      formData: {
        username: ''
      },
      isLoading: false
    }
  },
  onLoad() {
    this.loadUserInfo();
  },
  methods: {
    loadUserInfo() {
      this.userInfo = uni.getStorageSync('userInfo') || {};
      this.formData = {
        username: this.userInfo.username || ''
      };
    },
    goBack() {
      uni.navigateBack();
    },
    async chooseAvatar() {
      try {
        // 调用微信授权获取用户信息
        const userInfoRes = await uni.getUserProfile({
          desc: '用于完善会员资料'
        })
        
        if (userInfoRes.userInfo) {
          // 将微信用户信息发送到后端更新
          const response = await apiService.put('/api/auth/me', {
            nickname: userInfoRes.userInfo.nickName,
            avatar: userInfoRes.userInfo.avatarUrl
          })
          
          // 更新本地存储的用户信息
          const updatedUserInfo = { ...this.userInfo, ...response.data }
          uni.setStorageSync('userInfo', updatedUserInfo)
          
          // 更新页面数据
          this.userInfo = updatedUserInfo
          this.formData.username = updatedUserInfo.username || updatedUserInfo.nickname || ''
          
          uni.showToast({ title: '头像更新成功', icon: 'success' })
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        if (error.errMsg && error.errMsg.includes('cancel')) {
          // 用户取消授权，不提示
        } else {
          uni.showToast({ title: '获取用户信息失败', icon: 'none' })
        }
      }
    },
    async saveChanges() {
      if (!this.formData.username.trim()) {
        uni.showToast({ title: '请输入昵称', icon: 'none' });
        return;
      }
      
      try {
        this.isLoading = true;
        const response = await apiService.put('/api/auth/me', {
          username: this.formData.username
        });
        
        const updatedUserInfo = { ...this.userInfo, username: response.data.username };
        uni.setStorageSync('userInfo', updatedUserInfo);
        
        uni.showToast({ title: '修改成功', icon: 'success' });
        setTimeout(() => { this.goBack(); }, 1000);
      } catch (error) {
        uni.showToast({ title: '保存失败，请重试', icon: 'none' });
      } finally {
        this.isLoading = false;
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.edit-profile-container {
  min-height: 100vh;
  background-color: $bg-page;
  position: relative;
}

/* 顶部品牌色氛围 */
.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200rpx;
  background: linear-gradient(180deg, $primary-alpha-10 0%, $bg-page 100%);
  z-index: 0;
}

.form-content {
  position: relative;
  z-index: 1;
  padding: 60rpx $spacing-md;
}

/* 头像区域优化 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;
}

.avatar-wrapper {
  position: relative;
  margin-bottom: $spacing-sm;
}

.avatar-preview {
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, $primary-dark);
  @include flex-center;
  box-shadow: 0 8rpx 20rpx rgba(255, 143, 31, 0.2);
  border: 6rpx solid $bg-white;
}

.avatar-text {
  font-size: 80rpx;
  color: $text-inverse;
  font-family: $font-family-money;
  font-weight: $fw-semibold;
}

.edit-badge {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 50rpx;
  height: 50rpx;
  background-color: $bg-white;
  border-radius: 50%;
  @include flex-center;
  box-shadow: $shadow-card;
  color: $primary;
  font-size: 24rpx;
}

.avatar-tip {
  font-size: 24rpx;
  color: $text-placeholder;
}

/* 表单卡片化 */
.form-card {
  background-color: $bg-white;
  border-radius: $radius-lg;
  padding: 0 $spacing-md;
  box-shadow: $shadow-card;
}

.form-field {
  display: flex;
  align-items: center;
  padding: 40rpx 0;
  // 如果有多个字段，这里可以使用 border-bottom: 1rpx solid $border-color;
}

.field-label {
  width: 140rpx;
  font-size: 30rpx;
  color: $text-main;
  font-weight: $fw-medium;
}

.field-input {
  flex: 1;
  font-size: 30rpx;
  color: $text-main;
  @include tabular-nums;
}

.placeholder-style {
  color: $text-placeholder;
  font-size: 28rpx;
}

.form-tip {
  padding: $spacing-base $spacing-sm;
  font-size: 24rpx;
  color: $text-placeholder;
}

/* 保存按钮重构 */
.button-box {
  margin-top: 80rpx;
}

.save-button {
  width: 100%;
  height: 100rpx;
  border-radius: $radius-base;
  background: linear-gradient(to right, $primary, $primary-dark);
  color: $text-inverse;
  font-size: 32rpx;
  font-weight: $fw-semibold;
  @include flex-center;
  border: none;
  box-shadow: $shadow-button;
  transition: all 0.3s ease;

  &::after { border: none; }
}

.button-hover {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 8rpx rgba(255, 143, 31, 0.2);
  filter: brightness(1.05);
}

.btn-disabled {
  background: $btn-disabled-bg !important;
  color: $btn-disabled-text !important;
  box-shadow: none !important;
}

.loading-icon {
  margin-right: 12rpx;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.hover-opacity {
  opacity: 0.8;
}
</style>
