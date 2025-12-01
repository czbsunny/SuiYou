<template>
  <view 
      class="account-card"
      @click="$emit('click', account)"
  >
    <!-- 头部区域 -->
    <view class="card-header">
      <view class="header-left">
        <!-- 图标容器：根据类型动态添加类名 -->
        <view :class="['icon-box', getTypeClass(account.type)]">
          <img :src="getIcon(account.type)" class="account-icon" />
        </view>
        
        <view class="account-info">
           <text class="account-name">{{ account.name }}</text>
           <text class="account-tag">{{ account.type }}</text>
        </view>
      </view>
      
      <!-- 编辑按钮 -->
      <view 
          class="more-btn"
          @click.stop="$emit('edit', account)"
      >
        <img src="/static/images/ellipsis.png" class="more-icon" />
      </view>
    </view>

    <!-- 数据区域 -->
    <view class="card-body">
      <view class="data-item">
        <text class="label">持仓市值</text>
        <text class="value main-value">
          {{ showBalance ? formatCurrency(account.totalAsset)  : '****' }}
        </text>
      </view>
      
      <view class="data-item align-right">
        <text class="label">当日收益</text>
        <text :class="['value', account.dailyReturn >= 0 ? 'text-gain' : 'text-loss']">
          {{ account.dailyReturn >= 0 ? '+' : '' }}{{ showBalance ? formatNumber(account.dailyReturn) : '***' }}
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { formatNumber, formatCurrency } from '../../services/formatUtil';

defineProps(['account', 'showBalance']);
defineEmits(['edit', 'click']);

const getIcon = (type) => {
  switch (type) {
    case 'ALIPAY': return '/static/icons/zfb.png';
    case 'TIANTIAN': return '/static/icons/trending-up.png';
    case 'XUEQIU': return '/static/icons/pie-chart.png';
    default: return '/static/icons/wallet.png';
  }
};

// 将原来的 Tailwind 类名映射改为语义化类名
const getTypeClass = (type) => {
   switch (type) {
    case 'ALIPAY': return 'type-alipay';
    case 'TIANTIAN': return 'type-tiantian';
    case 'XUEQIU': return 'type-xueqiu';
    default: return 'type-default';
  }
};
</script>

<style lang="scss" scoped>
.account-card {
  background-color: #ffffff;
  border-radius: 16px; /* rounded-2xl */
  padding: 20px;       /* p-5 */
  margin-bottom: 12px; /* mb-3 */
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); /* shadow-sm */
  border: 1px solid transparent;
  transition: all 0.3s ease;
  cursor: pointer;

  /* hover 效果 (注意：小程序端 hover 可能需要使用 hover-class) */
  &:active, &:hover {
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); /* shadow-md */
    border-color: rgba($primary, 0.2); /* hover:border-subtle */
  }
}

.card-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px; /* mb-4 */
  border-bottom: 1px solid $border-color;
  padding-bottom: 12px; /* pb-3 */
}

.header-left {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.icon-box {
  width: 40px;  /* w-10 */
  height: 40px; /* h-10 */
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px; /* space-x-3 gap */
  
  /* 对应原来的 bg-xxx 逻辑 */
  &.type-alipay { background-color: #eff6ff; } /* bg-blue-50 */
  &.type-tiantian { background-color: #fff7ed; } /* bg-orange-50 */
  &.type-xueqiu { background-color: #eff6ff; } /* bg-blue-50 */
  &.type-default { background-color: rgba($primary, 0.1); }
}

.account-icon {
  width: 20px;
  height: 20px;
  /* 如果 PNG 图片本身自带颜色，不需要 CSS 控制颜色。
     如果是 SVG 且需要变色，可以使用 filter 或 CSS mask */
}

.account-info {
  display: flex;
  flex-direction: column;
}

.account-name {
  display: block;
  font-size: 14px; /* text-sm */
  font-weight: bold;
  color: $text-main;
}

.account-tag {
  font-size: 10px;
  padding: 2px 6px; /* px-1.5 py-0.5 */
  border-radius: 4px;
  color: $text-sub;
  background-color: $bg-subtle;
  margin-top: 2px;
  align-self: flex-start; /* 防止标签占满整行 */
}

.more-btn {
  padding: 8px; /* p-2 */
  border-radius: 50%;
  transition: background-color 0.2s;
  
  /* 模拟 text-gray-300 */
  opacity: 0.6; 

  &:active {
    background-color: $bg-subtle;
    opacity: 1;
  }
}

.more-icon {
  width: 20px;
  height: 20px;
}

.card-body {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-end;
}

.data-item {
  display: flex;
  flex-direction: column;
}

.align-right {
  text-align: right;
  align-items: flex-end;
}

.label {
  font-size: 12px; /* text-xs */
  color: $text-sub;
  margin-bottom: 4px; /* mb-1 */
  display: block;
}

.value {
  font-size: 16px; /* text-base */
  font-weight: bold;
  font-family: monospace; /* font-mono */
  
  &.main-value {
    font-size: 18px; /* text-lg */
    color: $text-main;
  }
  
  &.text-gain {
    color: $color-gain;
  }
  
  &.text-loss {
    color: $color-loss;
  }
}
</style>