<template>
  <div class="quick-actions-grid">
    <div class="action-btn">
      <div class="ab-icon blue"><i class="fa-solid fa-plus"></i></div>
      <span>记一笔</span>
    </div>
    <div class="action-btn">
      <div class="ab-icon green"><i class="fa-solid fa-money-bill-transfer"></i></div>
      <span>转账划转</span>
    </div>
    <div class="action-btn">
      <div class="ab-icon purple"><i class="fa-solid fa-chart-pie"></i></div>
      <span>再平衡</span>
    </div>
    <div class="action-btn">
      <div class="ab-icon orange"><i class="fa-solid fa-file-invoice"></i></div>
      <span>月度复盘</span>
    </div>
  </div>
</template>

<style scoped>
.quick-actions-grid {
  margin: 0 20px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px;
  background: white; padding: 20px; border-radius: 20px; 
  box-shadow: 0 8px 20px rgba(0,0,0,0.05); margin-bottom: 40px;
}
.action-btn { display: flex; flex-direction: column; align-items: center; gap: 8px; cursor: pointer; }
.ab-icon {
  width: 48px; height: 48px; border-radius: 16px; display: flex; align-items: center; justify-content: center;
  font-size: 18px; transition: transform 0.1s;
}
.action-btn:active .ab-icon { transform: scale(0.95); }
.ab-icon.blue { background: #EFF6FF; color: #007AFF; }
.ab-icon.green { background: #ECFDF5; color: #10B981; }
.ab-icon.purple { background: #F5F3FF; color: #8B5CF6; }
.ab-icon.orange { background: #FFF7ED; color: #F97316; }
.action-btn span { font-size: 11px; color: #4B5563; font-weight: 500; }
</style>