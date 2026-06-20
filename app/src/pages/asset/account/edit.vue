<template>
  
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.edit-account-page {
  min-height: 100vh;
  background: $background;
}

.loading-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-text {
  font-size: 28rpx;
  color: $outline;
}

.scroll-content {
  height: calc(100vh - 160rpx);
}

.institution-info-card {
  padding: 32rpx;
}

.institution-header {
  display: flex;
  align-items: center;
  padding: $spacing-4;
  background: #FFFFFF;
  border-radius: $rounded-lg;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.inst-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: $rounded-half;
  background: $surface-container;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.inst-icon-img {
  width: 80rpx;
  height: 80rpx;
  border-radius: $rounded-default;
}

.inst-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.inst-label {
  font-size: 22rpx;
  font-weight: 700;
  color: $outline;
  letter-spacing: 0.8rpx;
}

.inst-name {
  margin-top: 4rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: $on-surface;
}

.readonly-tag {
  padding: 6rpx 16rpx;
  background: $surface-container-low;
  border-radius: $rounded-full;
}

.readonly-text {
  font-size: 22rpx;
  color: $outline;
  letter-spacing: 1rpx;
}

.account-type-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-4;
  margin-top: $spacing-3;
  background: #FFFFFF;
  border-radius: $rounded-lg;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.account-type-label {
  font-size: 26rpx;
  font-weight: 600;
  color: $outline;
}

.account-type-value {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.account-type-text {
  font-size: 28rpx;
  font-weight: 700;
  color: $on-surface;
}

.readonly-chip {
  font-size: 20rpx;
  color: $outline;
  background: $surface-container-low;
  padding: 4rpx 14rpx;
  border-radius: $rounded-full;
}

.form-section {
  background: #FFFFFF;
  border-radius: $spacing-4;
  margin: 0 32rpx;
  padding: 48rpx;
  box-shadow: $shadow-soft;
}

.form-item {
  margin-bottom: 40rpx;
}

.form-label {
  display: block;
  font-size: 24rpx;
  font-weight: 600;
  color: $outline;
  margin-bottom: 16rpx;
  padding-left: 8rpx;
  letter-spacing: 2rpx;
}

.required-mark {
  color: $error;
  margin-left: 4rpx;
}

.form-input {
  width: 100%;
  height: 84rpx;
  padding: 0 32rpx;
  background: $surface-container-low;
  border: none;
  border-radius: $rounded-full;
  font-size: 28rpx;
  color: $on-surface;
  box-sizing: border-box;
  outline: none;
  transition: box-shadow $transition-base;
  line-height: 84rpx;

  &:focus {
    box-shadow: inset 0 0 0 4rpx rgba($primary, 0.2);
  }

  &::placeholder {
    color: $outline-variant;
  }
}

.visibility-section {
  padding-top: 16rpx;
}

.visibility-options {
  display: flex;
  gap: 80rpx;
}

.visibility-option {
  display: flex;
  align-items: center;
  gap: 12rpx;
  cursor: pointer;
}

.radio-wrap {
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.custom-radio {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid $outline;
  border-radius: $rounded-half;
  position: relative;
  transition: all $transition-base;

  &.checked {
    border-color: $primary;
    background: $primary;

    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 12rpx;
      height: 12rpx;
      background: #FFFFFF;
      border-radius: $rounded-half;
    }
  }
}

.option-text {
  font-size: 24rpx;
  color: $on-surface;
}

.networth-section {
  margin-top: 32rpx;
  padding-top: 32rpx;
  border-top: 1rpx solid $surface-container-high;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.networth-info {
  flex: 1;
}

.networth-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $on-surface;
  display: block;
}

.networth-desc {
  margin-top: 4rpx;
  font-size: 24rpx;
  color: $outline;
  display: block;
}

.switch-wrap {
  position: relative;
  width: 88rpx;
  height: 48rpx;
  border-radius: $rounded-md;
  background: $outline-variant;
  cursor: pointer;
  transition: background $transition-base;

  &.checked {
    background: $primary;
  }

  .switch-thumb {
    position: absolute;
    top: 4rpx;
    left: 4rpx;
    width: 40rpx;
    height: 40rpx;
    border-radius: $rounded-half;
    background: #FFFFFF;
    transition: transform $transition-base;
  }

  &.checked .switch-thumb {
    transform: translateX(40rpx);
  }
}

.modules-section {
  padding: 48rpx 32rpx 64rpx;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 32rpx;
}

.section-icon {
  width: 40rpx;
  height: 40rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: $on-surface;
}

.module-group {
  margin-bottom: 40rpx;
}

.group-label {
  display: block;
  font-size: 22rpx;
  font-weight: 700;
  color: $outline;
  letter-spacing: 0.8rpx;
  text-transform: uppercase;
  margin-bottom: 20rpx;
  padding-left: 8rpx;
}

.module-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.module-card {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #FFFFFF;
  border-radius: $rounded-lg;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.02);
  cursor: pointer;
  transition: all $transition-base;

  &.required-card {
    background: $surface-container-low;
    border: 1rpx solid rgba($outline-variant, 0.3);
  }

  &.disabled {
    opacity: 0.7;
  }

  &:active {
    transform: scale(0.99);
  }
}

.module-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: $rounded-md;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;

  &.primary-bg {
    background: rgba($primary, 0.1);
  }

  &.secondary-bg {
    background: rgba($secondary, 0.1);
  }

  &.tertiary-bg {
    background: rgba($tertiary-container, 0.3);
  }

  &.default-bg {
    background: $surface-container;
  }
}

.module-icon {
  width: 40rpx;
  height: 40rpx;
}

.module-info {
  flex: 1;
}

.module-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.module-name {
  font-size: 28rpx;
  font-weight: 700;
  color: $on-surface;
}

.module-tag {
  padding: 4rpx 12rpx;
  border-radius: $rounded-sm;
  font-size: 20rpx;
  font-weight: 600;

  &.required-tag {
    background: rgba($outline-variant, 0.3);
    color: $outline;
  }

  &.default-tag {
    background: rgba($primary, 0.1);
    color: $primary;
  }

  &.optional-tag {
    background: $surface-container;
    color: $outline;
  }
}

.module-lock {
  width: 48rpx;
  height: 48rpx;
  opacity: 0.6;
}

.module-checkbox {
  width: 40rpx;
  height: 40rpx;
  border: 3rpx solid $outline-variant;
  border-radius: $rounded-half;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all $transition-base;

  &.checked {
    background: $primary;
    border-color: $primary;

    .check-icon {
      color: #FFFFFF;
      font-size: 24rpx;
      font-weight: 700;
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: $spacing-4 $spacing-4;
  background: $surface-container-lowest;
  z-index: 100;
}

.bottom-content {
  max-width: 720rpx;
  margin: 0 auto;
}

.confirm-btn {
  width: 100%;
  padding: 20rpx;
  background-color: $primary !important;
  border-radius: $rounded-full;
  border: none;
  display: flex;
  justify-content: center;
  align-items: center;

  &:active {
    transform: scale(0.98);
  }

  &:disabled {
    opacity: 0.6;
  }
}

.btn-text {
  font-size: 32rpx;
  font-weight: 700;
  color: $on-primary;
}

.divider-line {
  height: 1rpx;
  background: $surface-container-high;
  margin: 0 32rpx;
}

.delete-section {
  padding: 32rpx 32rpx 64rpx;
}

.delete-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  width: 100%;
  height: 88rpx;
  background: #FFFFFF;
  border: 1rpx solid rgba($error, 0.3);
  border-radius: $rounded-full;
  transition: all $transition-base;

  &:active {
    background: rgba($error, 0.05);
  }

  &:disabled {
    opacity: 0.5;
  }
}

.delete-text {
  font-size: 28rpx;
  font-weight: 600;
  color: $error;
}
</style>