<template>
  <div>
    <div class="section-title-row">
      <span class="h2">目标追踪</span>
      <span class="link">全部</span>
    </div>

    <div class="goal-cockpit-card">
      <div class="cockpit-top">
        <!-- 左侧 -->
        <div class="cockpit-left">
          <div class="cp-label">
            <i class="fa-solid fa-piggy-bank icon-pig"></i> 愿望基金
          </div>
          <div class="cp-amount">
            {{ isPrivacyOn ? '****' : '¥ 1,180,000' }}
          </div>
          <div class="cp-sub">累计达成 42%</div>
        </div>

        <div class="cp-divider"></div>

        <!-- 右侧 -->
        <div class="cockpit-right">
          <div class="cp-label">本月投入</div>
          <div class="monthly-status">
            <span class="curr">{{ isPrivacyOn ? '****' : '¥ 8k' }}</span>
            <span class="target">/ 计划 1w</span>
          </div>
          <div class="mini-track">
            <div class="mini-fill" style="width: 80%;"></div>
          </div>
          <div class="cp-status-text">还需存入 ¥2,000</div>
        </div>
      </div>

      <!-- 底部 -->
      <div class="cockpit-bottom">
        <div class="milestone-row">
          <div class="ms-icon-box warn">
            <i class="fa-solid fa-triangle-exclamation"></i>
          </div>
          <div class="ms-content">
            <div class="ms-title">子女教育金滞后</div>
            <div class="ms-desc">建议本月额外追加 ¥2,000 补齐缺口</div>
          </div>
          <div class="ms-action">去调整</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ isPrivacyOn: Boolean });
</script>

<style scoped>
.section-title-row { padding: 0 24px 12px 24px; display: flex; justify-content: space-between; align-items: center; }
.h2 { font-size: 16px; font-weight: 700; color: var(--text-main); }
.link { font-size: 12px; color: var(--primary); cursor: pointer; }

.goal-cockpit-card {
  margin: 0 20px 24px 20px; background: #FFFFFF; border-radius: 20px; padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03); border: 1px solid rgba(0,0,0,0.01);
}
.cockpit-top { display: flex; justify-content: space-between; margin-bottom: 20px; }
.cockpit-left, .cockpit-right { flex: 1; }
.cp-divider { width: 1px; background: #F3F4F6; margin: 0 20px; }
.icon-pig { color: var(--primary); margin-right: 4px; }
.cp-label { font-size: 11px; color: var(--text-sub); margin-bottom: 6px; display: flex; align-items: center; font-weight: 600; }
.cp-amount { font-size: 20px; font-weight: 700; color: var(--text-main); font-family: monospace; margin-bottom: 4px; line-height: 1.2; }
.cp-sub { font-size: 10px; color: var(--primary); background: #ecfdf5; padding: 2px 6px; border-radius: 4px; display: inline-block; font-weight: 600; }
.monthly-status { font-family: monospace; margin-bottom: 6px; }
.monthly-status .curr { font-size: 16px; font-weight: 700; color: var(--text-main); }
.monthly-status .target { font-size: 12px; color: var(--text-sub); }
.mini-track { height: 6px; background: #F3F4F6; border-radius: 3px; overflow: hidden; margin-bottom: 6px; }
.mini-fill { height: 100%; background: var(--primary); border-radius: 3px; }
.cp-status-text { font-size: 10px; color: var(--status-warn); font-weight: 600; }

.cockpit-bottom { border-top: 1px solid #F9FAFB; padding-top: 16px; }
.milestone-row { display: flex; align-items: center; }
.ms-icon-box { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin-right: 12px; }
.ms-icon-box.warn { background: #FEF2F2; color: var(--status-danger); }
.ms-content { flex: 1; }
.ms-title { font-size: 13px; font-weight: 700; color: var(--text-main); margin-bottom: 2px; }
.ms-desc { font-size: 11px; color: var(--text-sub); }
.ms-action { font-size: 11px; color: var(--status-danger); border: 1px solid var(--status-danger); padding: 4px 10px; border-radius: 12px; font-weight: 600; cursor: pointer; }
</style>