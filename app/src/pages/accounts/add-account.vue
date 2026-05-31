
&lt;template&gt;
  &lt;view class="add-account-page"&gt;
    &lt;scroll-view scroll-y class="scroll-content"&gt;
      &lt;view class="institution-info-card"&gt;
        &lt;view class="institution-header"&gt;
          &lt;view class="inst-icon"&gt;
            &lt;text class="inst-icon-text"&gt;{{ institutionIcon }}&lt;/text&gt;
          &lt;/view&gt;
          &lt;view class="inst-details"&gt;
            &lt;text class="inst-label"&gt;当前选择机构&lt;/text&gt;
            &lt;text class="inst-name"&gt;{{ institutionName }}&lt;/text&gt;
          &lt;/view&gt;
          &lt;view class="change-btn" @tap="handleChangeInstitution"&gt;
            &lt;text class="change-text"&gt;修改&lt;/text&gt;
            &lt;text class="change-arrow"&gt;›&lt;/text&gt;
          &lt;/view&gt;
        &lt;/view&gt;
      &lt;/view&gt;

      &lt;view class="form-section"&gt;
        &lt;view class="form-item"&gt;
          &lt;text class="form-label"&gt;账户标识&lt;text class="required-mark"&gt;*&lt;/text&gt;&lt;/text&gt;
          &lt;input 
            class="form-input" 
            v-model="accountForm.accountId" 
            placeholder="如卡号/手机号后四位"
          /&gt;
        &lt;/view&gt;

        &lt;view class="form-item"&gt;
          &lt;text class="form-label"&gt;账户名称&lt;text class="required-mark"&gt;*&lt;/text&gt;&lt;/text&gt;
          &lt;input 
            class="form-input" 
            v-model="accountForm.accountName" 
            placeholder="例如 招商银行工资卡"
          /&gt;
        &lt;/view&gt;

        &lt;view class="form-item"&gt;
          &lt;text class="form-label"&gt;账户用途 / 模板&lt;/text&gt;
          &lt;picker 
            mode="selector" 
            :range="accountTemplates" 
            range-key="name"
            @change="handleTemplateChange"
          &gt;
            &lt;view class="form-picker"&gt;
              {{ selectedTemplate || '储蓄卡 / 借记卡' }}
              &lt;text class="picker-arrow"&gt;›&lt;/text&gt;
            &lt;/view&gt;
          &lt;/picker&gt;
        &lt;/view&gt;

        &lt;view class="visibility-section"&gt;
          &lt;text class="form-label"&gt;可见范围&lt;/text&gt;
          &lt;view class="visibility-options"&gt;
            &lt;label class="visibility-option"&gt;
              &lt;radio value="PRIVATE" :checked="accountForm.visibility === 'PRIVATE'" color="#006754" /&gt;
              &lt;text class="option-text"&gt;仅自己可见&lt;/text&gt;
            &lt;/label&gt;
            &lt;label class="visibility-option"&gt;
              &lt;radio value="FAMILY" :checked="accountForm.visibility === 'FAMILY'" color="#006754" /&gt;
              &lt;text class="option-text"&gt;家庭成员可见&lt;/text&gt;
            &lt;/label&gt;
          &lt;/view&gt;
        &lt;/view&gt;

        &lt;view class="networth-section"&gt;
          &lt;view class="networth-info"&gt;
            &lt;text class="networth-title"&gt;计入净值&lt;/text&gt;
            &lt;text class="networth-desc"&gt;将此账户金额计入个人资产总额&lt;/text&gt;
          &lt;/view&gt;
          &lt;switch 
            :checked="accountForm.includeInNetworth" 
            @change="handleNetworthToggle"
            color="#006754"
          /&gt;
        &lt;/view&gt;
      &lt;/view&gt;

      &lt;view class="modules-section"&gt;
        &lt;view class="section-header"&gt;
          &lt;text class="section-icon"&gt;📊&lt;/text&gt;
          &lt;text class="section-title"&gt;资产模块配置&lt;/text&gt;
        &lt;/view&gt;

        &lt;view v-if="requiredModules.length &gt; 0" class="module-group"&gt;
          &lt;text class="group-label"&gt;必选模块&lt;/text&gt;
          &lt;view class="module-list"&gt;
            &lt;AssetModuleItem
              v-for="module in requiredModules"
              :key="module.categoryCode || module.id"
              :module="module"
              selection-type="REQUIRED"
              :checked="true"
            /&gt;
          &lt;/view&gt;
        &lt;/view&gt;

        &lt;view v-if="defaultModules.length &gt; 0" class="module-group"&gt;
          &lt;text class="group-label"&gt;默认模块&lt;/text&gt;
          &lt;view class="module-list"&gt;
            &lt;AssetModuleItem
              v-for="module in defaultModules"
              :key="module.categoryCode || module.id"
              :module="module"
              selection-type="DEFAULT_SELECTED"
              :checked="getModuleChecked(module, 'DEFAULT_SELECTED')"
              @toggle="(checked) =&gt; toggleModule(module, checked)"
            /&gt;
          &lt;/view&gt;
        &lt;/view&gt;

        &lt;view v-if="optionalModules.length &gt; 0" class="module-group"&gt;
          &lt;text class="group-label"&gt;可选模块&lt;/text&gt;
          &lt;view class="module-list"&gt;
            &lt;AssetModuleItem
              v-for="module in optionalModules"
              :key="module.categoryCode || module.id"
              :module="module"
              selection-type="OPTIONAL"
              :checked="getModuleChecked(module, 'OPTIONAL')"
              @toggle="(checked) =&gt; toggleModule(module, checked)"
            /&gt;
          &lt;/view&gt;
        &lt;/view&gt;
      &lt;/view&gt;
    &lt;/scroll-view&gt;

    &lt;view class="bottom-bar"&gt;
      &lt;view class="bottom-content"&gt;
        &lt;button class="confirm-btn" @tap="handleConfirm" :disabled="!canSubmit"&gt;
          &lt;text class="btn-text"&gt;确认添加&lt;/text&gt;
        &lt;/button&gt;
      &lt;/view&gt;
    &lt;/view&gt;
  &lt;/view&gt;
&lt;/template&gt;

&lt;script setup&gt;
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

const requiredModules = computed(() =&gt; institutionModules.value.filter(m =&gt; m.selectionType === 'REQUIRED'))
const defaultModules = computed(() =&gt; institutionModules.value.filter(m =&gt; m.selectionType === 'DEFAULT_SELECTED'))
const optionalModules = computed(() =&gt; institutionModules.value.filter(m =&gt; m.selectionType === 'OPTIONAL'))

const institutionName = computed(() =&gt; {
  if (institution.value) {
    return institution.value.instName || institution.value.shortName || '未知机构'
  }
  return '招商银行'
})

const institutionIcon = computed(() =&gt; {
  const name = institutionName.value
  return name.charAt(0)
})

const canSubmit = computed(() =&gt; {
  return accountForm.value.accountName.trim().length &gt; 0 &amp;&amp; accountForm.value.accountId.trim().length &gt; 0
})

const getModuleChecked = (module, type) =&gt; {
  if (type === 'REQUIRED') return true
  const key = module.categoryCode || module.id
  if (selectedModules.value.includes(key)) {
    return true
  }
  return type === 'DEFAULT_SELECTED'
}

const toggleModule = (module, checked) =&gt; {
  const key = module.categoryCode || module.id
  if (checked) {
    if (!selectedModules.value.includes(key)) {
      selectedModules.value.push(key)
    }
  } else {
    const index = selectedModules.value.indexOf(key)
    if (index &gt; -1) {
      selectedModules.value.splice(index, 1)
    }
  }
}

const handleChangeInstitution = () =&gt; {
  uni.navigateBack()
}

const handleTemplateChange = (e) =&gt; {
  const index = e.detail.value
  selectedTemplate.value = accountTemplates.value[index].name
}

const handleNetworthToggle = (e) =&gt; {
  accountForm.value.includeInNetworth = e.detail.value
}

const handleConfirm = async () =&gt; {
  if (!canSubmit.value) {
    uni.showToast({
      title: '请填写完整信息',
      icon: 'none'
    })
    return
  }

  submitting.value = true
  try {
    await new Promise(resolve =&gt; setTimeout(resolve, 1000))
    uni.showToast({
      title: '添加成功',
      icon: 'success'
    })
    setTimeout(() =&gt; {
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

const loadInstitution = async () =&gt; {
  if (!instCode.value) return
  
  loading.value = true
  try {
    const res = await getInstitutionDetail(instCode.value)
    if (res &amp;&amp; res.data) {
      institution.value = res.data
    }
  } catch (error) {
    console.error('加载机构信息失败:', error)
  } finally {
    loading.value = false
  }
}

const loadModules = async () =&gt; {
  if (!instCode.value) {
    loadMockModules()
    return
  }

  try {
    const res = await getInstitutionCategories(instCode.value)
    if (res &amp;&amp; res.data) {
      institutionModules.value = res.data
      initDefaultModules()
    }
  } catch (error) {
    console.error('加载机构模块失败:', error)
    loadMockModules()
  }
}

const loadMockModules = () =&gt; {
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

const initDefaultModules = () =&gt; {
  selectedModules.value = []
  defaultModules.value.forEach(module =&gt; {
    const key = module.categoryCode || module.id
    selectedModules.value.push(key)
  })
}

onLoad((options) =&gt; {
  if (options.instCode) {
    instCode.value = options.instCode
  }
})

onMounted(async () =&gt; {
  await loadInstitution()
  await loadModules()
})
&lt;/script&gt;

&lt;style lang="scss" scoped&gt;
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
&lt;/style&gt;

