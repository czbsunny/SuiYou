
&lt;template&gt;
  &lt;view class="institution-card" @click="handleClick"&gt;
    &lt;view class="card-content"&gt;
      &lt;view class="institution-icon" :class="iconClass"&gt;
        &lt;text class="icon-text"&gt;{{ iconLetter }}&lt;/text&gt;
      &lt;/view&gt;
      &lt;view class="institution-info"&gt;
        &lt;view class="name-row"&gt;
          &lt;text class="institution-name"&gt;{{ institution.instName || institution.shortName }}&lt;/text&gt;
          &lt;view v-if="institution.isHot" class="hot-tag"&gt;热门&lt;/view&gt;
        &lt;/view&gt;
        &lt;view v-if="institutionType" class="type-tag"&gt;
          {{ institutionType.typeName }}
        &lt;/view&gt;
      &lt;/view&gt;
    &lt;/view&gt;
    &lt;view class="arrow"&gt;
      &lt;text class="arrow-icon"&gt;›&lt;/text&gt;
    &lt;/view&gt;
  &lt;/view&gt;
&lt;/template&gt;

&lt;script setup&gt;
import { computed } from 'vue'

const props = defineProps({
  institution: {
    type: Object,
    required: true
  },
  institutionType: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['click'])

const iconLetter = computed(() =&gt; {
  const name = props.institution.instName || props.institution.shortName || ''
  return name.charAt(0)
})

const iconClass = computed(() =&gt; {
  const typeCode = props.institution.instType || ''
  const typeClasses = {
    'BANK': 'bank-icon',
    'INTERNET_BANK': 'internet-bank-icon',
    'FOREIGN_BANK': 'foreign-bank-icon',
    'SECURITY': 'security-icon',
    'FUND_PLATFORM': 'fund-icon',
    'INSURANCE': 'insurance-icon',
    'PAYMENT': 'payment-icon',
    'FINTECH': 'fintech-icon'
  }
  return typeClasses[typeCode] || 'default-icon'
})

const handleClick = () =&gt; {
  emit('click', props.institution)
}
&lt;/script&gt;

&lt;style lang="scss" scoped&gt;
@import '@/styles/variables.scss';

.institution-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
  transition: background 0.2s;
  
  &amp;:active {
    background: $surface-container-low;
  }
}

.card-content {
  display: flex;
  align-items: center;
  gap: 28rpx;
  flex: 1;
}

.institution-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bank-icon {
  background: rgba(183, 16, 42, 0.08);
}

.internet-bank-icon {
  background: rgba(35, 102, 232, 0.08);
}

.foreign-bank-icon {
  background: rgba(100, 116, 139, 0.08);
}

.security-icon {
  background: rgba(250, 166, 26, 0.08);
}

.fund-icon {
  background: rgba(79, 70, 229, 0.08);
}

.insurance-icon {
  background: rgba(6, 182, 212, 0.08);
}

.payment-icon {
  background: rgba(0, 103, 84, 0.08);
}

.fintech-icon {
  background: rgba(139, 92, 246, 0.08);
}

.default-icon {
  background: $surface-container;
}

.icon-text {
  font-size: 36rpx;
  font-weight: 700;
  color: $on-surface;
}

.institution-info {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.institution-name {
  font-size: 30rpx;
  font-weight: 700;
  color: $on-surface;
}

.hot-tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: $error-container;
  color: $error;
  font-size: 20rpx;
  font-weight: 700;
}

.type-tag {
  margin-top: 8rpx;
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
  background: $surface-container;
  color: $on-surface-variant;
  font-size: 20rpx;
  font-weight: 500;
  display: inline-block;
}

.arrow {
  display: flex;
  align-items: center;
}

.arrow-icon {
  font-size: 48rpx;
  font-weight: 300;
  color: $outline-variant;
}
&lt;/style&gt;

