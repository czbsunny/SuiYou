<template>
  <view class="edit-account-page" v-if="!loading">
    <scroll-view scroll-y class="scroll-content">
      <!-- 机构信息卡片 -->
      <view class="institution-info-card">
        <view class="institution-header">
          <view class="inst-icon">
            <image :src="formatLogoUrl(institution?.logoUrl)" class="inst-icon-img" mode="aspectFit" />
          </view>
          <view class="inst-details">
            <text class="inst-label">所属机构</text>
            <text class="inst-name">{{ institutionName }}</text>
          </view>
        </view>
      </view>

      <!-- 表单 -->
      <view class="form-section">
        <view class="form-item">
          <text class="form-label">账号 / 卡号<text class="required-mark">*</text></text>
          <input
            class="form-input"
            v-model="accountForm.accountNo"
            placeholder="如卡号/手机号后四位"
          />
        </view>

        <view class="form-item">
          <text class="form-label">账户名称<text class="required-mark">*</text></text>
          <input
            class="form-input"
            v-model="accountForm.accountName"
            placeholder="例如 招商银行工资卡"
          />
        </view>

        <view class="form-item">
          <text class="form-label">账户类型</text>
          <view class="form-input readonly-input">{{ accountTypeName }}</view>
        </view>

        <view class="visibility-section">
          <text class="form-label">可见范围</text>
          <view class="visibility-options">
            <view class="visibility-option" @tap="accountForm.visibility = 'PRIVATE'">
              <view class="radio-wrap">
                <view class="custom-radio" :class="{ checked: accountForm.visibility === 'PRIVATE' }"></view>
              </view>
              <text class="option-text">仅自己可见</text>
            </view>
            <view class="visibility-option" @tap="accountForm.visibility = 'FAMILY'">
              <view class="radio-wrap">
                <view class="custom-radio" :class="{ checked: accountForm.visibility === 'FAMILY' }"></view>
              </view>
              <text class="option-text">家庭成员可见</text>
            </view>
          </view>
        </view>

        <view class="networth-section">
          <view class="networth-info">
            <text class="networth-title">计入净值</text>
            <text class="networth-desc">将此账户金额计入个人资产总额</text>
          </view>
          <view
            class="switch-wrap"
            :class="{ checked: accountForm.includeInNetworth }"
            @tap="accountForm.includeInNetworth = !accountForm.includeInNetworth"
          >
            <view class="switch-thumb"></view>
          </view>
        </view>
      </view>

      <!-- 资产模块配置 -->
      <view class="assets-section">
        <view class="section-header">
          <image src="/static/images/grid_view.png" class="section-icon" mode="aspectFit" />
          <text class="section-title">资产模块配置</text>
        </view>

        <view v-if="requiredAssets.length > 0" class="asset-group">
          <text class="group-label">必选模块</text>
          <view class="asset-list">
            <view
              v-for="asset in requiredAssets"
              :key="asset.assetType"
              class="asset-card required-card"
            >
              <view class="asset-icon-wrap primary-bg">
                <image :src="asset.iconUrl || '/static/images/default_icon.png'" class="asset-icon" mode="aspectFit" />
              </view>
              <view class="asset-info">
                <view class="asset-name-row">
                  <text class="asset-name">{{ asset.assetName }}</text>
                  <view class="asset-tag required-tag">必选</view>
                </view>
              </view>
              <image src="/static/images/lock.png" class="asset-lock" mode="aspectFit" />
            </view>
          </view>
        </view>

        <view v-if="optionalAssets.length > 0" class="asset-group">
          <text class="group-label">可选模块</text>
          <view class="asset-list">
            <view
              v-for="asset in optionalAssets"
              :key="asset.assetType"
              class="asset-card"
              @tap="handleAssetTap(asset)"
            >
              <view class="asset-icon-wrap default-bg">
                <image :src="asset.iconUrl || '/static/images/default_icon.png'" class="asset-icon" mode="aspectFit" />
              </view>
              <view class="asset-info">
                <view class="asset-name-row">
                  <text class="asset-name">{{ asset.assetName }}</text>
                  <view class="asset-tag optional-tag">可选</view>
                </view>
              </view>
              <view class="asset-checkbox" :class="{ checked: getAssetChecked(asset) }">
                <view v-if="getAssetChecked(asset)" class="check-icon">✓</view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="bottom-content bottom-row">
        <button class="delete-btn bottom-delete-btn" @tap="handleDelete" :disabled="deleting">
          <text class="delete-text">删除</text>
        </button>
        <button class="confirm-btn bottom-save-btn" @tap="handleSave" :disabled="!canSubmit || submitting">
          <text class="btn-text">{{ submitting ? '修改中...' : '修改' }}</text>
        </button>
      </view>
    </view>
  </view>

  <!-- 加载中 -->
  <view class="loading-wrap" v-else>
    <text class="loading-text">加载中...</text>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getAccountById, getInstitutionDetail, getAccountAssets, updateAccount, deleteAccount } from '@/api/modules/asset'

const accountId = ref('')
const loading = ref(false)
const submitting = ref(false)
const deleting = ref(false)

const institution = ref(null)
const accountData = ref(null)
const accountTypesRaw = ref([])
const accountTypeName = ref('')

const accountForm = ref({
  accountNo: '',
  accountName: '',
  visibility: 'PRIVATE',
  includeInNetworth: true
})

const institutionAssets = ref([])
const selectedAssets = ref([])
// 保存原始选中的资产，用于检测变更
const originalSelectedAssets = ref([])

const requiredAssets = computed(() =>
  institutionAssets.value.filter(m => m.required).sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
)
const optionalAssets = computed(() =>
  institutionAssets.value.filter(m => !m.required).sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
)

const institutionName = computed(() => {
  if (institution.value) {
    return institution.value.instName || institution.value.shortName || '未知机构'
  }
  return '未知机构'
})

const formatLogoUrl = (url) => {
  if (!url) return ''
  let formattedUrl = url.startsWith('/') ? url.substring(1) : url
  return '/' + formattedUrl
}

const canSubmit = computed(() => {
  return accountForm.value.accountName.trim().length > 0
      && accountForm.value.accountNo.trim().length > 0
})

const getAssetChecked = (asset) => {
  if (asset.required) return true
  const key = asset.assetType || asset.id
  return selectedAssets.value.includes(key)
}

const handleAssetTap = (asset) => {
  const key = asset.assetType || asset.id
  const index = selectedAssets.value.indexOf(key)
  if (index > -1) {
    selectedAssets.value.splice(index, 1)
  } else {
    selectedAssets.value.push(key)
  }
}

// 检查是否有移除资产的操作
const hasRemovedAssets = () => {
  if (originalSelectedAssets.value.length === 0) return false
  // 检查原始选中的资产中，有哪些现在没选中了
  return originalSelectedAssets.value.some(key => !selectedAssets.value.includes(key))
}

const handleSave = async () => {
  if (!canSubmit.value) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }

  // 如果有移除资产模块，弹窗确认
  if (hasRemovedAssets()) {
    uni.showModal({
      title: '确认修改',
      content: '您移除了部分资产模块，确认要修改账户信息吗？',
      success: async (res) => {
        if (res.confirm) {
          await doSave()
        }
      }
    })
  } else {
    await doSave()
  }
}

const doSave = async () => {
  submitting.value = true
  try {
    const assets = [
      ...requiredAssets.value.map(a => ({ assetType: a.assetType, assetName: a.assetName })),
      ...optionalAssets.value
        .filter(a => getAssetChecked(a))
        .map(a => ({ assetType: a.assetType, assetName: a.assetName }))
    ].filter(a => a.assetType)

    const resp = await updateAccount({
      accountId: accountId.value,
      accountNo: accountForm.value.accountNo,
      accountName: accountForm.value.accountName,
      includeInNetWorth: accountForm.value.includeInNetworth,
      assets: assets
    })

    if (resp?.statusCode === 200) {
      uni.showToast({ title: '修改成功', icon: 'success' })
      uni.$emit('refreshAccountDetail')
      setTimeout(() => {
        uni.navigateBack()
      }, 1200)
    } else {
      uni.showToast({
        title: resp?.data?.message || '修改失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('修改账户失败:', error)
    uni.showToast({ title: '修改失败，请稍后重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const handleDelete = () => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该账户吗？删除后数据不可恢复。',
    success: async (res) => {
      if (res.confirm) {
        deleting.value = true
        try {
          const resp = await deleteAccount(accountId.value)
          if (resp?.statusCode === 204) {
            uni.showToast({ title: '删除成功', icon: 'success' })
            setTimeout(() => {
              uni.switchTab({ url: '/pages/asset/index' })
            }, 1200)
          } else {
            uni.showToast({
              title: resp?.data?.message || '删除失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('删除账户失败:', error)
          uni.showToast({ title: '删除失败，请稍后重试', icon: 'none' })
        } finally {
          deleting.value = false
        }
      }
    }
  })
}

const loadInstitution = async (instCode, accType) => {
  if (!instCode) return

  try {
    const res = await getInstitutionDetail(instCode)
    if (res && res.data) {
      institution.value = res.data
      const raw = res.data.accountTypes || []
      accountTypesRaw.value = raw
      // 找到当前账户类型的显示名
      const found = raw.find(t => t.accountType === accType)
      accountTypeName.value = found?.accountTypeName || accType || '未知类型'
    }
  } catch (error) {
    console.error('加载机构信息失败:', error)
  }
}

const loadAssets = async (instCode, accType) => {
  if (!instCode || !accType) return

  try {
    const res = await getAccountAssets(instCode, accType)
    if (res && res.data) {
      institutionAssets.value = res.data
    }
  } catch (error) {
    console.error('加载账户资产失败:', error)
  }
}

onLoad((options) => {
  if (options.accountId) {
    accountId.value = options.accountId
  }
})

onMounted(async () => {
  if (!accountId.value) {
    uni.showToast({ title: '缺少账户信息', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 500)
    return
  }

  loading.value = true
  try {
    // 加载账户详情
    const res = await getAccountById(accountId.value)
    if (res?.statusCode === 200 && res.data) {
      const data = res.data
      accountData.value = data

      accountForm.value.accountNo = data.accountNumber || data.accountNo || ''
      accountForm.value.accountName = data.accountName || ''
      accountForm.value.visibility = data.visibility || 'PRIVATE'
      accountForm.value.includeInNetworth = data.includeInNetWorth !== undefined ? data.includeInNetWorth : true

      const instCode = data.instCode || ''
      const accType = data.accountType || ''

      // 并行加载机构和资产信息
      await Promise.all([
        loadInstitution(instCode, accType),
        loadAssets(instCode, accType)
      ])

      // 初始化已选资产
      if (data.assets && data.assets.length > 0) {
        const initialAssets = data.assets
          .filter(a => !a.required)
          .map(a => a.assetType || a.id)
        selectedAssets.value = [...initialAssets]
        originalSelectedAssets.value = [...initialAssets]
      }
    } else {
      uni.showToast({ title: '获取账户详情失败', icon: 'none' })
      setTimeout(() => uni.navigateBack(), 500)
    }
  } catch (error) {
    console.error('加载账户数据失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.edit-account-page {
  min-height: 100vh;
  background: $background;
}

.loading-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-text {
  font-size: 28rpx;
  color: $outline;
}

.scroll-content {
  height: 100vh; 
  box-sizing: border-box;
  padding-bottom: calc($spacing-8 + env(safe-area-inset-bottom)); 
}

.institution-info-card {
  padding: 32rpx;
}

.institution-header {
  display: flex;
  align-items: center;
  padding: $spacing-4;
  background: #FFFFFF;
  border-radius: $rounded-lg;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.inst-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: $rounded-half;
  background: $surface-container;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.inst-icon-img {
  width: 80rpx;
  height: 80rpx;
  border-radius: $rounded-default;
}

.inst-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.inst-label {
  font-size: 22rpx;
  font-weight: 700;
  color: $outline;
  letter-spacing: 0.8rpx;
}

.inst-name {
  margin-top: 4rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: $on-surface;
}

.form-section {
  background: #FFFFFF;
  border-radius: $spacing-4;
  margin: 0 32rpx;
  padding: 48rpx;
  box-shadow: $shadow-soft;
}

.form-item {
  margin-bottom: 40rpx;
}

.form-label {
  display: block;
  font-size: 24rpx;
  font-weight: 600;
  color: $outline;
  margin-bottom: 16rpx;
  padding-left: 8rpx;
  letter-spacing: 2rpx;
}

.required-mark {
  color: $error;
  margin-left: 4rpx;
}

.form-input {
  width: 100%;
  height: 84rpx;
  padding: 0 32rpx;
  background: $surface-container-low;
  border: none;
  border-radius: $rounded-full;
  font-size: 28rpx;
  color: $on-surface;
  box-sizing: border-box;
  outline: none;
  transition: box-shadow $transition-base;
  line-height: 84rpx;

  &:focus {
    box-shadow: inset 0 0 0 4rpx rgba($primary, 0.2);
  }

  &::placeholder {
    color: $outline-variant;
  }
}

.readonly-input {
  display: flex;
  align-items: center;
  color: $on-surface;
  opacity: 0.7;
}

.visibility-section {
  padding-top: 16rpx;
}

.visibility-options {
  display: flex;
  gap: 80rpx;
}

.visibility-option {
  display: flex;
  align-items: center;
  gap: 12rpx;
  cursor: pointer;
}

.radio-wrap {
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.custom-radio {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid $outline;
  border-radius: $rounded-half;
  position: relative;
  transition: all $transition-base;

  &.checked {
    border-color: $primary;
    background: $primary;

    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 12rpx;
      height: 12rpx;
      background: #FFFFFF;
      border-radius: $rounded-half;
    }
  }
}

.option-text {
  font-size: 24rpx;
  color: $on-surface;
}

.networth-section {
  margin-top: 32rpx;
  padding-top: 32rpx;
  border-top: 1rpx solid $surface-container-high;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.networth-info {
  flex: 1;
}

.networth-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $on-surface;
  display: block;
}

.networth-desc {
  margin-top: 4rpx;
  font-size: 24rpx;
  color: $outline;
  display: block;
}

.switch-wrap {
  position: relative;
  width: 88rpx;
  height: 48rpx;
  border-radius: $rounded-md;
  background: $outline-variant;
  cursor: pointer;
  transition: background $transition-base;

  &.checked {
    background: $primary;
  }

  .switch-thumb {
    position: absolute;
    top: 4rpx;
    left: 4rpx;
    width: 40rpx;
    height: 40rpx;
    border-radius: $rounded-half;
    background: #FFFFFF;
    transition: transform $transition-base;
  }

  &.checked .switch-thumb {
    transform: translateX(40rpx);
  }
}

.asset-section {
  padding: 48rpx 32rpx 64rpx;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 32rpx;
}

.section-icon {
  width: 40rpx;
  height: 40rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: $on-surface;
}

.asset-group {
  margin-bottom: 40rpx;
}

.group-label {
  display: block;
  font-size: 22rpx;
  font-weight: 700;
  color: $outline;
  letter-spacing: 0.8rpx;
  text-transform: uppercase;
  margin-bottom: 20rpx;
  padding-left: 8rpx;
}

.asset-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.asset-card {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #FFFFFF;
  border-radius: $rounded-lg;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.02);
  cursor: pointer;
  transition: all $transition-base;

  &.required-card {
    background: $surface-container-low;
    border: 1rpx solid rgba($outline-variant, 0.3);
  }

  &.disabled {
    opacity: 0.7;
  }

  &:active {
    transform: scale(0.99);
  }
}

.asset-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: $rounded-md;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;

  &.primary-bg {
    background: rgba($primary, 0.1);
  }

  &.secondary-bg {
    background: rgba($secondary, 0.1);
  }

  &.tertiary-bg {
    background: rgba($tertiary-container, 0.3);
  }

  &.default-bg {
    background: $surface-container;
  }
}

.asset-icon {
  width: 40rpx;
  height: 40rpx;
}

.asset-info {
  flex: 1;
}

.asset-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.asset-name {
  font-size: 28rpx;
  font-weight: 700;
  color: $on-surface;
}

.asset-tag {
  padding: 4rpx 12rpx;
  border-radius: $rounded-sm;
  font-size: 20rpx;
  font-weight: 600;

  &.required-tag {
    background: rgba($outline-variant, 0.3);
    color: $outline;
  }

  &.default-tag {
    background: rgba($primary, 0.1);
    color: $primary;
  }

  &.optional-tag {
    background: $surface-container;
    color: $outline;
  }
}

.asset-lock {
  width: 48rpx;
  height: 48rpx;
  opacity: 0.6;
}

.asset-checkbox {
  width: 40rpx;
  height: 40rpx;
  border: 3rpx solid $outline-variant;
  border-radius: $rounded-half;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all $transition-base;

  &.checked {
    background: $primary;
    border-color: $primary;

    .check-icon {
      color: #FFFFFF;
      font-size: 24rpx;
      font-weight: 700;
    }
  }
}

.delete-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  width: 100%;
  height: 88rpx;
  background: #FFFFFF;
  border: 1rpx solid rgba($error, 0.3);
  border-radius: $rounded-full;
  transition: all $transition-base;

  &:active {
    background: rgba($error, 0.05);
  }

  &:disabled {
    opacity: 0.5;
  }
}

.delete-text {
  font-size: 28rpx;
  font-weight: 600;
  color: $error;
}

.bottom-bar {
  position: fixed;
  bottom: -2rpx; /* 微调向下偏移，遮挡边缘空隙 */
  left: 0;
  right: 0;

  padding: $spacing-4 $spacing-4 calc(constant(safe-area-inset-bottom));
  padding: $spacing-4 $spacing-4 calc(env(safe-area-inset-bottom));
  background: $surface-container-lowest;
  z-index: 100;
}

.bottom-content {
  max-width: 720rpx;
  margin: 0 auto;
}

.bottom-row {
  display: flex;
  gap: 24rpx;
}

.bottom-delete-btn {
  flex: 1;
}

.bottom-save-btn {
  flex: 1;
}

.confirm-btn {
  width: 100%;
  height: 88rpx;
  padding: 0 32rpx;
  background-color: $primary !important;
  border-radius: $rounded-full;
  border: none;
  display: flex;
  justify-content: center;
  align-items: center;

  &:active {
    transform: scale(0.98);
  }

  &:disabled {
    opacity: 0.6;
  }
}

.btn-text {
  font-size: 32rpx;
  font-weight: 700;
  color: $on-primary;
}
</style>