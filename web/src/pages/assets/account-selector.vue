<template>
  <view class="selector-page">
    <!-- 1. 顶部搜索栏 -->
    <view class="search-header">
      <view class="search-input-box">
        <uni-icons type="search" size="18" color="#a3b0ad"></uni-icons>
        <input 
          type="text" 
          v-model="searchKeyword" 
          placeholder="搜索账户名称/机构..." 
          placeholder-class="placeholder-style"
        />
      </view>
    </view>

    <!-- 2. 账户列表区 -->
    <scroll-view class="list-scroll" scroll-y>
      <view v-for="(group, gIndex) in groupedAccounts" :key="gIndex" class="account-group">
        <text class="group-title">{{ group.title }}</text>
        
        <view 
          v-for="account in group.list" 
          :key="account.id"
          class="account-card card-warm"
          hover-class="item-active"
          @click="handleSelect(account)"
        >
          <view class="card-left">
            <view class="icon-wrapper">
              <image :src="getInstitutionIcon(account.institution)" mode="aspectFit" class="bank-icon" />
            </view>
            <view class="name-box">
              <text class="main-name">{{ account.accountName || account.institutionName }}</text>
              <text class="sub-name">{{ account.institutionIdentifier }}</text>
            </view>
          </view>
          
          <view class="card-right">
            <view class="balance-box">
              <text class="currency">￥</text>
              <text class="amount num-font">{{ formatMoney(account.totalBalance) }}</text>
            </view>
            <uni-icons type="right" size="14" color="#ccd4d2"></uni-icons>
          </view>
        </view>
      </view>
      
      <!-- 无数据占位 -->
      <view v-if="groupedAccounts.length === 0" class="empty-state">
        <image src="/static/images/empty-box.png" mode="aspectFit" class="empty-img" />
        <text>未找到相关账户</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getAccounts } from '@/services/accountService.js';
import { useConfigStore } from '@/stores/config.js';

const configStore = useConfigStore();
const allAccounts = ref([]);
const searchKeyword = ref('');

onLoad(async () => {
  await loadAccounts();
});

const loadAccounts = async () => {
  try {
    // 调用后端接口获取账户
    const res = await getAccounts();
    allAccounts.value = res.accounts || [];
  } catch (e) {
    console.error('加载账户失败:', e);
  }
};

// 格式化金额
const formatMoney = (val) => {
  return Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

// 获取机构图标（从 configStore 映射）
const getInstitutionIcon = (instCode) => {
  // 假设 store 里有 institution 映射关系
  return configStore.institutionMap[instCode]?.iconUrl || '/static/images/default-bank.png';
};

// 按类型分组并过滤搜索内容
const groupedAccounts = computed(() => {
  let list = allAccounts.value;

  // 搜索过滤
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase();
    list = list.filter(a => 
      (a.accountName && a.accountName.toLowerCase().includes(kw)) || 
      (a.institutionName && a.institutionName.toLowerCase().includes(kw))
    );
  }

  // 分组逻辑
  const groups = {
    'LIQUID': { title: '现金/第三方支付', list: [] },
    'BANK': { title: '银行借记卡', list: [] },
    'INVEST': { title: '投资/券商账户', list: [] },
    'OTHER': { title: '其他', list: [] }
  };

  list.forEach(acc => {
    // 这里的 logic 根据你后端的 account.type 或 institution 判定
    if (['ALIPAY', 'WECHAT', 'POCKET'].includes(acc.institution)) {
      groups['LIQUID'].list.push(acc);
    } else if (acc.institution.includes('BANK')) {
      groups['BANK'].list.push(acc);
    } else {
      groups['OTHER'].list.push(acc);
    }
  });

  return Object.values(groups).filter(g => g.list.length > 0);
});

// 选择并返回
const handleSelect = (account) => {
  // 使用 UniApp 的 EventChannel 向上一页发送数据
  const eventChannel = uni.getOpenerEventChannel();
  eventChannel.emit('acceptAccountFromSelector', { data: account });
  
  // 震动反馈并返回
  uni.vibrateShort();
  uni.navigateBack();
};
</script>

<style lang="scss" scoped>
.selector-page {
  height: 100vh;
  background-color: $bg-page;
  display: flex;
  flex-direction: column;
}

/* 搜索栏 */
.search-header {
  padding: 20rpx $spacing-md;
  background-color: $bg-white;
  .search-input-box {
    background-color: $bg-page;
    height: 80rpx;
    border-radius: $radius-base;
    display: flex;
    align-items: center;
    padding: 0 24rpx;
    gap: 16rpx;
    
    input {
      flex: 1;
      font-size: 28rpx;
      color: $text-main;
    }
  }
}

/* 列表区 */
.list-scroll {
  flex: 1;
  padding: 0 $spacing-md;
}

.group-title {
  display: block;
  font-size: 24rpx;
  color: $text-muted;
  margin: 40rpx 0 20rpx 10rpx;
  font-weight: $fw-medium;
}

.account-card {
  padding: 28rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  
  .card-left {
    display: flex;
    align-items: center;
    gap: 24rpx;
    
    .icon-wrapper {
      width: 80rpx;
      height: 80rpx;
      background-color: $bg-page;
      border-radius: $radius-base;
      @include flex-center;
      .bank-icon { width: 52rpx; height: 52rpx; }
    }
    
    .name-box {
      display: flex;
      flex-direction: column;
      gap: 4rpx;
      .main-name { font-size: 30rpx; color: $text-main; font-weight: $fw-semibold; }
      .sub-name { font-size: 22rpx; color: $text-muted; }
    }
  }
  
  .card-right {
    display: flex;
    align-items: center;
    gap: 12rpx;
    
    .balance-box {
      text-align: right;
      .currency { font-size: 20rpx; color: $text-muted; margin-right: 4rpx; }
      .amount { font-size: 32rpx; color: $text-sub; font-weight: $fw-bold; }
    }
  }
}

.item-active {
  background-color: $bg-subtle;
  transform: scale(0.98);
}

.empty-state {
  padding-top: 200rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: $text-placeholder;
  font-size: 26rpx;
  .empty-img { width: 240rpx; height: 240rpx; margin-bottom: 20rpx; opacity: 0.5; }
}
</style>