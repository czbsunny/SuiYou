<template>
  <view class="page record-page">
    <!-- 滚动区域（包含切换器和分类） -->
    <scroll-view scroll-y class="main-scroll">
      <view class="scroll-content">
        <!-- 1. 支出/收入分段选择器 -->
        <view class="segmented-bar">
          <view class="segmented-picker" :class="{ 'income-active': activeTab === 'INCOME' }">
            <view class="segmented-indicator"></view>
            <view class="segment-btn" :class="{ active: activeTab === 'EXPENSE' }" @tap="handleTabChange('EXPENSE')">
              <text>支出</text>
            </view>
            <view class="segment-btn" :class="{ active: activeTab === 'INCOME' }" @tap="handleTabChange('INCOME')">
              <text>收入</text>
            </view>
          </view>
        </view>

        <!-- 2. 分类选择区域 -->
        <view class="category-card">
          <view v-for="(row, rowIndex) in chunkedCategories" :key="rowIndex" class="category-row-group">
            
            <!-- 主分类网格 -->
            <view class="category-grid">
              <view 
                v-for="cat in row" 
                :key="cat.name" 
                class="category-item" 
                :class="{ active: selectedPrimary === cat.name }" 
                @tap="selectPrimary(cat)"
              >
                <view class="category-icon-wrapper" :class="{ active: selectedPrimary === cat.name }">
                  <text class="category-icon">{{ cat.icon }}</text>
                </view>
                <text class="category-label" :class="{ active: selectedPrimary === cat.name }">{{ cat.name }}</text>
              </view>
            </view>

            <!-- 子分类弹出面板 (抽屉式) -->
            <view class="sub-drawer" v-if="isParentInRow(row)">
              <view class="sub-category-section">
                <view class="sub-category-grid">
                  <view 
                    v-for="sub in selectedPrimaryData?.children" 
                    :key="sub.name" 
                    class="sub-category-item" 
                    :class="{ active: selectedSub === sub.name }" 
                    @tap="selectSub(sub)"
                  >
                    <view class="sub-icon-wrapper" :class="{ active: selectedSub === sub.name }">
                      <text class="sub-icon">{{ sub.icon }}</text>
                    </view>
                    <text class="sub-label" :class="{ active: selectedSub === sub.name }">{{ sub.name }}</text>
                  </view>
                </view>
              </view>
            </view>
            
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 3. 录入详情卡片 -->
    <view class="info-card">
      <view class="amount-display">
        <text class="currency" :class="{ 'income-color': activeTab === 'INCOME' }">¥</text>
        <text class="amount-value" :class="{ 'income-color': activeTab === 'INCOME' }">{{ currentAmount || '0' }}</text>
      </view>
      
      <view class="info-tags">
        <!-- 日期标签 -->
        <view class="info-tag">
          <svg class="tag-svg" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#8D938C" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="16" y1="2" x2="16" y2="6"></line>
            <line x1="8" y1="2" x2="8" y2="6"></line>
            <line x1="3" y1="10" x2="21" y2="10"></line>
          </svg>
          <text class="tag-text">{{ displayDateTime }}</text>
        </view>
        
        <!-- 账户标签 -->
        <view class="info-tag" @tap="handleAccount">
          <svg class="tag-svg" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#8D938C" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="5" width="20" height="14" rx="2" ry="2"></rect>
            <line x1="2" y1="10" x2="22" y2="10"></line>
          </svg>
          <text class="tag-text">{{ selectedAccount?.accountName || '账户' }}</text>
        </view>
        
        <!-- 备注标签 -->
        <view class="info-tag remark">
          <svg class="tag-svg" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#8D938C" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="8" y1="6" x2="21" y2="6"></line>
            <line x1="8" y1="12" x2="21" y2="12"></line>
            <line x1="8" y1="18" x2="21" y2="18"></line>
            <circle cx="3" cy="6" r="1"></circle>
            <circle cx="3" cy="12" r="1"></circle>
            <circle cx="3" cy="18" r="1"></circle>
          </svg>
          <input class="tag-input" type="text" v-model="remark" placeholder="备注..." placeholder-style="color:#8D938C" />
        </view>
      </view>
    </view>

    <!-- 4. 键盘面板 -->
    <NumberKeypad 
      v-model="currentAmount" 
      @confirm="confirmSave" 
      @saveAndAgain="saveAndAgain"
      @operator="setOperator"
    />
    
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import NumberKeypad from '@/components/bill/NumberKeypad.vue'

const activeTab = ref('EXPENSE')
const selectedPrimary = ref('食品餐饮')
const selectedSub = ref('早餐')
const currentAmount = ref('6')
const remark = ref('')
const selectedAccount = ref({ accountName: '账户' })

// 类别库配置
const categories = {
  EXPENSE: [
    { 
      name: '食品餐饮', icon: '食', children: [
        { name: '早餐', icon: '早' },
        { name: '午餐', icon: '午' },
        { name: '晚餐', icon: '晚' },
        { name: '宵夜', icon: '宵' },
        { name: '酒水', icon: '酒' },
        { name: '甜点', icon: '甜' },
        { name: '零食', icon: '零' },
        { name: '食材', icon: '材' },
        { name: '生鲜', icon: '鲜' },
        { name: '自定义', icon: '自' }
      ]
    },
    { name: '购物消费', icon: '购', children: [
        { name: '日用品', icon: '日' },
        { name: '服装', icon: '服' },
        { name: '数码', icon: '数' },
        { name: '家电', icon: '电' },
        { name: '美妆', icon: '美' },
        { name: '母婴', icon: '婴' },
        { name: '图书', icon: '书' },
        { name: '运动', icon: '动' },
        { name: '家居', icon: '居' },
        { name: '自定义', icon: '自' }
      ]
    },
    { name: '出行交通', icon: '行', children: [
        { name: '公交', icon: '公' },
        { name: '地铁', icon: '地' },
        { name: '打车', icon: '打' },
        { name: '自驾', icon: '驾' },
        { name: '高铁', icon: '高' },
        { name: '机票', icon: '机' },
        { name: '租车', icon: '租' },
        { name: '加油', icon: '油' },
        { name: '停车', icon: '停' },
        { name: '自定义', icon: '自' }
      ]
    },
    { name: '休闲娱乐', icon: '乐', children: [
        { name: '电影', icon: '影' },
        { name: '游戏', icon: '游' },
        { name: 'KTV', icon: 'K' },
        { name: '旅游', icon: '旅' },
        { name: '运动', icon: '动' },
        { name: '演出', icon: '演' },
        { name: '酒吧', icon: '酒' },
        { name: '咖啡', icon: '咖' },
        { name: '茶馆', icon: '茶' },
        { name: '自定义', icon: '自' }
      ]
    },
    { name: '教育', icon: '教', children: [
        { name: '学费', icon: '学' },
        { name: '培训', icon: '培' },
        { name: '书本', icon: '书' },
        { name: '文具', icon: '文' },
        { name: '考试', icon: '考' },
        { name: '留学', icon: '留' },
        { name: '兴趣班', icon: '兴' },
        { name: '网课', icon: '网' },
        { name: '其他', icon: '其' },
        { name: '自定义', icon: '自' }
      ]
    },
    { name: '居家', icon: '居', children: [
        { name: '房租', icon: '房' },
        { name: '水电', icon: '水' },
        { name: '燃气', icon: '燃' },
        { name: '物业', icon: '物' },
        { name: '维修', icon: '修' },
        { name: '装修', icon: '装' },
        { name: '家具', icon: '家' },
        { name: '保洁', icon: '保' },
        { name: '网络', icon: '网' },
        { name: '自定义', icon: '自' }
      ]
    },
    { name: '医疗', icon: '医', children: [
        { name: '门诊', icon: '门' },
        { name: '住院', icon: '住' },
        { name: '药品', icon: '药' },
        { name: '体检', icon: '体' },
        { name: '牙科', icon: '牙' },
        { name: '眼科', icon: '眼' },
        { name: '美容', icon: '美' },
        { name: '保险', icon: '保' },
        { name: '器械', icon: '器' },
        { name: '自定义', icon: '自' }
      ]
    },
    { name: '人情', icon: '情', children: [
        { name: '红包', icon: '红' },
        { name: '礼物', icon: '礼' },
        { name: '请客', icon: '请' },
        { name: '聚餐', icon: '聚' },
        { name: '婚庆', icon: '婚' },
        { name: '生日', icon: '生' },
        { name: '节日', icon: '节' },
        { name: '探病', icon: '探' },
        { name: '丧葬', icon: '丧' },
        { name: '自定义', icon: '自' }
      ]
    },
    { name: '职业', icon: '职', children: [
        { name: '工资', icon: '工' },
        { name: '奖金', icon: '奖' },
        { name: '报销', icon: '报' },
        { name: '加班', icon: '加' },
        { name: '出差', icon: '差' },
        { name: '培训', icon: '培' },
        { name: '社保', icon: '社' },
        { name: '公积金', icon: '公' },
        { name: '税费', icon: '税' },
        { name: '自定义', icon: '自' }
      ]
    },
    { name: '更多', icon: '多', children: [
        { name: '理财', icon: '理' },
        { name: '投资', icon: '投' },
        { name: '利息', icon: '息' },
        { name: '手续费', icon: '手' },
        { name: '罚款', icon: '罚' },
        { name: '捐赠', icon: '捐' },
        { name: '退款', icon: '退' },
        { name: '借款', icon: '借' },
        { name: '还款', icon: '还' },
        { name: '自定义', icon: '自' }
      ]
    }
  ],
  INCOME: [
    { name: '工资', icon: '工', children: [
        { name: '月薪', icon: '月' },
        { name: '奖金', icon: '奖' },
        { name: '绩效', icon: '绩' },
        { name: '年终奖', icon: '年' },
        { name: '提成', icon: '提' },
        { name: '津贴', icon: '津' },
        { name: '补贴', icon: '补' },
        { name: '加班费', icon: '加' },
        { name: '其他', icon: '其' },
        { name: '自定义', icon: '自' }
      ]
    },
    { name: '投资', icon: '投', children: [
        { name: '股票', icon: '股' },
        { name: '基金', icon: '基' },
        { name: '理财', icon: '理' },
        { name: '利息', icon: '息' },
        { name: '股息', icon: '股' },
        { name: '分红', icon: '分' },
        { name: '债券', icon: '债' },
        { name: '外汇', icon: '汇' },
        { name: '其他', icon: '其' },
        { name: '自定义', icon: '自' }
      ]
    },
    { name: '副业', icon: '副', children: [
        { name: '兼职', icon: '兼' },
        { name: '自由职业', icon: '自' },
        { name: '电商', icon: '电' },
        { name: '自媒体', icon: '媒' },
        { name: '写作', icon: '写' },
        { name: '设计', icon: '设' },
        { name: '编程', icon: '编' },
        { name: '教学', icon: '教' },
        { name: '咨询', icon: '咨' },
        { name: '自定义', icon: '自' }
      ]
    },
    { name: '生意', icon: '生', children: [
        { name: '营业收入', icon: '营' },
        { name: '批发', icon: '批' },
        { name: '零售', icon: '零' },
        { name: '代理', icon: '代' },
        { name: '加盟', icon: '加' },
        { name: '出租', icon: '租' },
        { name: '转让', icon: '转' },
        { name: '合作', icon: '合' },
        { name: '其他', icon: '其' },
        { name: '自定义', icon: '自' }
      ]
    },
    { name: '其他', icon: '其', children: [
        { name: '红包', icon: '红' },
        { name: '礼金', icon: '礼' },
        { name: '退款', icon: '退' },
        { name: '报销', icon: '报' },
        { name: '借款', icon: '借' },
        { name: '还款', icon: '还' },
        { name: '中奖', icon: '中' },
        { name: '继承', icon: '继' },
        { name: '捐赠', icon: '捐' },
        { name: '自定义', icon: '自' }
      ]
    }
  ]
}

const chunkedCategories = computed(() => {
  const cats = categories[activeTab.value] || []
  const chunks = []
  for (let i = 0; i < cats.length; i += 5) {
    chunks.push(cats.slice(i, i + 5))
  }
  return chunks
})

const selectedPrimaryData = computed(() => {
  const cats = categories[activeTab.value] || []
  return cats.find(cat => cat.name === selectedPrimary.value)
})

const isParentInRow = (row) => {
  if (!selectedPrimary.value) return false
  return row.some(item => item.name === selectedPrimary.value)
}

const formatNow = () => {
  const now = new Date()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const hh = String(now.getHours()).padStart(2, '0')
  const mm = String(now.getMinutes()).padStart(2, '0')
  return `今天 ${hh}:${mm}`
}

const displayDateTime = ref(formatNow())

const handleTabChange = (tab) => {
  activeTab.value = tab
  const cats = categories[tab] || []
  if (cats.length > 0) {
    selectPrimary(cats[0])
  }
}

const selectPrimary = (cat) => {
  selectedPrimary.value = cat.name
  selectedSub.value = cat.children?.length > 0 ? cat.children[0].name : null
}

const selectSub = (sub) => {
  selectedSub.value = sub.name
}

const handleAccount = () => {
  uni.showToast({ title: '选择账户', icon: 'none' })
}

const setOperator = (op) => {
  console.log('Operator:', op)
}

const saveAndAgain = () => {
  if (!currentAmount.value || parseFloat(currentAmount.value) === 0) {
    return uni.showToast({ title: '请输入金额', icon: 'none' })
  }
  uni.showToast({ title: '已保存并再记一笔', icon: 'success' })
  currentAmount.value = '0'
  remark.value = ''
}

const confirmSave = () => {
  if (!currentAmount.value || parseFloat(currentAmount.value) === 0) {
    return uni.showToast({ title: '请输入金额', icon: 'none' })
  }
  uni.showToast({ title: '记账成功', icon: 'success' })
  setTimeout(() => {
    uni.navigateBack()
  }, 800)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
.page {
  height: 100vh;
  background: $background;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 1. 支出/收入分段选择器 */
.segmented-bar {
  padding: 24rpx 0;
  display: flex;
  justify-content: center;
  background: $background;
}

.segmented-picker {
  position: relative;
  width: 440rpx;
  height: 76rpx;
  background: #ECEFEC;
  border-radius: 38rpx;
  display: flex;
  padding: 6rpx;
  box-sizing: border-box;
}

.segmented-indicator {
  position: absolute;
  top: 6rpx;
  bottom: 6rpx;
  left: 6rpx;
  width: calc(50% - 6rpx);
  background: #FFFFFF;
  border-radius: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: transform 0.2s cubic-bezier(0.25, 1, 0.5, 1);
}

.income-active .segmented-indicator {
  transform: translateX(100%);
}

.segment-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  
  text {
    font-size: 28rpx;
    font-weight: 700;
    color: #8D938C;
    transition: color 0.2s;
  }
  
  &.active text {
    color: $primary;
  }
}

/* 主滚动区域（包含切换器和分类） */
.main-scroll {
  flex: 1;
  overflow-y: auto;
}

.scroll-content {
  padding: 16rpx 32rpx 32rpx;
}

.category-card {
  background: $surface-container-lowest;
  border-radius: 32rpx;
}

.category-row-group {
  margin-bottom: 40rpx;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  row-gap: 32rpx;
  column-gap: 16rpx;
  padding: 0 8rpx;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.category-icon-wrapper {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: #F1F3F0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease-out;
  padding: 12rpx;

  .category-icon {
    font-size: 36rpx;
  }

  &.active {
    background: $primary;
    box-shadow: 0 4rpx 14rpx rgba(36, 125, 109, 0.2);
  }
}

.category-label {
  font-size: 24rpx;
  color: #8D938C;
  font-weight: 500;
  line-height: 1.2;

  &.active {
    color: $primary;
    font-weight: 700;
  }
}

/* 子分类展示抽屉 (展开二级分类面板) */
.sub-drawer {
  width: 100%;
  margin-top: 24rpx;
  margin-left: 8rpx;
  margin-right: 8rpx;
  animation: slideDown 0.2s ease-out;
}

.sub-category-section {
  padding: 16rpx 16rpx;
  border-radius: 40rpx;
  background: $surface;
}

.sub-category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  row-gap: 32rpx;
}

.sub-category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.sub-icon-wrapper {
  width: 84rpx;
  height: 84rpx;
  border-radius: 50%;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;

  .sub-icon {
    font-size: 38rpx;
  }

  &.active {
    background: $primary;
    box-shadow: 0 4rpx 12rpx rgba(36, 125, 109, 0.2);
  }
}

.sub-label {
  font-size: 20rpx;
  color: #8D938C;

  &.active {
    color: $primary;
    font-weight: 700;
  }
}

/* 输入详情框 */
.info-card {
  margin: 24rpx 32rpx;
  padding: 32rpx;
  border-radius: 40rpx;
  background: #FFFFFF;
  border: 1rpx solid #EAEAEA;
}

.amount-display {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  padding-bottom: 24rpx;
}

.currency {
  font-size: 42rpx;
  font-weight: 700;
  color: $primary;
}

.amount-value {
  font-size: 64rpx;
  font-weight: 700;
  line-height: 1;
  color: $primary;
}

/* 收入状态时的专属颜色 */
.income-color {
  color: #C5A36A !important;
}

/* 录入标签设计 (精细 SVG 组装) */
.info-tags {
  display: flex;
  gap: 16rpx;
}

.info-tag {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 14rpx 24rpx;
  border-radius: 100rpx;
  background: #F1F3F0;
  flex-shrink: 0;

  .tag-svg {
    flex-shrink: 0;
  }

  &.remark {
    flex: 1;
    min-width: 0;
  }
}

.tag-text {
  font-size: 24rpx;
  color: #1E211F;
  font-weight: 500;
}

.tag-input {
  font-size: 24rpx;
  color: #1E211F;
  background: transparent;
  border: none;
  outline: none;
  width: 100%;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-8rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>