<template>
  <div class="page-container mobile-wrapper">
    <!-- 顶部导航：接收隐私切换事件 -->
    <NavBar 
      :is-privacy-on="isPrivacyOn" 
      @toggle-privacy="togglePrivacy" 
    />

    <!-- 1. 核心净资产 -->
    <NetWorthCard :is-privacy-on="isPrivacyOn" />

    <!-- 2. 结构分析 -->
    <AssetStructure :is-privacy-on="isPrivacyOn" />

    <!-- 3. 目标驾驶舱 (你的新设计) -->
    <GoalCockpit :is-privacy-on="isPrivacyOn" />

    <!-- 4. 最新动态 -->
    <ActivityFeed :is-privacy-on="isPrivacyOn" />

    <!-- 5. 快捷操作 -->
    <QuickActions />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import NavBar from '@/components/home/NavBar.vue';
import AssetStructure from '@/components/home/AssetStructure.vue';
import GoalCockpit from '@/components/home/GoalCockpit.vue';
import ActivityFeed from '@/components/home/ActivityFeed.vue';
import QuickActions from '@/components/home/QuickActions.vue';
import NetWorthCard from '@/components/home/NetWorthCard.vue';

// 全局隐私模式状态
const isPrivacyOn = ref(false);

const togglePrivacy = () => {
  isPrivacyOn.value = !isPrivacyOn.value;
};
</script>

<style scoped>
/* 全局样式定义 */
:root {
  --primary: #2a806c;
  --bg-body: #F5F7F9;
  --card-white: #FFFFFF;
  --text-main: #1F2937;
  --text-sub: #9CA3AF;
  --status-warn: #F59E0B;
  --status-danger: #EF4444;
  --status-success: #10B981;
}

body {
  margin: 0;
  background-color: #eef2f5;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", Helvetica, Arial, sans-serif;
}

.mobile-wrapper {
  max-width: 414px;
  margin: 0 auto;
  background-color: var(--bg-body);
  min-height: 100vh;
  box-shadow: 0 0 50px rgba(0,0,0,0.1);
  padding-bottom: 40px;
}
</style>