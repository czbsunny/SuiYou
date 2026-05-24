<template>
  <view class="button" :class="[`button--${type}`, { 'button--disabled': disabled }]" @tap="handleClick">
    <slot></slot>
  </view>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'primary', 'success', 'warning', 'error'].includes(value)
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  border-radius: $rounded-full;
  font-size: 32rpx;

  &--default {
    background: $surface-container-lowest;
    color: $text-color;
    border: 1px solid $border-color;
  }

  &--primary {
    background: $primary;
    color: #ffffff;
    border: none;
  }

  &--success {
    background: $success-color;
    color: #ffffff;
    border: none;
  }

  &--warning {
    background: $warning-color;
    color: #ffffff;
    border: none;
  }

  &--error {
    background: $error-color;
    color: #ffffff;
    border: none;
  }

  &--disabled {
    background: $text-color-disabled;
    color: $text-color-placeholder;
    border: none;
  }

  &:active:not(.button--disabled) {
    opacity: 0.8;
  }
}
</style>
