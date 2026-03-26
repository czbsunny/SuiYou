<template>
  <div>
    <div class="section-title-row">
      <span class="h2">最新动态</span>
    </div>
    <div class="activity-list">
      <div class="activity-item" v-for="(item, index) in list" :key="index">
        <div class="act-icon">
          <img :src="item.icon" alt="icon" class="icon-image"></img>
        </div>
        <div class="act-content">
          <span class="act-title">{{ item.title }}</span>
          <span class="act-date">{{ item.date }} · {{ item.account }}</span>
        </div>
        <div class="act-amount" :class="item.textClass">
          {{ isPrivacyOn ? '****' : item.amount }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { getTransactions } from '@/services/transactionService';
import { useConfigStore } from '@/stores/config';
import { TRANSACTION_ICONS } from '@/configs/common';

const configStore = useConfigStore();

defineProps({ isPrivacyOn: Boolean });

const transactions = ref([]);
const loading = ref(false);
const error = ref(null);

const list = computed(() => {
  return transactions.value.map(transaction => {
    const isIncome = transaction.type === 'INCOME';
    const amount = transaction.amount.toFixed(2);
    
    const category = configStore.getTransferCategoriesById(transaction.categoryId);
    console.log(category);
    
    return {
      title: category?.name || TRANSACTION_ICONS[transaction.type].name,
      date: formatDate(transaction.transTime),
      account: transaction.assetName || '未知账户',
      amount: isIncome ? `+${amount}` : `-${amount}`,
      icon: category?.iconUrl || TRANSACTION_ICONS[transaction.type].iconUrl,
      textClass: isIncome ? 'text-green' : 'text-black'
    };
  }).slice(0, 5);
});

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diffTime = now - date;
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
  
  if (diffDays === 0) {
    return `今日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  } else if (diffDays === 1) {
    return `昨日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日`;
  }
};

const fetchTransactions = async () => {
  try {
    loading.value = true;
    error.value = null;
    const data = await getTransactions({ page: 0, size: 10, sort: 'transTime,desc' });
    transactions.value = data.content || [];
  } catch (err) {
    console.error('获取交易记录失败:', err);
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchTransactions();
});
</script>

<style lang="scss" scoped>
.section-title-row { padding: $spacing-md; }
.h2 { font-size: 16px; font-weight: 700; color: var(--text-main); }
.activity-list { padding: 0 $spacing-md; margin-bottom: 30px; }
.activity-item {
  background: #FFFFFF; padding: $spacing-md; border-radius: $radius-lg; margin-bottom: 10px;
  display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
}
.act-icon { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin-right: 12px; }
.icon-image { width: 32px; height: 32px; }
.act-content { flex: 1; }
.act-title { font-size: 14px; font-weight: 600; color: var(--text-main); display: block; margin-bottom: 2px; }
.act-date { font-size: 11px; color: var(--text-sub); display: block; }
.act-amount { font-size: 15px; font-weight: 700; font-family: monospace; }
.text-green { color: #10B981; }
.text-black { color: var(--text-main); }
</style>