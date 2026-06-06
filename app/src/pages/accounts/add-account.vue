
<template>
  <view class="add-account-page">
    <scroll-view scroll-y class="scroll-content">
      <view class="institution-info-card">
        <view class="institution-header">
          <view class="inst-icon">
            <image :src="formatLogoUrl(institution?.logoUrl)" class="inst-icon-img" mode="aspectFit" />
          </view>
          <view class="inst-details">
            <text class="inst-label">当前选择机构</text>
            <text class="inst-name">{{ institutionName }}</text>
          </view>
          <view class="change-btn" @tap="handleChangeInstitution">
            <text class="change-text">修改</text>
            <image src="/static/images/chevron_right.png" class="change-arrow" mode="aspectFit" />
          </view>
        </view>
      </view>

      <view class="form-section">
        <view class="form-item">
          <text class="form-label">账号 / 卡号<text class="required-mark">*</text></text>
          <input 
            class="form-input" 
            v-model="accountForm.accountNo" 
            placeholder="如卡号/手机号后四位"
          />
        </view>

        <view v-if="showAccountTypePicker" class="form-item">
          <text class="form-label">账户类型<text class="required-mark">*</text></text>
          <picker 
            mode="selector" 
            :range="accountTypeOptions" 
            range-key="name"
            @change="handleAccountTypeChange"
          >
            <view class="form-picker">
              {{ selectedAccountTypeName || '请选择' }}
              <image src="/static/images/chevron_right.png" class="picker-arrow" mode="aspectFit" />
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">账户名称<text class="required-mark">*</text></text>
          <input 
            class="form-input" 
            v-model="accountForm.accountName" 
            placeholder="例如 招商银行工资卡"
          />
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
            @tap="handleNetworthToggle"
          >
            <view class="switch-thumb"></view>
          </view>
        </view>
      </view>

      <view class="modules-section">
        <view class="section-header">
          <image src="/static/images/grid_view.png" class="section-icon" mode="aspectFit" />
          <text class="section-title">资产模块配置</text>
        </view>

        <view v-if="requiredModules.length > 0" class="module-group">
          <text class="group-label">必选模块</text>
          <view class="module-list">
            <view 
              v-for="module in requiredModules" 
              :key="module.categoryCode || module.id"
              class="module-card required-card"
            >
              <view class="module-icon-wrap primary-bg">
                <image :src="module.iconUrl" class="module-icon" mode="aspectFit" />
              </view>
              <view class="module-info">
                <view class="module-name-row">
                  <text class="module-name">{{ module.name }}</text>
                  <view class="module-tag required-tag">必选</view>
                </view>
                <text class="module-desc">{{ module.description }}</text>
              </view>
              <image src="/static/images/lock.png" class="module-lock" mode="aspectFit" />
            </view>
          </view>
        </view>

        <view v-if="defaultModules.length > 0" class="module-group">
          <text class="group-label">默认模块</text>
          <view class="module-list">
            <view 
              v-for="module in defaultModules" 
              :key="module.categoryCode || module.id"
              class="module-card"
              @tap="toggleModule(module, !getModuleChecked(module, 'DEFAULT_SELECTED'))"
            >
              <view class="module-icon-wrap" :class="module.iconBgClass">
                <image :src="module.iconUrl" class="module-icon" mode="aspectFit" />
              </view>
              <view class="module-info">
                <view class="module-name-row">
                  <text class="module-name">{{ module.name }}</text>
                  <view class="module-tag default-tag">默认</view>
                </view>
                <text class="module-desc">{{ module.description }}</text>
              </view>
              <view 
                class="module-checkbox" 
                :class="{ checked: getModuleChecked(module, 'DEFAULT_SELECTED') }"
              >
                <view v-if="getModuleChecked(module, 'DEFAULT_SELECTED')" class="check-icon">✓</view>
              </view>
            </view>
          </view>
        </view>

        <view v-if="optionalModules.length > 0" class="module-group">
          <text class="group-label">可选模块</text>
          <view class="module-list">
            <view 
              v-for="module in optionalModules" 
              :key="module.categoryCode || module.id"
              class="module-card"
              :class="{ disabled: module.disabled }"
              @tap="!module.disabled && toggleModule(module, !getModuleChecked(module, 'OPTIONAL'))"
            >
              <view class="module-icon-wrap default-bg">
                <image :src="module.iconUrl" class="module-icon" mode="aspectFit" />
              </view>
              <view class="module-info">
                <view class="module-name-row">
                  <text class="module-name">{{ module.name }}</text>
                  <view class="module-tag optional-tag">可选</view>
                </view>
                <text class="module-desc">{{ module.description }}</text>
              </view>
              <view 
                class="module-checkbox" 
                :class="{ checked: getModuleChecked(module, 'OPTIONAL') }"
              >
                <view v-if="getModuleChecked(module, 'OPTIONAL')" class="check-icon">✓</view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <view class="bottom-content">
        <button class="confirm-btn" @tap="handleConfirm" :disabled="!canSubmit || submitting">
          <text class="btn-text">{{ submitting ? '提交中...' : '确认添加' }}</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getInstitutionDetail, getInstitutionModules, createAccount, getAccountTypes } from '@/api/modules/asset'

const instCode = ref('')
const institution = ref(null)
const loading = ref(false)
const submitting = ref(false)

const accountTypesRaw = ref([])
const selectedAccountType = ref('')
const accountTypePickerIndex = ref(0)

const accountForm = ref({
  accountNo: '',
  accountName: '',
  visibility: 'PRIVATE',
  includeInNetworth: true
})

const institutionModules = ref([])
const selectedModules = ref([])

const requiredModules = computed(() => institutionModules.value.filter(m => m.selectionType === 'REQUIRED'))
const defaultModules = computed(() => institutionModules.value.filter(m => m.selectionType === 'DEFAULT_SELECTED'))
const optionalModules = computed(() => institutionModules.value.filter(m => m.selectionType === 'OPTIONAL'))

const accountTypeOptions = computed(() => {
  return accountTypesRaw.value.map(raw => {
    const [code, name] = raw.split(':')
    return { code, name: name || code }
  })
})

const showAccountTypePicker = computed(() => accountTypeOptions.value.length > 1)

const selectedAccountTypeName = computed(() => {
  const found = accountTypeOptions.value.find(o => o.code === selectedAccountType.value)
  return found ? found.name : ''
})

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
      && selectedAccountType.value.length > 0
})

const getModuleChecked = (module, type) => {
  if (type === 'REQUIRED') return true
  const key = module.categoryCode || module.id
  if (selectedModules.value.includes(key)) {
    return true
  }
  return type === 'DEFAULT_SELECTED'
}

const toggleModule = (module, checked) => {
  const key = module.categoryCode || module.id
  if (checked) {
    if (!selectedModules.value.includes(key)) {
      selectedModules.value.push(key)
    }
  } else {
    const index = selectedModules.value.indexOf(key)
    if (index > -1) {
      selectedModules.value.splice(index, 1)
    }
  }
}

const handleChangeInstitution = () => {
  uni.navigateBack()
}

const handleAccountTypeChange = (e) => {
  const index = Number(e.detail.value)
  accountTypePickerIndex.value = index
  selectedAccountType.value = accountTypeOptions.value[index].code
}

const handleNetworthToggle = () => {
  accountForm.value.includeInNetworth = !accountForm.value.includeInNetworth
}

const handleConfirm = async () => {
  if (!canSubmit.value) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    const selectedModules = [
      ...requiredModules.value.map(m => ({ assetType: m.groupType, moduleName: m.name })),
      ...defaultModules.value
        .filter(m => getModuleChecked(m, 'DEFAULT_SELECTED'))
        .map(m => ({ assetType: m.groupType, moduleName: m.name })),
      ...optionalModules.value
        .filter(m => getModuleChecked(m, 'OPTIONAL'))
        .map(m => ({ assetType: m.groupType, moduleName: m.name }))
    ]

    const resp = await createAccount({
      instCode: instCode.value,
      accountNo: accountForm.value.accountNo,
      accountType: selectedAccountType.value,
      accountName: accountForm.value.accountName,
      includeInNetWorth: accountForm.value.includeInNetworth,
      modules: selectedModules
    })

    const statusCode = resp?.statusCode ?? resp?.data?.code
    const ok = statusCode === 200 || statusCode === undefined

    if (ok) {
      uni.showToast({ title: '添加成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1200)
    } else {
      uni.showToast({
        title: (resp?.data?.message) || '添加失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('添加账户失败:', error)
    uni.showToast({ title: '添加失败，请稍后重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const loadInstitution = async () => {
  if (!instCode.value) return
  
  loading.value = true
  try {
    let loadedTypes = null
    try {
      const typeRes = await getAccountTypes(instCode.value)
      if (typeRes && typeRes.data && Array.isArray(typeRes.data) && typeRes.data.length > 0) {
        loadedTypes = typeRes.data
      }
    } catch (e) {
      console.warn('加载账户类型独立接口失败，尝试 fallback:', e)
    }

    const res = await getInstitutionDetail(instCode.value)
    if (res && res.data) {
      institution.value = res.data

      if (loadedTypes && loadedTypes.length > 0) {
        accountTypesRaw.value = loadedTypes
      } else {
        const raw = res.data.institutionType?.accountTypes || []
        accountTypesRaw.value = raw
      }

      if (accountTypeOptions.value.length === 1) {
        selectedAccountType.value = accountTypeOptions.value[0].code
      } else if (accountTypeOptions.value.length > 1) {
        selectedAccountType.value = accountTypeOptions.value[0].code
      } else {
        uni.showToast({ title: '该机构暂无可选账户类型', icon: 'none' })
      }
    }
  } catch (error) {
    console.error('加载机构信息失败:', error)
    uni.showToast({ title: '加载机构信息失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const loadModules = async () => {
  if (!instCode.value) return
  
  try {
    const res = await getInstitutionModules(instCode.value)
    if (res && res.data) {
      const { required, defaultList, optional } = res.data
      
      const transformedModules = [
        ...(required || []).map(module => ({ ...module, selectionType: 'REQUIRED' })),
        ...(defaultList || []).map(module => ({ ...module, selectionType: 'DEFAULT_SELECTED' })),
        ...(optional || []).map(module => ({ ...module, selectionType: 'OPTIONAL' }))
      ]
      
      institutionModules.value = transformedModules
      initDefaultModules()
    }
  } catch (error) {
    console.error('加载机构模块失败:', error)
    uni.showToast({ title: '加载模块失败', icon: 'none' })
  }
}

const initDefaultModules = () => {
  selectedModules.value = []
  defaultModules.value.forEach(module => {
    const key = module.categoryCode || module.id
    selectedModules.value.push(key)
  })
}

onLoad((options) => {
  if (options.instCode) {
    instCode.value = options.instCode
  }
})

onMounted(async () => {
  await loadInstitution()
  await loadModules()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.add-account-page {
  min-height: 100vh;
  background: $background;
}

.scroll-content {
  height: calc(100vh - 160rpx);
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

.change-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.change-text {
  font-size: 26rpx;
  font-weight: 700;
  color: #1F1F1F;
}

.change-arrow {
  width: 40rpx;
  height: 40rpx;
  opacity: 0.8;
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

.form-picker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 84rpx;
  padding: 0 32rpx;
  background: $surface-container-low;
  border-radius: $rounded-full;
  font-size: 28rpx;
  color: $on-surface;
}

.picker-arrow {
  width: 40rpx;
  height: 40rpx;
  opacity: 0.6;
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

.modules-section {
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

.module-group {
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

.module-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.module-card {
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

.module-icon-wrap {
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

.module-icon {
  width: 40rpx;
  height: 40rpx;
}

.module-info {
  flex: 1;
}

.module-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.module-name {
  font-size: 28rpx;
  font-weight: 700;
  color: $on-surface;
}

.module-tag {
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

.module-desc {
  font-size: 24rpx;
  color: $outline;
}

.module-lock {
  width: 48rpx;
  height: 48rpx;
  opacity: 0.6;
}

.module-checkbox {
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

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: $spacing-4 $spacing-4;
  background: $surface-container-lowest;
  z-index: 100;
}

.bottom-content {
  max-width: 720rpx;
  margin: 0 auto;
}

.confirm-btn {
  width: 100%;
  padding: 20rpx;
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
