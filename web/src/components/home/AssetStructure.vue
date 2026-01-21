<template>
  <div class="section-card">
    <!-- 资产负债比 -->
    <div class="chart-block">
      <div class="chart-header">
        <span class="chart-title">资产负债比</span>
        <span class="chart-value">
          <span class="unit">权益占比</span>
          {{ isPrivacyOn ? '**%' : '72%' }} 
        </span>
      </div>
      <div class="balance-bar-container">
        <div class="balance-track">
          <div class="balance-fill" style="width: 72%"></div>
        </div>
        <div class="balance-labels">
          <span>{{ isPrivacyOn ? '****' : '总资产 ¥405w' }}</span>
          <span>{{ isPrivacyOn ? '****' : '负债 ¥120w' }}</span>
        </div>
      </div>
    </div>

    <!-- 资产分布 -->
    <div class="chart-block">
      <div class="chart-header">
        <span class="chart-title">资产分布</span>
      </div>
      <div class="stack-bar">
        <div class="stack-segment seg-1" style="width: 15%"></div>
        <div class="stack-segment seg-2" style="width: 45%"></div>
        <div class="stack-segment seg-3" style="width: 30%"></div>
        <div class="stack-segment seg-4" style="width: 10%"></div>
      </div>
      <div class="legend-row">
        <div class="legend-item"><div class="dot seg-1"></div>现金 15%</div>
        <div class="legend-item"><div class="dot seg-2"></div>投资 45%</div>
        <div class="legend-item"><div class="dot seg-3"></div>固产 30%</div>
        <div class="legend-item"><div class="dot seg-4"></div>其他 10%</div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ isPrivacyOn: Boolean });
</script>

<style scoped>
.section-card {
  margin: 0 20px 24px 20px; background: #FFFFFF; border-radius: 20px; padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.chart-block { margin-bottom: 24px; }
.chart-block:last-child { margin-bottom: 0; }
.chart-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 12px; }
.chart-title { font-size: 14px; font-weight: 700; color: var(--text-main); }
.chart-value { font-size: 16px; font-weight: 700; color: var(--primary); }
.chart-value .unit { font-size: 14px; color: var(--text-sub); font-weight: 400; margin-left: 2px;}
.chart-action { font-size: 12px; color: var(--text-sub); cursor: pointer;}
.balance-track { height: 12px; background: #FEE2E2; border-radius: 5px; overflow: hidden; width: 100%; }
.balance-fill { height: 100%; background: var(--primary); border-radius: 5px; }
.balance-labels { display: flex; justify-content: space-between; font-size: 16px; color: var(--text-sub); margin-top: 6px; font-weight: 500;}

/* Stack Bar Colors */
.stack-bar { height: 12px; border-radius: 8px; overflow: hidden; display: flex; width: 100%; margin-bottom: 12px; }
.stack-segment { height: 100%; position: relative; }
.stack-segment:not(:last-child)::after { content: ''; position: absolute; right: 0; top: 0; bottom: 0; width: 2px; background: #fff; }
.seg-1 { background: #4facfe; }
.seg-2 { background: #2a806c; }
.seg-3 { background: #e3c46e; }
.seg-4 { background: #AF52DE; }
.legend-row { display: flex; justify-content: space-between; width: 100%; }
.legend-item { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #6B7280; font-weight: 500; flex: 1; justify-content: center; }
.dot { width: 8px; height: 8px; border-radius: 50%; }
</style>