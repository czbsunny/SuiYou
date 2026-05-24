<template>
  <view class="family-member" @tap="handleTap">
    <view class="member-avatar" :class="{ primary: member.isPrimary }">
      <image 
        :src="member.avatar || 'https://neeko-copilot.bytedance.net/api/text_to_image?prompt=professional%20portrait&image_size=square'" 
        mode="aspectFill"
        class="avatar-img"
      />
    </view>
    <span class="member-name">{{ member.name }}</span>
  </view>
</template>

<script setup>
const props = defineProps({
  member: {
    type: Object,
    required: true
  }
})

const handleTap = () => {
  uni.showToast({ title: props.member.name, icon: 'none' })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.family-member {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $stack-gap-sm;
  
  .member-avatar {
    width: 112rpx;
    height: 112rpx;
    border-radius: $rounded-full;
    border: 4rpx solid $surface-container-high;
    overflow: hidden;
    
    &.primary {
      border-color: $primary;
    }
    
    .avatar-img {
      width: 100%;
      height: 100%;
    }
  }
  
  .member-name {
    font-family: $font-family-primary;
    font-size: $font-size-label-caps;
    font-weight: $font-weight-bold;
    letter-spacing: 0.05em;
    color: $on-surface-variant;
    text-transform: uppercase;
  }
}
</style>