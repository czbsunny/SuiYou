<script setup lang="ts">
import { ref, computed } from 'vue';

// --- 类型定义 ---
type StatusType = 'ON_TRACK' | 'AT_RISK' | 'COMPLETED';

interface WishlistItem {
  id: string;
  icon: string;
  title: string;
  current: number;
  target: number;
  status: StatusType;
}

interface AchievementItem {
  id: string;
  icon: string;
  title: string;
  date: string;
  amount: number;
}

// --- 数据状态 (Mock Data) ---
const primaryGoal = ref({
  title: '快乐老家',
  subtitle: '首要目标 · 婚房基金',
  image: 'https://images.unsplash.com/photo-1518780664697-55e3ad937233?q=80&w=1000&auto=format&fit=crop',
  progress: 32,
  status: 'ON_TRACK' as StatusType,
  monthlyAuto: 2000,
  nextDeduction: '11月15日'
});

const wishlist = ref<WishlistItem[]>([
  { id: '1', icon: '🏔️', title: '川西徒步', current: 8000, target: 15000, status: 'ON_TRACK' },
  { id: '2', icon: '📷', title: 'Sony A7M4', current: 5000, target: 18000, status: 'AT_RISK' },
  { id: '3', icon: '💻', title: 'MacBook Pro', current: 2000, target: 16000, status: 'ON_TRACK' },
]);

const achievements = ref<AchievementItem[]>([
  { id: '1', icon: '🎓', title: '助学贷款结清', date: '2023.05', amount: 24000 },
  { id: '2', icon: '💰', title: '人生第一桶金', date: '2022.12', amount: 100000 },
]);

// --- 工具函数 ---
const formatMoney = (val: number) => val.toLocaleString();

// 获取状态样式配置
const getStatusConfig = (status: StatusType) => {
  const config = {
    'ON_TRACK': { class: 'bg-green-100 text-green-700', icon: 'fa-check-circle', text: 'On Track' },
    'AT_RISK': { class: 'bg-orange-100 text-orange-700', icon: 'fa-triangle-exclamation', text: 'At Risk' },
    'COMPLETED': { class: 'bg-yellow-100 text-yellow-700', icon: 'fa-trophy', text: 'Done' },
  };
  return config[status] || config['ON_TRACK'];
};

// 计算百分比
const getPercent = (current: number, target: number) => Math.round((current / target) * 100);

</script>

<template>
  <Transition name="slide-fade" appear>
    <div class="flex-1 flex flex-col h-full bg-[#F5F7F9] overflow-hidden">
      
      <!-- Header -->
      <header class="px-6 pb-2 pt-8 safe-top bg-[#F5F7F9]/95 backdrop-blur-sm sticky top-0 z-20">
        <div class="flex justify-between items-center mb-4">
          <h1 class="font-serif text-2xl font-bold text-[#2C3E50]">目标管理</h1>
          <button class="w-8 h-8 rounded-full bg-white shadow-sm border border-gray-100 flex items-center justify-center text-[#2a806c] active:scale-95 transition-transform">
            <i class="fa-solid fa-plus"></i>
          </button>
        </div>

        <!-- Summary Dashboard -->
        <div class="flex gap-4 overflow-x-auto no-scrollbar pb-2">
          <div class="pl-1">
            <p class="text-[10px] text-[#95A5A6] font-bold uppercase tracking-wider mb-1">Total Saved</p>
            <p class="text-xl font-sans font-bold text-[#2C3E50]">¥684,500</p>
          </div>
          <div class="w-px h-8 bg-gray-200"></div>
          <div>
            <p class="text-[10px] text-[#95A5A6] font-bold uppercase tracking-wider mb-1">Monthly Auto</p>
            <p class="text-xl font-sans font-bold text-[#2a806c]">+¥5,500</p>
          </div>
          <div class="w-px h-8 bg-gray-200"></div>
          <div>
            <p class="text-[10px] text-[#95A5A6] font-bold uppercase tracking-wider mb-1">Avg Progress</p>
            <p class="text-xl font-sans font-bold text-[#D4AF37]">24%</p>
          </div>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto no-scrollbar px-6 pb-24 pt-2">
        
        <!-- Section: Primary Focus (Hero Card) -->
        <section class="mb-8">
          <div class="flex justify-between items-end mb-3">
            <h2 class="text-sm font-bold text-[#2C3E50]">当前主线 (Focus)</h2>
          </div>
          
          <div class="relative w-full aspect-[4/3] rounded-[2rem] overflow-hidden shadow-[0_4px_12px_rgba(44,62,80,0.06)] group">
            <img 
              :src="primaryGoal.image" 
              alt="Dream Goal" 
              class="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>

            <div class="absolute bottom-0 left-0 right-0 p-6 text-white">
              <div class="flex justify-between items-start mb-3">
                <div>
                  <div class="flex items-center gap-2 mb-1">
                    <h3 class="text-2xl font-serif font-bold">{{ primaryGoal.title }}</h3>
                    <!-- Status Pill -->
                    <span :class="`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold border border-white/50 shadow-sm ${getStatusConfig(primaryGoal.status).class}`">
                      <i :class="`fa-solid ${getStatusConfig(primaryGoal.status).icon}`"></i> 
                      {{ getStatusConfig(primaryGoal.status).text }}
                    </span>
                  </div>
                  <p class="text-white/70 text-xs">{{ primaryGoal.subtitle }}</p>
                </div>
                <div class="text-right">
                  <div class="text-2xl font-sans font-bold text-[#D4AF37]">{{ primaryGoal.progress }}%</div>
                </div>
              </div>

              <!-- Progress Bar -->
              <div class="w-full h-1.5 bg-white/20 rounded-full mb-4 overflow-hidden backdrop-blur-sm">
                <div 
                  class="h-full bg-[#D4AF37] rounded-full shadow-[0_0_10px_rgba(212,175,55,0.5)]" 
                  :style="{ width: `${primaryGoal.progress}%` }"
                ></div>
              </div>

              <!-- Auto-save Info -->
              <div class="flex items-center gap-3 text-xs font-medium bg-white/10 backdrop-blur-md p-3 rounded-xl border border-white/10">
                <div class="w-8 h-8 rounded-full bg-green-500/20 flex items-center justify-center text-green-400">
                  <i class="fa-solid fa-rotate"></i>
                </div>
                <div>
                  <div class="text-white">每月自动存入 <span class="text-green-300 font-bold">+¥{{ formatMoney(primaryGoal.monthlyAuto) }}</span></div>
                  <div class="text-white/50 text-[10px]">下一次扣款: {{ primaryGoal.nextDeduction }}</div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Section: Wishlist Grid (In Progress) -->
        <section class="mb-8">
          <div class="flex justify-between items-end mb-3">
            <h2 class="text-sm font-bold text-[#2C3E50]">愿望清单 (Wishlist)</h2>
            <span class="text-[10px] text-[#95A5A6]">进行中 {{ wishlist.length }}</span>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <!-- Wishlist Item Loop -->
            <div 
              v-for="item in wishlist" 
              :key="item.id"
              class="bg-white p-4 rounded-3xl shadow-[0_8px_30px_rgba(44,62,80,0.04)] border border-white flex flex-col justify-between h-40 relative overflow-hidden active:scale-95 transition-transform cursor-pointer"
            >
              <div class="flex justify-between items-start z-10">
                <div class="w-10 h-10 rounded-2xl bg-[#F5F7F9] flex items-center justify-center text-xl shadow-inner text-[#2C3E50]">
                  {{ item.icon }}
                </div>
                <div v-if="item.status === 'AT_RISK'" class="w-2 h-2 rounded-full bg-[#E67E22] animate-pulse"></div>
              </div>

              <div class="z-10">
                <h4 class="font-bold text-[#2C3E50] mb-1">{{ item.title }}</h4>
                <div class="text-[10px] text-[#95A5A6] mb-2">
                  ¥{{ formatMoney(item.current) }} / {{ formatMoney(item.target) }}
                </div>
                <div class="w-full h-1.5 bg-gray-100 rounded-full overflow-hidden">
                  <div 
                    :class="`h-full rounded-full ${item.status === 'AT_RISK' ? 'bg-[#E67E22]' : 'bg-[#2a806c]'}`" 
                    :style="{ width: `${getPercent(item.current, item.target)}%` }"
                  ></div>
                </div>
              </div>

              <!-- Quick Add Hover Button -->
              <button class="absolute top-0 right-0 p-4 opacity-0 hover:opacity-100 transition-opacity z-20">
                <div class="w-6 h-6 rounded-full bg-gray-50 flex items-center justify-center text-gray-400 hover:bg-[#2a806c] hover:text-white transition-colors">
                  <i class="fa-solid fa-plus text-xs"></i>
                </div>
              </button>
            </div>

            <!-- New Wish Button -->
            <div class="bg-white/50 border-2 border-dashed border-gray-200 rounded-3xl flex items-center justify-center h-40 active:scale-95 transition-transform cursor-pointer hover:bg-white/80">
              <div class="text-center text-gray-300">
                <i class="fa-solid fa-plus text-2xl mb-2"></i>
                <div class="text-xs">新愿望</div>
              </div>
            </div>
          </div>
        </section>

        <!-- Section: Achievement Gallery (Completed) -->
        <section>
          <div class="flex justify-between items-end mb-3">
            <h2 class="text-sm font-bold text-[#2C3E50]">成就展馆 (Hall of Fame)</h2>
            <span class="text-[10px] text-[#95A5A6]">已达成 {{ achievements.length }}</span>
          </div>

          <div class="space-y-3">
            <div 
              v-for="item in achievements" 
              :key="item.id"
              class="flex items-center justify-between p-3 bg-white border border-yellow-100 rounded-2xl shadow-sm relative overflow-hidden group hover:shadow-md transition-shadow"
            >
              <!-- Background shine effect -->
              <div class="absolute top-0 right-0 w-24 h-full bg-gradient-to-l from-yellow-50 via-white/50 to-transparent opacity-60"></div>
              
              <div class="flex items-center gap-3 z-10">
                <div class="w-10 h-10 rounded-full bg-gradient-to-br from-yellow-50 to-white text-yellow-600 flex items-center justify-center text-lg border border-yellow-100 shadow-sm">
                  {{ item.icon }}
                </div>
                <div>
                  <div class="text-sm font-bold text-[#2C3E50] group-hover:text-yellow-700 transition-colors">
                    {{ item.title }}
                  </div>
                  <div class="text-[10px] text-[#95A5A6]">达成于 {{ item.date }}</div>
                </div>
              </div>
              
              <div class="text-right z-10">
                <div class="text-xs font-bold text-[#2C3E50]">¥{{ formatMoney(item.amount) }}</div>
                <div class="text-[10px] text-yellow-600 font-bold uppercase tracking-wide flex items-center justify-end gap-1">
                  <i class="fa-solid fa-medal"></i> Done
                </div>
              </div>
            </div>
          </div>
        </section>

      </main>
    </div>
  </Transition>
</template>

<style scoped>
/* 隐藏滚动条 */
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 简单的进场动画替代 framer-motion */
.slide-fade-enter-active {
  transition: all 0.4s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(20px);
  opacity: 0;
}
</style>