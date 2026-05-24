<template>
  <view v-if="shouldShow">
    <slot />
  </view>
  <view v-else-if="$slots.fallback">
    <slot name="fallback" />
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { usePlatform } from '@/utils/platform'

const props = defineProps({
  platforms: {
    type: Array,
    default: () => ['wechat', 'douyin', 'android', 'ios', 'h5']
  }
})

const platform = usePlatform()

const shouldShow = computed(() => {
  if (props.platforms.includes('wechat') && platform.isWechat) return true
  if (props.platforms.includes('douyin') && platform.isDouyin) return true
  if (props.platforms.includes('android') && platform.isAndroid) return true
  if (props.platforms.includes('ios') && platform.isIOS) return true
  if (props.platforms.includes('h5') && platform.isH5) return true
  return false
})
</script>

<style lang="scss" scoped>
</style>
