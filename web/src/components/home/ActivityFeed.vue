<template>
  <div>
    <div class="section-title-row">
      <span class="h2">最新动态</span>
    </div>
    <div class="activity-list">
      <div class="activity-item" v-for="(item, index) in list" :key="index">
        <div class="act-icon" :class="item.bgClass">
          <i class="fa-solid" :class="item.icon"></i>
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
defineProps({ isPrivacyOn: Boolean });

const list = [
  { title: '工资收入', date: '今日 09:30', account: '招商银行', amount: '+35,000.00', icon: 'fa-wallet', bgClass: 'bg-green', textClass: 'text-green' },
  { title: '基金定投扣款', date: '昨日 14:00', account: '且慢账户', amount: '-5,000.00', icon: 'fa-seedling', bgClass: 'bg-blue', textClass: 'text-black' },
  { title: '房贷偿还', date: '10月20日', account: '工商银行', amount: '-8,500.00', icon: 'fa-house', bgClass: 'bg-orange', textClass: 'text-black' }
];
</script>

<style scoped>
.section-title-row { padding: 0 24px 12px 24px; }
.h2 { font-size: 16px; font-weight: 700; color: var(--text-main); }
.activity-list { padding: 0 20px; margin-bottom: 30px; }
.activity-item {
  background: #FFFFFF; padding: 16px; border-radius: 16px; margin-bottom: 10px;
  display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
}
.act-icon { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: white; margin-right: 12px; font-size: 16px; }
.bg-green { background: #10B981; }
.bg-blue { background: #3B82F6; }
.bg-orange { background: #F59E0B; }
.act-content { flex: 1; }
.act-title { font-size: 14px; font-weight: 600; color: var(--text-main); display: block; margin-bottom: 2px; }
.act-date { font-size: 11px; color: var(--text-sub); display: block; }
.act-amount { font-size: 15px; font-weight: 700; font-family: monospace; }
.text-green { color: #10B981; }
.text-black { color: var(--text-main); }
</style>