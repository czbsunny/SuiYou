<template>
  <div class="nav-bar">
    <div class="nav-left">
      <span class="brand-text">FAMILY CFO</span>
      <div class="user-trigger">
        <span class="greeting">晚上好，张总</span>
        <i class="fa-solid fa-chevron-down arrow-icon"></i>
      </div>
    </div>
    <div class="nav-right">
      <!-- 点击触发父组件事件 -->
      <div class="icon-btn" @click="$emit('togglePrivacy')">
        <i class="fa-solid" :class="isPrivacyOn ? 'fa-eye-slash' : 'fa-eye'"></i>
      </div>
      <div class="icon-btn">
        <i class="fa-regular fa-bell"></i>
        <div class="red-dot"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  isPrivacyOn: Boolean
});
defineEmits(['togglePrivacy']);
</script>

<style scoped>
.nav-bar {
  padding: 50px 20px 15px 20px;
  background: var(--bg-body);
  display: flex; justify-content: space-between; align-items: center;
  position: sticky; top: 0; z-index: 50;
}
.brand-text { font-size: 10px; color: var(--primary); font-weight: 800; letter-spacing: 1px; display: block; margin-bottom: 2px;}
.user-trigger { display: flex; align-items: center; cursor: pointer; }
.greeting { font-size: 18px; font-weight: 700; color: var(--text-main); }
.arrow-icon { font-size: 10px; margin-left: 6px; color: #9CA3AF; }
.nav-right { display: flex; gap: 12px; }
.icon-btn {
  width: 36px; height: 36px; border-radius: 50%; background: #fff;
  display: flex; align-items: center; justify-content: center;
  color: #4B5563; box-shadow: 0 2px 5px rgba(0,0,0,0.05); cursor: pointer;
  transition: transform 0.1s;
}
.icon-btn:active { transform: scale(0.95); }
.red-dot { width: 8px; height: 8px; background: #EF4444; border-radius: 50%; position: absolute; top: 8px; right: 8px; border: 1px solid #fff;}
</style>