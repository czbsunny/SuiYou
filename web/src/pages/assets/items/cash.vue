<template>
  <view class="asset-detail-page">
    <!-- 1. 核心资产区 (直接在背景上，去容器) -->
    <view class="asset-hero animate-fade-in">
      <text class="inst-name">金额</text>
      <view class="balance-box">
        <text class="currency">¥</text>
        <text class="balance-val num-font">{{ formatMoney(balance) }}</text>
      </view>
    </view>

    <!-- 2. 动作按钮区 (直接在背景上) -->
    <view class="action-group">
      <button class="btn btn-primary" @tap="handleAction('recharge')" hover-class="btn-hover-active">充值</button>
      <button class="btn btn-outline" @tap="handleAction('withdraw')" hover-class="btn-hover-grey">提现</button>
    </view>

    <!-- 3. 最近记录列表 (卡片化展示) -->
    <view class="history-section">
      <view class="section-header">
        <text class="title">最近记录</text>
      </view>
      
      <scroll-view class="log-list" scroll-y enable-back-to-top>
        <view v-for="(log, index) in logs" :key="log.id" class="log-item" hover-class="item-active">
          <view class="log-icon" :style="{ backgroundColor: log.iconBg }">
            <image :src="log.icon" mode="aspectFit" class="icon-image" />
          </view>
          <view class="log-info">
            <text class="title">{{ log.title }}</text>
            <text class="time">{{ log.time }}</text>
          </view>
          <view class="log-right">
            <text :class="['log-amt', 'num-font', log.textClass]">
              {{ log.amountDisplay }}
            </text>
          </view>
        </view>
        
        <!-- 空状态 -->
        <view v-if="logs.length === 0 && !loading" class="empty-state">
          <text>暂无最近变动记录</text>
        </view>
        
        <!-- 加载更多 -->
        <view v-if="logs.length > 0" class="load-more">
          <view v-if="!loading && hasMore" @click="handleLoadMore" class="load-more-text clickable">
            点击或上拉加载更多
          </view>
          <view v-else-if="loading" class="load-more-text">
            加载中...
          </view>
          <view v-else class="load-more-text">
            没有更多数据了
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="safe-bottom-spacer"></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getTransactions } from '@/services/transactionService';
import { useConfigStore } from '@/stores/config';
import { TRANSACTION_ICONS } from '@/configs/common';

const configStore = useConfigStore();
const balance = ref(1.51);
const currentAssetId = ref(null);
const transactions = ref([]);
const loading = ref(false);
const page = ref(0);
const hasMore = ref(true);

const logs = computed(() => {
  return transactions.value.map(transaction => {
    const type = transaction.type;
    const isIncome = type === 'INCOME';
    const isTransfer = type === 'TRANSFER';
    const isAdjustment = type === 'ADJUSTMENT';
    const isInvestmentReturn = type === 'INVESTMENT_RETURN';

    const amount = transaction.amount.toFixed(2);
    const category = configStore.getTransferCategoriesById(transaction.categoryId);

    let amountDisplay = '';
    if (isTransfer) {
      amountDisplay = amount;
    } else if (isAdjustment || isInvestmentReturn) {
      amountDisplay = `${amount}`;
    } else if (isIncome) {
      amountDisplay = `+${amount}`;
    } else {
      amountDisplay = `-${amount}`;
    }

    let title = category?.name;
    if (!title) {
      if (isTransfer) title = '账户调拨';
      else if (type === 'ADJUSTMENT') title = '资产校准';
      else if (type === 'INVESTMENT_RETURN') title = '投资收益';
      else title = isIncome ? '收金' : '杂项支出';
    }

    return {
      id: transaction.id,
      title: title,
      time: formatDate(transaction.transTime),
      amountDisplay: amountDisplay,
      icon: category?.iconUrl || TRANSACTION_ICONS[type]?.iconUrl || '/static/images/default-icon.png',
      iconBg: isIncome ? '#ECFDF5' : (isTransfer ? '#F5F3FF' : '#F9F8F4'),
      textClass: isIncome ? 'text-up' : 'text-main'
    };
  });
});

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr.replace('T', ' '));
  const now = new Date();
  
  const isSameDay = (d1, d2) => d1.getFullYear() === d2.getFullYear() && d1.getMonth() === d2.getMonth() && d1.getDate() === d2.getDate();
  
  const timeStr = `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  
  if (isSameDay(date, now)) {
    return `今日 ${timeStr}`;
  }
  
  const yesterday = new Date(now);
  yesterday.setDate(now.getDate() - 1);
  if (isSameDay(date, yesterday)) {
    return `昨日 ${timeStr}`;
  }
  
  return `${date.getMonth() + 1}月${date.getDate()}日`;
};

const fetchTransactions = async (isLoadMore = false) => {
  if (loading.value) return;
  
  try {
    loading.value = true;
    const currentPage = isLoadMore ? page.value + 1 : 0;
    const data = await getTransactions({ 
      page: currentPage, 
      size: 10, 
      sort: 'transTime,desc',
      assetId: currentAssetId.value 
    });
    
    if (isLoadMore) {
      transactions.value = [...transactions.value, ...(data.content || [])];
    } else {
      transactions.value = data.content || [];
    }
    
    page.value = currentPage;
    hasMore.value = !data.last;
  } catch (err) {
    console.error('获取交易记录失败:', err);
  } finally {
    loading.value = false;
  }
};

const handleLoadMore = () => {
  if (!loading.value && hasMore.value) {
    fetchTransactions(true);
  }
};

onLoad((options) => {
  let item = null;
  if (options.data) {
    item = JSON.parse(decodeURIComponent(options.data));
    console.log('解析出的资产:', item);

    if (item.name) {
      uni.setNavigationBarTitle({ title: item.name });
    }

    if (item) {
      balance.value = item.availableBalance || 0;
      currentAssetId.value = item.id;
    }
  }
  fetchTransactions();
});

const formatMoney = (val) => Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 });

const handleAction = (type) => {
  uni.vibrateShort();
  uni.showToast({ title: '跳转至' + type, icon: 'none' });
};
</script>

<style lang="scss" scoped>
.asset-detail-page {
  min-height: 100vh;
  background-color: $bg-page; // 应用暖米白 #FAF9F6
  padding: 0 20rpx; // 统一 10px 左右内边距
}

/* 1. 核心资产区优化：直接在背景上显示 */
.asset-hero {
  text-align: center;
  padding: 100rpx 0 60rpx;

  .inst-name {
    font-size: 26rpx;
    color: $text-sub;
    margin-bottom: 20rpx;
    display: block;
    letter-spacing: 2rpx;
  }

  .balance-box {
    display: flex;
    align-items: baseline;
    justify-content: center;
    color: $text-main;

    .currency {
      font-size: 40rpx;
      font-weight: 600;
      margin-right: 8rpx;
    }
    .balance-val {
      font-size: 96rpx;
      font-weight: 800;
      line-height: 1;
    }
  }
}

/* 2. 按钮区优化：左右平排 */
.action-group {
  padding: 40rpx 0 80rpx;
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 30rpx;

  .btn {
    flex: 1;
    height: 100rpx;
    border-radius: $radius-base;
    font-size: 32rpx;
    font-weight: 600;
    @include flex-center;
    transition: all 0.2s;

    &.btn-primary {
      background-color: $primary;
      color: $text-inverse;
      border: none;
      box-shadow: $shadow-button;
    }

    &.btn-outline {
      background-color: $white;
      color: $text-sub;
      border: 1rpx solid $gray-200;
    }
  }
}

/* 3. 最近记录列表：卡片化 */
.history-section {
  .section-header {
    padding: 0 10rpx;
    margin-bottom: 24rpx;
    .title {
      font-size: 28rpx;
      font-weight: 700;
      color: $text-main;
    }
  }
}

.log-list {
  padding-bottom: 40rpx;
}

.log-item {
  background-color: $white;
  border-radius: $radius-lg; // 32rpx
  padding: 30rpx 32rpx;
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  box-shadow: $shadow-card; // 温润环境色阴影
  border: 1rpx solid rgba(50, 46, 43, 0.02);

  .log-icon {
    width: 88rpx;
    height: 88rpx;
    border-radius: 24rpx;
    @include flex-center;
    margin-right: 24rpx;
    flex-shrink: 0;
    
    .icon-image {
      width: 56rpx;
      height: 56rpx;
    }
  }

  .log-info {
    flex: 1;
    min-width: 0;

    .title {
      font-size: 28rpx;
      font-weight: 600;
      color: $text-main;
      display: block;
      @include text-ellipsis;
    }
    .time {
      font-size: 22rpx;
      color: $text-muted;
      margin-top: 8rpx;
      display: block;
    }
  }

  .log-amt {
    font-size: 32rpx;
    font-weight: 700;
    margin-left: 20rpx;
    text-align: right;
    
    &.text-up { color: $color-gain; }
    &.text-main { color: $text-main; }
  }
}

.empty-state {
  padding: 100rpx 0;
  text-align: center;
  color: $text-muted;
  font-size: 24rpx;
}

.load-more {
  padding: 30rpx 0;
  text-align: center;
  
  .load-more-text {
    font-size: 22rpx;
    color: $text-muted;
    
    &.clickable {
      color: $primary;
      cursor: pointer;
      
      &:active {
        opacity: 0.7;
      }
    }
  }
}

.safe-bottom-spacer {
  height: calc(40rpx + env(safe-area-inset-bottom));
}

.btn-hover-active {
  transform: scale(0.97);
  filter: brightness(0.95);
}

.btn-hover-grey {
  background-color: $gray-50 !important;
}

.item-active {
  background-color: #fdfcfb;
  transform: scale(0.99);
}

.animate-fade-in {
  animation: fadeIn 0.6s cubic-bezier(0.25, 1, 0.5, 1);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10rpx); }
  to { opacity: 1; transform: translateY(0); }
}
</style>