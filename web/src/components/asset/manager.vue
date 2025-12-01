<template>
  <view class="manager-page">
    <!-- 提示栏 -->
    <view class="warning-bar">
        <image src="/static/images/info.png" class="warning-icon" />
        <text>长按左侧图标可拖拽排序，松开后自动保存</text>
    </view>

    <!-- 
      movable-area 高度 = 行高 * 数量 
      必须设置明确高度，否则无法拖动到底部 
    -->
    <scroll-view scroll-y class="scroll-container">
        <movable-area 
            class="drag-area" 
            :style="{ height: items.length * rowHeight + 'px' }"
        >
            <movable-view
                v-for="(acc, index) in items"
                :key="acc.id"
                :y="acc.y"
                direction="vertical"
                :disabled="!isDragging"
                :damping="40"
                class="drag-item"
                :style="{ height: rowHeight + 'px', zIndex: currentDragId === acc.id ? 99 : 1 }"
                @change="onMove($event, index)"
                @touchend="onDrop"
            >
                <!-- 卡片内容 -->
                <view class="account-card">
                    <!-- 左侧：拖拽手柄 -->
                    <!-- 
                        touchstart: 激活拖拽 
                        touchend: 关闭拖拽（防止影响输入框点击）
                    -->
                    <view 
                        class="drag-handle"
                        @touchstart="startDrag(acc.id)"
                        @mousedown="startDrag(acc.id)"
                    >
                        <image src="/static/images/ellipsis.png" class="handle-icon" />
                    </view>

                    <!-- 中间：名称输入 -->
                    <view class="input-section">
                        <text class="input-label">账户名称</text>
                        <input 
                          type="text" 
                          v-model="acc.name"
                          @blur="handleUpdateName(acc.id, acc.name)"
                          class="name-input"
                          placeholder="请输入账户名称"
                          placeholder-class="input-placeholder"
                        />
                    </view>

                    <!-- 右侧：操作按钮 -->
                    <view class="action-section">
                         <view 
                           @click.stop="handleClear(acc)"
                           :class="['action-btn', acc.totalValue === 0 ? 'is-disabled' : 'is-active']"
                         >
                             <text>{{ acc.totalValue === 0 ? '已清空' : '清空持仓' }}</text>
                         </view>
                    </view>
                </view>
            </movable-view>
        </movable-area>
    </scroll-view>
    
    <!-- 底部按钮 -->
    <view class="footer">
        <view @click="goBack" class="close-btn">
            <text>完成</text>
        </view>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { 
    getAssetAccounts, 
    updateAssetAccount, 
    clearAccountHoldings 
} from '../../services/accountService';

// --- 拖拽配置 ---
const rowHeight = 90; // 每一行的高度(px)，需与CSS一致，包含margin
const items = ref([]);
const isDragging = ref(false); // 全局拖拽开关
const currentDragId = ref(null); // 当前正在拖拽的ID
let currentY = 0; // 记录当前拖拽项的Y值

// 页面显示时加载数据
const loadData = async () => {
    try {
        const res = await getAssetAccounts();
        // 初始化数据，给每个项增加 y 坐标
        items.value = res.map((item, index) => ({
            ...item,
            y: index * rowHeight // 初始位置
        }));
    } catch (e) {
        console.error(e);
        uni.showToast({ title: '加载失败', icon: 'none' });
    }
};

onShow(() => {
    loadData();
});

// --- 拖拽逻辑核心 ---

// 1. 按下图标，激活拖拽
const startDrag = (id) => {
    isDragging.value = true;
    currentDragId.value = id;
};

// 2. 移动过程中记录Y值
const onMove = (e, index) => {
    // 只有在激活状态下才记录，避免初始化时的抖动
    if (isDragging.value && e.detail.source === 'touch') {
        currentY = e.detail.y;
    }
};

// 3. 松开手指，计算排序
const onDrop = () => {
    if (!isDragging.value) return;
    
    // 根据最终的 Y 值计算它应该在第几行 (四舍五入)
    let targetIndex = Math.round(currentY / rowHeight);
    
    // 边界限制
    if (targetIndex < 0) targetIndex = 0;
    if (targetIndex > items.value.length - 1) targetIndex = items.value.length - 1;

    // 找到当前正在拖拽的元素在数组中的原始索引
    const oldIndex = items.value.findIndex(i => i.id === currentDragId.value);

    if (oldIndex !== targetIndex && oldIndex !== -1) {
        // 移动数组元素
        const movingItem = items.value[oldIndex];
        items.value.splice(oldIndex, 1); // 移除旧位置
        items.value.splice(targetIndex, 0, movingItem); // 插入新位置
        
        // TODO: 调用后端 API 保存排序顺序
        // saveSortOrder(items.value);
        uni.showToast({ title: '排序已更新', icon: 'none' });
    }

    // 无论是否改变顺序，都需要重新计算所有人的 Y 值，让 UI 归位
    // 使用 nextTick 确保视图刷新
    nextTick(() => {
        items.value.forEach((item, index) => {
            item.y = index * rowHeight;
        });
        isDragging.value = false;
        currentDragId.value = null;
        currentY = 0;
    });
};

// --- 原有业务逻辑 ---

const handleUpdateName = async (id, newName) => {
    if (!newName.trim()) return;
    try {
        await updateAssetAccount(id, { name: newName });
    } catch (e) {
        uni.showToast({ title: '修改失败', icon: 'none' });
    }
};

const handleClear = (acc) => {
    if (acc.totalValue === 0) return;
    uni.showModal({
        title: '确认操作',
        content: `确定要清空【${acc.name}】的所有持仓数据吗？`,
        success: async (res) => {
            if (res.confirm) {
                try {
                    uni.showLoading({ title: '处理中...' });
                    await clearAccountHoldings(acc.id);
                    const target = items.value.find(i => i.id === acc.id);
                    if (target) target.totalValue = 0;
                    uni.showToast({ title: '已清空', icon: 'success' });
                } catch (e) {
                    uni.showToast({ title: '操作失败', icon: 'none' });
                } finally {
                    uni.hideLoading();
                }
            }
        }
    });
};

const goBack = () => {
    uni.navigateBack();
};
</script>

<style lang="scss" scoped>
.manager-page {
  min-height: 100vh;
  background-color: $bg-page;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.warning-bar {
  background-color: #fff7ed;
  padding: 12px 24px;
  font-size: 12px;
  color: #f97316;
  display: flex;
  flex-direction: row;
  align-items: center;
  z-index: 10;
}

.warning-icon { width: 14px; height: 14px; margin-right: 8px; }

.scroll-container {
  flex: 1;
  height: 0; 
  padding: 16px;
  box-sizing: border-box;
  position: relative;
}

/* 拖拽区域容器 */
.drag-area {
  width: 100%;
  /* height 由 js 动态计算 */
}

/* 可拖拽视图 */
.drag-item {
  width: 100%;
  /* height 由 js 动态计算 (rowHeight) */
  /* z-index 动态控制 */
}

.account-card {
  background-color: $bg-white;
  /* 高度需减去 margin-bottom，确保总高度等于 rowHeight */
  height: 78px; 
  margin-bottom: 12px; 
  padding: 0 16px; /* padding 上下由 height 控制垂直居中，这里只设左右 */
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  border: 1px solid $border-color;
  box-sizing: border-box;
}

.drag-handle {
  padding: 20px 16px 20px 0; /* 增大触摸热区 */
  color: $text-muted;
  display: flex;
  align-items: center;
  height: 100%;
}

.handle-icon {
  width: 20px;
  height: 20px;
  opacity: 0.5;
}

.input-section {
  flex: 1;
  margin-right: 16px;
}

.input-label {
  font-size: 10px;
  color: $text-muted;
  margin-bottom: 4px;
  display: block;
}

.name-input {
  width: 100%;
  font-weight: bold;
  color: $text-main;
  background-color: #f9fafb;
  border-bottom: 1px dashed $border-color;
  height: 32px;
  font-size: 14px;
  padding-left: 4px;
}

.name-input:focus {
    border-bottom-style: solid;
    border-color: $primary;
}

.action-section {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.action-btn {
  font-size: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid transparent;
  transition: all 0.2s;

  &.is-disabled {
    color: $text-muted;
    border-color: $border-color;
    background-color: #f3f4f6;
  }

  &.is-active {
    color: $red;
    border-color: #fee2e2;
    background-color: $bg-white;
    
    &:active {
      background-color: #fef2f2;
    }
  }
}

.footer {
  padding: 16px;
  background-color: $bg-white;
  border-top: 1px solid $border-color;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
  z-index: 99;
}

.close-btn {
  width: 100%;
  padding: 12px 0;
  background-color: #f3f4f6;
  color: $text-sub;
  border-radius: 12px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active {
    background-color: $border-color;
  }
}
</style>