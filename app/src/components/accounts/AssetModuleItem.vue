
&lt;template&gt;
  &lt;view class="asset-module-item" :class="{ 'required-module': isRequired, 'disabled-module': disabled }"&gt;
    &lt;view class="module-content"&gt;
      &lt;view class="module-icon" :class="iconClass"&gt;
        &lt;text class="icon-letter"&gt;{{ iconLetter }}&lt;/text&gt;
      &lt;/view&gt;
      &lt;view class="module-info"&gt;
        &lt;view class="name-row"&gt;
          &lt;text class="module-name"&gt;{{ module.name || module.categoryCode }}&lt;/text&gt;
          &lt;view v-if="isRequired" class="required-tag"&gt;必选&lt;/view&gt;
          &lt;view v-else-if="isDefaultSelected" class="default-tag"&gt;默认&lt;/view&gt;
          &lt;view v-else class="optional-tag"&gt;可选&lt;/view&gt;
        &lt;/view&gt;
        &lt;text class="module-desc"&gt;{{ module.description || '添加账户的资产模块' }}&lt;/text&gt;
      &lt;/view&gt;
    &lt;/view&gt;
    &lt;view v-if="!isRequired" class="module-checkbox"&gt;
      &lt;checkbox :checked="checked" @tap="handleToggle" :disabled="disabled" color="#006754" /&gt;
    &lt;/view&gt;
    &lt;view v-else class="lock-icon"&gt;
      &lt;text class="lock-symbol"&gt;🔒&lt;/text&gt;
    &lt;/view&gt;
  &lt;/view&gt;
&lt;/template&gt;

&lt;script setup&gt;
import { computed } from 'vue'

const props = defineProps({
  module: {
    type: Object,
    required: true
  },
  selectionType: {
    type: String,
    default: 'OPTIONAL'
  },
  checked: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle'])

const isRequired = computed(() =&gt; props.selectionType === 'REQUIRED')
const isDefaultSelected = computed(() =&gt; props.selectionType === 'DEFAULT_SELECTED')

const iconLetter = computed(() =&gt; {
  const name = props.module.name || props.module.categoryCode || ''
  return name.charAt(0)
})

const iconClass = computed(() =&gt; {
  if (isRequired.value) {
    return 'required-icon'
  } else if (isDefaultSelected.value) {
    return 'default-icon'
  }
  return 'optional-icon'
})

const handleToggle = () =&gt; {
  if (isRequired.value || props.disabled) {
    return
  }
  emit('toggle', !props.checked)
}
&lt;/script&gt;

&lt;style lang="scss" scoped&gt;
@import '@/styles/variables.scss';

.asset-module-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
}

.required-module {
  background: rgba(107, 114, 128, 0.05);
  border: 1rpx solid $outline-variant;
}

.disabled-module {
  opacity: 0.6;
}

.module-content {
  display: flex;
  align-items: center;
  gap: 28rpx;
  flex: 1;
}

.module-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.required-icon {
  background: rgba(0, 103, 84, 0.1);
}

.default-icon {
  background: rgba(112, 86, 36, 0.1);
}

.optional-icon {
  background: $surface-container;
}

.icon-letter {
  font-size: 32rpx;
  font-weight: 700;
  color: $on-surface;
}

.module-info {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.module-name {
  font-size: 30rpx;
  font-weight: 700;
  color: $on-surface;
}

.required-tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: rgba(107, 114, 128, 0.2);
  color: $outline;
  font-size: 20rpx;
  font-weight: 700;
}

.default-tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: rgba(0, 103, 84, 0.1);
  color: $primary;
  font-size: 20rpx;
  font-weight: 700;
}

.optional-tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: $surface-container;
  color: $outline;
  font-size: 20rpx;
  font-weight: 500;
}

.module-desc {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: $outline;
  line-height: 1.5;
}

.module-checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
}

.lock-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.lock-symbol {
  font-size: 32rpx;
  color: $outline-variant;
}
&lt;/style&gt;

