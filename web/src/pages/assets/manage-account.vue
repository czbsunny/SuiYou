<template>
	<view class="manage-container">
		<!-- 1. 活跃账户部分 -->
		<view class="section">
			<view class="section-header">
				<text class="title">正在展示的账户</text>
				<text class="desc">点击左侧或左滑归档，右侧手柄排序</text>
			</view>

			<!-- 🟢 活跃账户拖拽与侧滑区 -->
			<view class="drag-list-container" :style="{ height: activeAccounts.length * 160 + 'rpx' }">
				<view 
					v-for="(acc, index) in activeAccounts" 
					:key="acc.id" 
					class="drag-item-wrapper"
					:class="{ 'is-dragging': draggingId === acc.id }"
					:style="{ 
						transform: `translateY(${getTranslationY(acc.id, index)}rpx)`,
						zIndex: draggingId === acc.id ? 100 : 1
					}"
				>
					<!-- 侧滑容器 -->
					<view class="swipe-container" :class="{ 'parent-is-swiped': swipedId === acc.id }">
						<!-- 底层：归档按钮 -->
						<view class="action-layer-right" @tap="openManageModal(acc)">归档</view>
						
						<!-- 顶层：卡片内容 -->
						<view 
							class="manage-item shadow-box"
							@touchstart="handleTouchStart"
							@touchmove.stop="handleTouchMove($event, acc.id, true)"
							@touchend="handleTouchEnd"
						>
							<!-- 左侧：移除触发 -->
							<view class="left-action" @tap="toggleAction(acc.id, true)">
								<view class="icon-circle bg-red">
									<view class="minus-line"></view>
								</view>
							</view>

							<view class="item-body">
								<view class="item-logo-box">
									<image :src="acc.logoUrl" class="mini-logo" mode="aspectFit" />
								</view>
								<view class="info">
									<text class="name">{{ acc.instName }} - {{ acc.accountName }}</text>
									<text class="amount">¥{{ formatAmount(acc.totalBalance || 0) }}</text>
								</view>
							</view>

							<!-- 右侧：排序手柄 (仅在未侧滑时有效) -->
							<view class="right-action" v-if="swipedId !== acc.id">
								<view class="drag-handle" 
									@touchstart.stop="onDragStart($event, acc.id, index)"
									@touchmove.stop.prevent="onDragMove"
									@touchend.stop="onDragEnd"
								>
									<image src="/static/images/drag-handle.png" class="drag-img" mode="aspectFit" />
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 2. 已归档账户部分 -->
		<view class="section" v-if="archivedAccounts.length > 0">
			<view class="section-header"><text class="title">已归档</text></view>
			<view class="account-list">
				<view v-for="acc in archivedAccounts" :key="acc.id" class="swipe-container archived-wrapper" :class="{ 'parent-is-swiped': swipedId === acc.id }">
					<!-- 底层：恢复确认按钮 -->
					<view class="action-layer-right bg-confirm" @tap="localUpdateStatus(acc.id, 1)">确认恢复</view>
					
					<view class="manage-item archived-style" 
						@touchstart="handleTouchStart" 
						@touchmove.stop="handleTouchMove($event, acc.id, false)"
					>
						<view class="left-action" @tap="toggleAction(acc.id, false)">
							<view class="icon-circle bg-green">
								<view class="plus-v"></view><view class="plus-h"></view>
							</view>
						</view>
						<view class="item-body">
							<view class="item-logo-box grayscale">
								<image :src="acc.logoUrl" class="mini-logo" mode="aspectFit" />
							</view>
							<view class="info">
								<text class="name">{{ acc.instName }} - {{ acc.accountName }}</text>
								<text class="status">不显示在我的账户中</text>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 底部保存 -->
		<view class="footer-action">
			<button class="save-btn" @tap="submitAllChanges">保存更改并生效</button>
		</view>

		<!-- 🟢 居中对话框：严谨排版 -->
		<view class="modal-mask" v-if="showManageModal" @tap="showManageModal = false">
			<view class="center-dialog" @tap.stop>
				<view class="close-x" @tap="showManageModal = false">×</view>
				<view class="dialog-header">
					<text class="dialog-title">确认归档</text>
					<text class="dialog-desc">“{{ selectedAcc?.accountName }}”</text>
					<view class="dialog-info">
						<text>· 归档后该账户及其余额将不在首页展示</text>
						<text>· 账户数据会保留，您可以在下方“已归档”中找回</text>
						<text>· 如需彻底删除账户，请前往“账户详情”页操作</text>
					</view>
				</view>
				<view class="dialog-body-row">
					<!-- 此时只有一个主按钮，或者点击取消 -->
					<view class="row-btn primary" @tap="localUpdateStatus(selectedAcc.id, 0)">确认归档</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import accountService from '@/services/accountService.js';
import { useConfigStore } from '@/stores/config.js';

const configStore = useConfigStore();
const localAccounts = ref([]); 
const swipedId = ref(null); // 当前露出按钮的卡片ID
const selectedAcc = ref(null);
const showManageModal = ref(false);

// 排序相关
const draggingId = ref(null);
const dragY = ref(0);
const startY = ref(0);
const itemOrder = ref([]); // 活跃账户的排序ID数组

// 侧滑手势相关
const touchStartX = ref(0);

const initialArchivedIds = ref([]); 

// --- 逻辑计算 ---
const activeAccounts = computed(() => {
	const filtered = localAccounts.value.filter(a => a.status === 1);
	if (itemOrder.value.length > 0) {
		return [...filtered].sort((a, b) => itemOrder.value.indexOf(a.id) - itemOrder.value.indexOf(b.id));
	}
	return filtered;
});

const archivedAccounts = computed(() => localAccounts.value.filter(a => a.status === 0));

const loadData = async () => {
	const instMap = configStore.institutionMap;
	try {
		const res = await accountService.getAccounts();
		
		const processedAccounts = res.accounts.map(acc => ({
			...acc,
			instName: instMap[acc.institution]?.instName || '其他',
			logoUrl: instMap[acc.institution]?.logoUrl || ''
		})).sort((a, b) => {
            return a.sortOrder - b.sortOrder;
        });
		
		localAccounts.value = processedAccounts;
		
		initialArchivedIds.value = processedAccounts
			.filter(a => a.status === 0)
			.map(a => a.id);

		itemOrder.value = processedAccounts
			.filter(a => a.status === 1)
			.map(a => a.id);
            
	} catch (e) {
		console.error("加载账户数据失败:", e);
	}
};

onShow(() => loadData());

// --- 🟢 侧滑与点击触发逻辑 ---

const toggleAction = (id, isActive) => {
	if (swipedId.value === id) swipedId.value = null;
	else swipedId.value = id;
};

const handleTouchStart = (e) => {
	touchStartX.value = e.touches[0].clientX;
};

const handleTouchMove = (e, id, isActive) => {
	const moveX = e.touches[0].clientX;
	const diffX = moveX - touchStartX.value;
	
	if (diffX < -40) swipedId.value = id; // 左滑露出
	if (diffX > 40) swipedId.value = null; // 右滑隐藏
};

const handleTouchEnd = () => {};

// --- 🟢 排序逻辑 (自动避让版) ---

const onDragStart = (e, id, index) => {
	if (swipedId.value) return; // 如果有侧滑，禁止排序
	draggingId.value = id;
	startY.value = e.touches[0].clientY;
	dragY.value = 0;
	uni.vibrateShort();
};

const onDragMove = (e) => {
	if (!draggingId.value) return;
	const currentY = e.touches[0].clientY;
	dragY.value = currentY - startY.value;

	const moveIndex = Math.round(dragY.value / uni.upx2px(160));
	const currentIndex = itemOrder.value.indexOf(draggingId.value);
	let targetIndex = currentIndex + moveIndex;
	targetIndex = Math.max(0, Math.min(targetIndex, itemOrder.value.length - 1));

	if (targetIndex !== currentIndex) {
		const newOrder = [...itemOrder.value];
		const [movedId] = newOrder.splice(currentIndex, 1);
		newOrder.splice(targetIndex, 0, movedId);
		itemOrder.value = newOrder;
		startY.value += (targetIndex - currentIndex) * uni.upx2px(160);
		dragY.value = 0;
	}
};

const onDragEnd = () => {
	draggingId.value = null;
};

const getTranslationY = (id, index) => {
	if (id === draggingId.value) return (index * 160) + (dragY.value * (750 / uni.getSystemInfoSync().windowWidth));
	return index * 160;
};

// --- 🟢 数据操作 ---

const openManageModal = (acc) => {
	selectedAcc.value = acc;
	showManageModal.value = true;
};

const submitAllChanges = async () => {
    uni.showLoading({ title: '保存中', mask: true });
    try {
        const currentArchivedIds = localAccounts.value
            .filter(a => a.status === 0)
            .map(a => a.id);

        const newlyArchivedIds = currentArchivedIds.filter(
            id => !initialArchivedIds.value.includes(id)
        );

        const payload = {
            activeAccountIds: [...itemOrder.value],
            archivedAccountIds: newlyArchivedIds
        };

        console.log('提交载荷:', payload);

        await accountService.batchUpdateAccounts(payload);
        
        uni.hideLoading();
        uni.showToast({ title: '配置已生效' });
        uni.$emit('refreshAccountList');
        setTimeout(() => uni.navigateBack(), 1000);
    } catch (e) {
        uni.hideLoading();
        uni.showToast({ title: '同步失败', icon: 'none' });
    }
};

const localUpdateStatus = (id, status) => {
    const targetIdx = localAccounts.value.findIndex(a => a.id === id);
    if (targetIdx !== -1) {
        localAccounts.value[targetIdx].status = status;
        
        if (status === 1) {
            // 恢复：加入活跃排序列表末尾
            if (!itemOrder.value.includes(id)) {
                itemOrder.value.push(id);
            }
        } else {
            // 归档：从活跃排序列表中剔除
            itemOrder.value = itemOrder.value.filter(i => i !== id);
        }
    }
    showManageModal.value = false;
    swipedId.value = null;
    uni.vibrateShort();
};

const formatAmount = (val) => Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
</script>

<style lang="scss" scoped>
.manage-container {
	min-height: 100vh; background-color: #F9F8F4; padding: 30rpx 32rpx 240rpx;
}

/* 🟢 核心排序容器：相对定位 */
.drag-list-container { position: relative; width: 100%; }

.drag-item-wrapper {
	position: absolute; left: 0; top: 0; width: 100%; height: 140rpx;
	transition: transform 0.3s cubic-bezier(0.2, 1, 0.2, 1);
	&.is-dragging { transition: none; } 
}

/* 🟢 侧滑容器 */
.swipe-container {
	position: relative; width: 100%; height: 140rpx; overflow: hidden; border-radius: 32rpx;
	background-color: transparent;
	
	&.archived-wrapper { margin-bottom: 24rpx; }
}

.manage-item {
	position: absolute; left: 0; top: 0; width: 100%; height: 100%;
	display: flex; align-items: center; padding: 0 32rpx; background: #fff;
	border-radius: 32rpx; transition: transform 0.3s cubic-bezier(0.25, 1, 0.5, 1);
	z-index: 2; box-sizing: border-box;
	
	/* 侧滑位移 */
		.parent-is-swiped & { transform: translateX(-160rpx); }
}

/* 🟢 底部操作按钮：位于卡片下方 */
.action-layer-right {
	position: absolute; right: 0; top: 0; width: 160rpx; height: 100%;
	background-color: #FF3B30; color: #fff; font-size: 28rpx; font-weight: 700;
	display: flex; align-items: center; justify-content: center; z-index: 1;
	border-radius: 0 32rpx 32rpx 0;
	
	&.bg-confirm { background-color: #34C759; }
}

.section { margin-bottom: 50rpx; }
.section-header {
	margin-bottom: 24rpx; padding-left: 10rpx;
	.title { font-size: 30rpx; font-weight: 800; color: #1F2937; }
	.desc { font-size: 24rpx; color: #9CA3AF; margin-top: 6rpx; margin-left: 4px; }
}

.manage-item {
	.left-action {
		padding-right: 24rpx;
		.icon-circle {
			width: 44rpx; height: 44rpx; border-radius: 50%;
			display: flex; align-items: center; justify-content: center; position: relative;
			&.bg-red { background-color: #FF3B30; }
			&.bg-green { background-color: #34C759; }
			.minus-line { width: 18rpx; height: 4rpx; background: #fff; border-radius: 2rpx; }
			.plus-v { width: 4rpx; height: 18rpx; background: #fff; position: absolute; }
			.plus-h { width: 18rpx; height: 4rpx; background: #fff; position: absolute; }
		}
	}
	.item-body {
		flex: 1; display: flex; align-items: center; gap: 20rpx; overflow: hidden;
		.item-logo-box {
			width: 68rpx; height: 68rpx; background: #F9FAFB;
			display: flex; align-items: center; justify-content: center;
			&.grayscale { filter: grayscale(1); opacity: 0.4; }
			.mini-logo { width: 68rpx; height: 68rpx; border-radius: 16rpx; }
		}
		.info { flex: 1; .name { font-size: 28rpx; font-weight: 700; color: #374151; display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; } .amount { font-size: 24rpx; color: #9CA3AF; font-family: 'DIN Alternate'; } .status { font-size: 22rpx; color: #D1D5DB; } }
	}
	.right-action { .drag-handle { padding-left: 20rpx; .drag-img { width: 40rpx; height: 40rpx; opacity: 0.2; } } }
}

.shadow-box { box-shadow: 0 4rpx 24rpx rgba(0,0,0,0.03); }

.archived-style {
  background-color: #F2F2F7 !important; 
  opacity: 1 !important;
  border-radius: 32rpx;
  
  .item-logo-box {
    &.grayscale { 
      filter: grayscale(1); 
      opacity: 0.3; 
    }
  }

  .info {
    .name { 
      text-decoration: line-through; 
      color: #9CA3AF !important; 
      font-weight: 500 !important; 
    }
    .status { 
      color: #D1D5DB; 
    }
  }
  
  &.parent-is-swiped {
    background-color: #F2F2F7 !important;
  }
}

/* 🟢 对话框样式 */
.modal-mask {
	position: fixed; inset: 0; background: rgba(0,0,0,0.4);
	display: flex; align-items: center; justify-content: center; z-index: 999;
}
.center-dialog {
	width: 600rpx; background: #fff; border-radius: 48rpx; padding: 48rpx; position: relative;
	.close-x { position: absolute; right: 30rpx; top: 20rpx; font-size: 56rpx; color: #D1D5DB; line-height: 60rpx; }
	.dialog-header {
		text-align: center; margin: 20rpx 0 50rpx;
		.dialog-title { font-size: 34rpx; font-weight: 800; color: #1F2937; display: block; }
		.dialog-desc { font-size: 28rpx; color: #4B5563; font-weight: 600; margin-top: 10rpx; display: block; }
		.dialog-info { margin-top: 32rpx; padding: 24rpx; background: #F9FAFB; border-radius: 20rpx; text { display: block; font-size: 22rpx; color: #9CA3AF; line-height: 1.6; text-align: left; } }
	}
	.dialog-body-row {
		display: flex; gap: 24rpx;
		.row-btn {
			flex: 1; height: 100rpx; border-radius: 28rpx; display: flex; align-items: center; justify-content: center; font-size: 30rpx; font-weight: 800;
			&.primary { background: #F0F9F6; color: #2D7A68; }
		}
	}
}

.footer-action {
	position: fixed; bottom: 0; left: 0; right: 0; padding: 40rpx 48rpx calc(40rpx + env(safe-area-inset-bottom));
	background: linear-gradient(to top, #F9F8F4 85%, transparent);
	z-index: 100;
	.save-btn { height: 108rpx; background: #1F2937; color: #fff; border-radius: 32rpx; font-size: 30rpx; font-weight: 700; display: flex; align-items: center; justify-content: center; }
}
</style>