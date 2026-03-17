<template>
  <view class="account-page-wallet">
    <view class="wallet-header">
      <text class="page-title">钱包账户</text>
    </view>
    
    <view class="wallet-content">
      <view class="wallet-balance">
        <text class="balance-label">账户余额</text>
        <text class="balance-amount money">¥{{ formatAmount(balance) }}</text>
      </view>
      
      <view class="wallet-actions">
        <view class="action-item btn-active" @tap="handleAction('RECHARGE')">
          <view class="icon-box bg-recharge">
            <img src="@/static/assets/actions/recharge.png" class="icon-img" alt="recharge">
          </view>
          <text>充值</text>
        </view>
        <view class="action-item btn-active" @tap="handleAction('WITHDRAW')">
          <view class="icon-box bg-withdraw">
            <img src="@/static/assets/actions/withdraw.png" class="icon-img" alt="withdraw">
          </view>
          <text>提现</text>
        </view>
        <view class="action-item btn-active" @tap="handleAction('TRANSFER')">
          <view class="icon-box bg-transfer">
            <img src="@/static/assets/actions/transfer.png" class="icon-img" alt="transfer">
          </view>
          <text>转账</text>
        </view>
      </view>
      
      <view class="wallet-history">
        <view class="history-header">
          <text class="history-title">交易记录</text>
          <text class="history-more">查看更多</text>
        </view>
        
        <view class="history-list">
          <view v-for="record in transactionHistory" :key="record.id" class="history-item">
            <view class="item-left">
              <view class="record-icon" :style="{ backgroundColor: record.color }"></view>
              <view class="record-info">
                <text class="record-title">{{ record.title }}</text>
                <text class="record-time">{{ record.time }}</text>
              </view>
            </view>
            <text class="record-amount" :class="{ 'income': record.type === 'income' }">
              {{ record.type === 'income' ? '+' : '-' }}¥{{ formatAmount(record.amount) }}
            </text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const accountId = route.params.id;

const balance = ref(0);
const transactionHistory = ref([]);

onMounted(() => {
  // 这里应该从API获取账户信息和交易记录
  // 暂时使用模拟数据
  fetchAccountData();
  fetchTransactionHistory();
});

const fetchAccountData = () => {
  // 模拟API调用
  balance.value = 8888.88;
};

const fetchTransactionHistory = () => {
  // 模拟API调用
  transactionHistory.value = [
    {
      id: '1',
      title: '充值',
      amount: 1000.00,
      type: 'income',
      time: '2026-03-17 10:00',
      color: '#22c55e'
    },
    {
      id: '2',
      title: '购物',
      amount: 200.00,
      type: 'expense',
      time: '2026-03-16 15:30',
      color: '#ef4444'
    },
    {
      id: '3',
      title: '转账',
      amount: 500.00,
      type: 'expense',
      time: '2026-03-15 09:00',
      color: '#3b82f6'
    }
  ];
};

const formatAmount = (val) => Number(Math.abs(val) || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });

const handleAction = (action) => {
  console.log('Action clicked:', action);
};
</script>

<style lang="scss" scoped>
.account-page-wallet {
  padding: 24rpx 32rpx;
}

.wallet-header {
  margin-bottom: 40rpx;
  .page-title {
    font-size: 36rpx;
    font-weight: 800;
    color: #1F2937;
  }
}

.wallet-balance {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 40rpx;
  padding: 48rpx;
  margin-bottom: 40rpx;
  color: #fff;
  .balance-label {
    font-size: 24rpx;
    opacity: 0.8;
    margin-bottom: 16rpx;
    display: block;
  }
  .balance-amount {
    font-size: 64rpx;
    font-weight: 800;
  }
}

.wallet-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24rpx;
  margin-bottom: 48rpx;
  .action-item {
    background: #fff;
    border-radius: 36rpx;
    padding: 32rpx 24rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
    .icon-box {
      width: 80rpx;
      height: 80rpx;
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      &.bg-recharge { background: #d1fae5; }
      &.bg-withdraw { background: #fee2e2; }
      &.bg-transfer { background: #dbeafe; }
      .icon-img {
        max-width: 60%;
        max-height: 60%;
        object-fit: contain;
      }
    }
    text {
      font-size: 24rpx;
      font-weight: 700;
      color: #374151;
    }
  }
}

.wallet-history {
  .history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    .history-title {
      font-size: 28rpx;
      font-weight: 800;
      color: #374151;
    }
    .history-more {
      font-size: 22rpx;
      color: #6b7280;
    }
  }
  .history-list {
    .history-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 32rpx 0;
      border-bottom: 1rpx solid #F8FAFC;
      .item-left {
        display: flex;
        align-items: center;
        gap: 20rpx;
        .record-icon {
          width: 64rpx;
          height: 64rpx;
          border-radius: 16rpx;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        .record-info {
          .record-title {
            font-size: 28rpx;
            font-weight: 700;
            color: #374151;
            margin-bottom: 8rpx;
            display: block;
          }
          .record-time {
            font-size: 20rpx;
            color: #9CA3AF;
          }
        }
      }
      .record-amount {
        font-size: 28rpx;
        font-weight: 800;
        color: #ef4444;
        &.income {
          color: #22c55e;
        }
      }
    }
  }
}

/* 特殊样式 */
.money { font-family: 'JetBrains Mono', 'DIN Alternate', monospace; }
.btn-active:active { opacity: 0.7; transform: scale(0.98); }
</style>