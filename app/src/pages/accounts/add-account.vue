
<template>
  <view class="add-account-page">
    <scroll-view scroll-y class="scroll-content">
      <view class="institution-info-card">
        <view class="institution-header">
          <view class="inst-icon">
            <text class="inst-icon-text">{{ institutionIcon }}</text>
          </view>
          <view class="inst-details">
            <text class="inst-label">当前选择机构</text>
            <text class="inst-name">{{ institutionName }}</text>
          </view>
          <view class="change-btn" @tap="handleChangeInstitution">
            <text class="change-text">修改</text>
            <text class="change-arrow">›</text>
          </view>
        </view>
      </view>

      <view class="form-section">
        <view class="form-item">
          <text class="form-label">账户标识<text class="required-mark">*</text></text>
          <input 
            class="form-input" 
            v-model="accountForm.accountId" 
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
          <text class="form-label">账户用途 / 模板</text>
          <picker 
            mode="selector" 
            :range="accountTemplates" 
            range-key="name"
            @change="handleTemplateChange"
          >
            <view class="form-picker">
              {{ selectedTemplate || '储蓄卡 / 借记卡' }}
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="visibility-section">
          <text class="form-label">可见范围</text>
          <view class="visibility-options">
            <label class="visibility-option">
              <radio value="PRIVATE" :checked="accountForm.visibility === 'PRIVATE'" color="#006754" />
              <text class="option-text">仅自己可见</text>
            </label>
            <label class="visibility-option">
              <radio value="FAMILY" :checked="accountForm.visibility === 'FAMILY'" color="#006754" />
              <text class="option-text">家庭成员可见</text>
            </label>
          </view>
        </view>

        <view class="networth-section">
          <view class="networth-info">
            <text class="networth-title">计入净值</text>
            <text class="networth-desc">将此账户金额计入个人资产总额</text>
          </view>
          <switch 
            :checked="accountForm.includeInNetworth" 
            @change="handleNetworthToggle"
            color="#006754"
          />
        </view>
      </view>

      <view class="modules-section">
        <view class="section-header">
          <text class="section-icon">📊</text>
          <text class="section-title">资产模块配置</text>
        </view>

        <view v-if="requiredModules.length > 0" class="module-group">
          <text class="group-label">必选模块</text>
          <view class="module-list">
            <AssetModuleItem
              v-for="module in requiredModules"
              :key="module.categoryCode || module.id"
              :module="module"
              selection-type="REQUIRED"
              :checked="true"
            />
          </view>
        </view>

        <view v-if="defaultModules.length > 0" class="module-group">
          <text class="group-label">默认模块</text>
          <view class="module-list">
            <AssetModuleItem
              v-for="module in defaultModules"
              :key="module.categoryCode || module.id"
              :module="module"
              selection-type="DEFAULT_SELECTED"
              :checked="getModuleChecked(module, 'DEFAULT_SELECTED')"
              @toggle="(checked) => toggleModule(module, checked)"
            />
          </view>
        </view>

        <view v-if="optionalModules.length > 0" class="module-group">
          <text class="group-label">可选模块</text>
          <view class="module-list">
            <AssetModuleItem
              v-for="module in optionalModules"
              :key="module.categoryCode || module.id"
              :module="module"
              selection-type="OPTIONAL"
              :checked="getModuleChecked(module, 'OPTIONAL')"
              @toggle="(checked) => toggleModule(module, checked)"
            />
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <view class="bottom-content">
        <button class="confirm-btn" @tap="handleConfirm" :disabled="!canSubmit">
          <text class="btn-text">确认添加</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getInstitutionDetail, getInstitutionCategories } from '@/api/modules/asset'
import AssetModuleItem from '@/components/accounts/AssetModuleItem.vue'

const instCode = ref('')
const institution = ref(null)
const loading = ref(false)
const submitting = ref(false)

const accountForm = ref({
  accountId: '',
  accountName: '',
  visibility: 'PRIVATE',
  includeInNetworth: true
})

const accountTemplates = ref([
  { id: 'DEBIT', name: '储蓄卡 / 借记卡' },
  { id: 'CREDIT', name: '信用卡' },
  { id: 'LOAN', name: '贷款账户' },
  { id: 'WALLET', name: '支付钱包' },
  { id: 'SECURITY', name: '证券账户' }
])
const selectedTemplate = ref('')

const institutionModules = ref([])
const selectedModules = ref([])

const requiredModules = computed(() => institutionModules.value.filter(m => m.selectionType === 'REQUIRED'))
const defaultModules = computed(() => institutionModules.value.filter(m => m.selectionType === 'DEFAULT_SELECTED'))
const optionalModules = computed(() => institutionModules.value.filter(m => m.selectionType === 'OPTIONAL'))

const institutionName = computed(() => {
  if (institution.value) {
    return institution.value.instName || institution.value.shortName || '未知机构'
  }
  return '招商银行'
})

const institutionIcon = computed(() => {
  const name = institutionName.value
  return name.charAt(0)
})

const canSubmit = computed(() => {
  return accountForm.value.accountName.trim().length > 0 && accountForm.value.accountId.trim().length > 0
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

const handleTemplateChange = (e) => {
  const index = e.detail.value
  selectedTemplate.value = accountTemplates.value[index].name
}

const handleNetworthToggle = (e) => {
  accountForm.value.includeInNetworth = e.detail.value
}

const handleConfirm = async () => {
  if (!canSubmit.value) {
    uni.showToast({
      title: '请填写完整信息',
      icon: 'none'
    })
    return
  }

  submitting.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    uni.showToast({
      title: '添加成功',
      icon: 'success'
    })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    uni.showToast({
      title: '添加失败',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

const loadInstitution = async () => {
  if (!instCode.value) return
  
  loading.value = true
  try {
    const res = await getInstitutionDetail(instCode.value)
    if (res && res.data) {
      institution.value = res.data
    }
  } catch (error) {
    console.error('加载机构信息失败:', error)
  } finally {
    loading.value = false
  }
}

const loadModules = async () => {
  if (!instCode.value) {
    loadMockModules()
    return
  }

  try {
    const res = await getInstitutionCategories(instCode.value)
    if (res && res.data) {
      institutionModules.value = res.data
      initDefaultModules()
    }
  } catch (error) {
    console.error('加载机构模块失败:', error)
    loadMockModules()
  }
}

const loadMockModules = () => {
  institutionModules.value = [
    { categoryCode: 'TRANSACTION', name: '收支流水', description: '该模块是此账户的基础模块', selectionType: 'REQUIRED' },
    { categoryCode: 'BALANCE', name: '余额', description: '实时账户可用金额统计', selectionType: 'DEFAULT_SELECTED' },
    { categoryCode: 'CURRENT', name: '活期', description: '灵活存取的闲置资金', selectionType: 'DEFAULT_SELECTED' },
    { categoryCode: 'TIME_DEPOSIT', name: '定存', description: '定期存款利息监控', selectionType: 'OPTIONAL' },
    { categoryCode: 'FINANCING', name: '理财', description: '银行理财产品净值同步', selectionType: 'OPTIONAL' },
    { categoryCode: 'INSTALLMENT', name: '分期', description: '信用卡或账单分期管理', selectionType: 'OPTIONAL' }
  ]
  initDefaultModules()
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
  padding: 32rpx;
  background: #FFFFFF;
  border-radius: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.inst-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: $surface-container;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.inst-icon-text {
  font-size: 32rpx;
  font-weight: 700;
  color: $primary;
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
  color: $primary;
}

.change-arrow {
  font-size: 32rpx;
  font-weight: 300;
  color: $primary;
}

.form-section {
  padding: 0 32rpx 32rpx;
  background: #FFFFFF;
  border-radius: 32rpx;
  margin: 0 32rpx;
  padding: 48rpx;
}

.form-item {
  margin-bottom: 40rpx;
}

.form-label {
  display: block;
  font-size: 24rpx;
  font-weight: 700;
  color: $outline;
  margin-bottom: 16rpx;
}

.required-mark {
  color: $error;
  margin-left: 4rpx;
}

.form-input {
  width: 100%;
  padding: 28rpx 32rpx;
  background: $surface-container-low;
  border-radius: 24rpx;
  font-size: 28rpx;
  color: $on-surface;
  box-sizing: border-box;
}

.form-picker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 32rpx;
  background: $surface-container-low;
  border-radius: 24rpx;
  font-size: 28rpx;
  color: $on-surface;
}

.picker-arrow {
  font-size: 32rpx;
  color: $outline;
  font-weight: 300;
}

.visibility-section {
  padding-top: 16rpx;
}

.visibility-options {
  display: flex;
  gap: 48rpx;
}

.visibility-option {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.option-text {
  font-size: 26rpx;
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
  font-weight: 700;
  color: $on-surface;
  display: block;
}

.networth-desc {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: $outline;
  display: block;
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
  font-size: 32rpx;
}

.section-title {
  font-size: 26rpx;
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

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 32rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  background: linear-gradient(to top, $background 70%, transparent 100%);
  z-index: 100;
}

.bottom-content {
  max-width: 640rpx;
  margin: 0 auto;
}

.confirm-btn {
  width: 100%;
  padding: 32rpx;
  background: $primary;
  border-radius: 999rpx;
  border: none;
  box-shadow: 0 8rpx 32rpx rgba(0, 103, 84, 0.2);
  display: flex;
  justify-content: center;
  align-items: center;
}

.confirm-btn[disabled] {
  opacity: 0.5;
}

.btn-text {
  font-size: 32rpx;
  font-weight: 700;
  color: $on-primary;
}
</style>

