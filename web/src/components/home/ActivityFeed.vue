<template>
  <view class="activity-section">
    <view class="section-title-row">
      <text class="h2">最新动态</text>
    </view>
    
    <view class="activity-list">
      <view class="activity-item card-warm" v-for="(item, index) in list" :key="index" @click="navToDetail(item)">
        <!-- 左侧图标 -->
        <view class="act-icon" :style="{ backgroundColor: item.iconBg }">
          <image :src="item.icon" mode="aspectFit" class="icon-image" />
        </view>
        
        <!-- 中间内容 -->
        <view class="act-content">
          <text class="act-title">{{ item.title }}</text>
          <view class="act-sub-row">
            <text class="act-date">{{ item.date }}</text>
            <text class="act-sep">·</text>
            <text class="act-account">{{ item.accountDisplay }}</text>
          </view>
        </view>
        
        <!-- 右侧金额 -->
        <view class="act-amount num-font" :class="item.textClass">
          {{ isPrivacyOn ? '****' : item.amountDisplay }}
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="list.length === 0 && !loading" class="empty-hint">
        <text>近 7 天暂无动态</text>
      </view>
      
      <!-- 加载更多 -->
      <view v-if="list.length > 0" class="load-more">
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
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { getTransactions } from '@/services/transactionService';
import { useConfigStore } from '@/stores/config';
import { TRANSACTION_ICONS } from '@/configs/common';

const props = defineProps({ isPrivacyOn: Boolean });
const configStore = useConfigStore();
const transactions = ref([]);
const loading = ref(false);
const page = ref(0);
const hasMore = ref(true);

const list = computed(() => {
  return transactions.value.map(transaction => {
    const type = transaction.type;
    const isIncome = type === 'INCOME';
    const isTransfer = type === 'TRANSFER';
    const amount = transaction.amount.toFixed(2);
    
    // 获取分类和机构配置
    const category = configStore.getTransferCategoriesById(transaction.categoryId);
    const instMap = configStore.institutionMap;

    // 逻辑处理：账户显示名
    let accountDisplay = '';
    if (isTransfer) {
      // 转账显示 A -> B
      const source = transaction.sourceAccountName || instMap[transaction.sourceAccountInstitution]?.shortName+'('+transaction.sourceAccountIdentifier+')';
      const target = transaction.targetAccountName || instMap[transaction.targetAccountInstitution]?.shortName+'('+transaction.targetAccountIdentifier+')';
      accountDisplay = `${source} → ${target}`;
    } else if (isIncome) {
      // 收入显示转入到的账户
      accountDisplay = transaction.targetAccountName || instMap[transaction.targetAccountInstitution]?.shortName+'('+transaction.targetAccountIdentifier+')';
    } else {
      // 支出显示扣款账户
      accountDisplay = transaction.sourceAccountName || instMap[transaction.sourceAccountInstitution]?.shortName+'('+transaction.sourceAccountIdentifier+')';
    }

    // 逻辑处理：标题
    let title = category?.name;
    if (!title) {
        if (isTransfer) title = '账户调拨';
        else if (type === 'ADJUST') title = '余额校准';
        else title = isIncome ? '收金' : '杂项支出';
    }

    return {
      id: transaction.id,
      title: title,
      date: formatDate(transaction.transTime),
      accountDisplay: accountDisplay,
      amountDisplay: isIncome ? `+${amount}` : (isTransfer ? amount : `-${amount}`),
      icon: category?.iconUrl || TRANSACTION_ICONS[type]?.iconUrl || '/static/images/default-icon.png',
      iconBg: isIncome ? '#ECFDF5' : (isTransfer ? '#F5F3FF' : '#F9F8F4'), // 对应项目定义的背景色
      textClass: isIncome ? 'text-up' : (isTransfer ? 'text-main' : 'text-main')
    };
  });
});

// 时间格式化：今日/昨日/日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr.replace('T', ' ')); // 兼容性处理
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
    const data = await getTransactions({ page: currentPage, size: 10, sort: 'transTime,desc' });
    
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

const navToDetail = (item) => {
    // uni.navigateTo({ url: `/pages/transactions/detail?id=${item.id}` });
};

// 暴露方法给父组件
defineExpose({
  handleLoadMore
});

onMounted(() => fetchTransactions());
</script>

<style lang="scss" scoped>
.activity-section {
  padding-bottom: 20rpx;
}

.section-title-row {
  padding: $spacing-base $spacing-base $spacing-sm;
  .h2 { font-size: 32rpx; font-weight: $fw-bold; color: $text-main; }
}

.activity-list {
  padding: 0 $spacing-base;
}

.activity-item {
  padding: $spacing-base;
  margin-bottom: $spacing-sm;
  display: flex;
  align-items: center;
  transition: all 0.2s;
  
  &:active {
    transform: scale(0.98);
    background-color: $bg-subtle;
  }
}

.act-icon {
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

.act-content {
  flex: 1;
  min-width: 0; // 保证 ellipsis 生效
  
  .act-title {
    font-size: 28rpx;
    font-weight: $fw-semibold;
    color: $text-main;
    display: block;
    margin-bottom: 6rpx;
    @include text-ellipsis;
  }
  
  .act-sub-row {
    display: flex;
    align-items: center;
    font-size: 22rpx;
    color: $text-muted;
    
    .act-sep {
      margin: 0 8rpx;
    }
    
    .act-account {
      @include text-ellipsis;
    }
  }
}

.act-amount {
  font-size: 32rpx;
  font-weight: $fw-bold;
  margin-left: 20rpx;
  text-align: right;
}

.empty-hint {
  padding: 60rpx 0;
  text-align: center;
  color: $text-placeholder;
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
</style>