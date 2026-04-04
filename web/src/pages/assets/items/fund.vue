<template>
  <view class="main-container">
    <!-- 1. 资产汇总区域 -->
    <AssetSummary 
      :asset="selectedAsset"
      :dailyReturn="selectedDailyReturn"
      :dailyReturnRate="selectedDailyReturnRate"
      @assetVisibilityChanged="handleAssetVisibilityChanged"
    />

    <!-- 2. 滚动内容区 -->
    <scroll-view 
      class="content-scroll" 
      scroll-y
      enable-back-to-top
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="handlePullDownRefresh"
      :style="{ height: scrollHeight }"
    >
      <!-- 单个账户详情页 -->
      <SingleAccountPage 
        ref="SingleAccountPage"
        :selectedAccount="selectedAccount" 
        :accountData="getCurrentAccountData()" 
        :isHoldingsLoading="holdingsLoading" 
        :isAssetHidden="isAssetHidden" 
        @refreshHoldings="handleRefreshHoldings" 
        @longpress="showActionMenu"
      />
      
      <view class="safe-area-bottom" style="height: 40rpx;"></view>
    </scroll-view>
    
    <ActionMenu 
      :visible="isShowActionMenu"
      :item="selectedHoldingItem"
      :menuStyle="actionMenuStyle"
      @hide="hideActionMenu"
      @edit="handleEditFund"
      @delete="handleDeleteFund"
    />
  </view>
</template>

<script>
import AssetSummary from '@/components/assets/fund/AssetSummary.vue';
import SingleAccountPage from '@/components/assets/fund/SingleAccountPage.vue';
import ActionMenu from '@/components/assets/fund/ActionMenu.vue';
import { getAssetAccounts, calculateSummaryData as calcSummary, getAccountById, getAccountHoldings, deleteHolding } from '@/services/fund.js';

export default {
  components: { AssetSummary, SingleAccountPage, ActionMenu },
  data() {
    return {
      accounts: [],
      accountHoldings: {},
      holdingsLoading: false,
      isLoading: false,
      isRefreshing: false,
      selectedAccount: -1,
      isAssetHidden: false,
      
      // 汇总数据容器
      totalAsset: 0, dailyReturn: 0, dailyReturnRate: 0, totalReturn: 0, totalReturnRate: 0,
      
      // 当前显示数据容器
      selectedAsset: 0, selectedDailyReturn: 0, selectedDailyReturnRate: 0, selectedTotalReturn: 0, selectedTotalReturnRate: 0,
      
      // 操作菜单相关
      isShowActionMenu: false,
      selectedHoldingItem: null,
      actionMenuStyle: {},
      
      scrollHeight: 'calc(100vh - 400rpx)'
    }
  },
  onLoad(options) {
    if (options.data) {
      const item = JSON.parse(decodeURIComponent(options.data));
      console.log('解析出的资产:', item);
      // 这里可以根据需要处理资产对象
    }
    if (options.name) uni.setNavigationBarTitle({ title: options.name });
    
    this.loadData();
    // 监听全局刷新事件
    uni.$on('refreshHoldings', (accountId) => {
      this.handleRefreshHoldings(accountId || this.selectedAccount);
    });
  },
  onShow() {    
    this.loadData();
  },
  onUnload() {
    uni.$off('refreshHoldings');
  },
  mounted() {
    this.calculateScrollHeight();
  },
  methods: {
    calculateScrollHeight() {
      const windowInfo = uni.getWindowInfo();
      this.scrollHeight = (windowInfo.windowHeight - uni.upx2px(450)) + 'px';
    },

    showActionMenu({ item, index }) {
      this.selectedHoldingItem = item;

      const query = uni.createSelectorQuery();
      
      if (this.$refs.SingleAccountPage) {
        query.in(this.$refs.SingleAccountPage);
      }

      query.select('.item-idx-' + index).boundingClientRect(rect => {
        if (rect) {
          const topFixedAreaHeight = uni.upx2px(300);
          
          let topPos = rect.top + rect.height + 10; // 项下方10px处
          
          const windowInfo = uni.getWindowInfo();
          const viewportHeight = windowInfo.windowHeight;
          const menuHeight = uni.upx2px(200); // 预估菜单高度
          
          // 如果菜单会超出底部，则显示在项的上方
          if (topPos + menuHeight > viewportHeight) {
            topPos = rect.top - menuHeight - 10;
          }
          
          if (topPos < topFixedAreaHeight) {
            topPos = topFixedAreaHeight + 10;
          }

          this.actionMenuStyle = {
            top: topPos + 'px',
            left: '50%',
            transform: 'translateX(-50%)',
            width: '560rpx'
          };
          this.isShowActionMenu = true;
          uni.vibrateShort();
        }
      }).exec();
    },

    hideActionMenu() {
      this.isShowActionMenu = false;
      this.selectedHoldingItem = null;
      this.actionMenuStyle = {};
    },

    handleEditFund(item) {
      uni.navigateTo({ 
        url: `/pages/assets/fund/edit?assetItemId=${item.id}&fundInfo=${encodeURIComponent(JSON.stringify(item))}`
      });
      this.hideActionMenu();
    },

    async handleDeleteFund(item) {
      const res = await uni.showModal({ title: '确认', content: '删除该持仓？', confirmColor: '#F24E4E' });
      if (res.confirm) { 
        await deleteHolding(this.selectedAccount, item.id); 
        // 刷新持仓数据
        await this.loadAccountHoldings(this.selectedAccount);
        // 更新账户汇总数据
        this.updateAccountSummary(this.selectedAccount);
      }
      this.hideActionMenu();
    },

    async loadData() {
      this.isLoading = true;
      try {
        const res = await getAssetAccounts();
        this.accounts = res.sort((a, b) => a.sortOrder - b.sortOrder);
        // 默认选择第一个账户
        if (this.accounts.length > 0 && this.selectedAccount === -1) {
          this.selectedAccount = this.accounts[0].id;
        }
        this.calculateSummaryData();
        if (this.selectedAccount !== -1) {
          await this.loadAccountHoldings(this.selectedAccount);
        }
      } catch (error) {
        console.error('Data Load Error:', error);
      } finally {
        this.isLoading = false;
      }
    },

    async handlePullDownRefresh() {
      this.isRefreshing = true;
      await this.loadData();
      this.isRefreshing = false;
      uni.showToast({ title: '数据已更新', icon: 'none' });
    },
    
    calculateSummaryData() {
      const summary = calcSummary(this.accounts);
      Object.assign(this, {
        totalAsset: summary.totalAsset,
        dailyReturn: summary.dailyReturn,
        dailyReturnRate: summary.dailyReturnRate,
        totalReturn: summary.totalReturn,
        totalReturnRate: summary.totalReturnRate
      });
      this.updateSelectedAccountData();
    },
    
    updateSelectedAccountData() {
      if (this.selectedAccount === -1) {
        this.selectedAsset = this.totalAsset;
        this.selectedDailyReturn = this.dailyReturn;
        this.selectedDailyReturnRate = this.dailyReturnRate;
        this.selectedTotalReturn = this.totalReturn;
        this.selectedTotalReturnRate = this.totalReturnRate;
      } else {
        const account = getAccountById(this.accounts, this.selectedAccount);
        if (account) {
          this.selectedAsset = account.asset;
          this.selectedDailyReturn = account.dailyReturn;
          this.selectedDailyReturnRate = account.dailyReturnRate;
          this.selectedTotalReturn = account.totalReturn;
          this.selectedTotalReturnRate = account.totalReturnRate;
        }
      }
    },
    
    async loadAccountHoldings(accountId) {
      if (accountId === -1) return;
      try {
        this.holdingsLoading = true;
        const holdings = await getAccountHoldings(accountId);
        this.$set(this.accountHoldings, accountId, holdings);
      } finally {
        this.holdingsLoading = false;
      }
    },
    
    async handleRefreshHoldings(accountId) {
      await this.loadAccountHoldings(accountId);
      this.updateAccountSummary(accountId);
    },
    
    updateAccountSummary(accountId) {
      const holdings = this.accountHoldings[accountId] || [];
      const account = getAccountById(this.accounts, accountId);
      if (account) {
        const total = holdings.reduce((s, i) => s + (i.amount || 0), 0);
        account.asset = total;
        // ... 其他收益计算逻辑保持 ...
        this.calculateSummaryData();
      }
    },

    handleAssetVisibilityChanged(isHidden) {
      this.isAssetHidden = isHidden;
    },
    
    getCurrentAccountData() {
      const account = getAccountById(this.accounts, this.selectedAccount);
      return account ? { ...account, holdings: this.accountHoldings[this.selectedAccount] || [] } : null;
    }
  }
}
</script>

<style lang="scss" scoped>
.main-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: $bg-page;
  overflow: hidden;
}

.content-scroll {
  flex: 1;
  position: relative;
  /* 针对下拉刷新圆圈颜色的微调（H5端生效） */
  &::v-deep .uni-scroll-view-refresh-inner {
    stroke: $primary;
  }
}
</style>