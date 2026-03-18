<template>
  <view v-if="visible" class="menu-mask" @tap="onHide" @touchmove.stop.prevent>
    <view class="menu-card" :style="menuStyle" @tap.stop>
      <view class="menu-header">
        <text class="m-name truncate">{{ item?.name }}</text>
      </view>
      <view class="menu-body">
        <view class="menu-option" @tap="onEdit" hover-class="hover-grey">
          <image class="m-icon" src="/static/images/edit_fund.png" />
          <text>修改持仓</text>
        </view>
        <view class="menu-option delete" @tap="onDelete" hover-class="hover-grey">
          <image class="m-icon" src="/static/images/del-fund.png" />
          <text>删除基金</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'ActionMenu',
  props: {
    visible: { type: Boolean, default: false },
    item: { type: Object, default: null },
    menuStyle: { type: Object, default: () => ({}) }
  },
  watch: {
    visible(newVal) {
      console.log('ActionMenu visible changed:', newVal);
      console.log('ActionMenu item:', this.item);
      console.log('ActionMenu menuStyle:', this.menuStyle);
    }
  },
  emits: ['hide', 'edit', 'delete'],
  methods: {
    onHide() { 
      console.log('ActionMenu onHide called');
      this.$emit('hide'); 
    },
    onEdit() { 
      console.log('ActionMenu onEdit called');
      this.$emit('edit', this.item); 
    },
    onDelete() { 
      console.log('ActionMenu onDelete called');
      this.$emit('delete', this.item); 
    }
  }
}
</script>

<style lang="scss" scoped>
.menu-mask {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0, 0, 0, 0.2); 
  backdrop-filter: blur(2px);
  z-index: 9999;
}
.menu-card {
  position: fixed;
  z-index: 10000; overflow: hidden;
  background: #fff; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
  border-radius: 16rpx;
}
.menu-header {
  padding: 24rpx; background: #f9f9f9; border-bottom: 1rpx solid #eee;
  .m-name { font-size: 26rpx; color: #333; font-weight: 600; text-align: center; }
}
.menu-body {
  display: flex;
  .menu-option {
    flex: 1; display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 32rpx 0;
    color: #666; font-size: 26rpx;
    .m-icon { width: 44rpx; height: 44rpx; margin-bottom: 12rpx; }
    &.delete { color: #F24E4E; }
  }
}
.truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>