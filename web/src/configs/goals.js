// 目标模块配置

// 目标类型
export const GOAL_TYPES = {
  RETIREMENT: 'retirement',
  HOUSE: 'house',
  EDUCATION: 'education',
  CAR: 'car',
  TRAVEL: 'travel',
  EMERGENCY: 'emergency',
  OTHER: 'other'
};

// 目标类型显示配置
export const GOAL_TYPE_DISPLAY = {
  [GOAL_TYPES.RETIREMENT]: {
    name: '退休规划',
    icon: '/static/icons/retirement.png',
    color: '#4a90e2'
  },
  [GOAL_TYPES.HOUSE]: {
    name: '购房计划',
    icon: '/static/icons/house.png',
    color: '#50e3c2'
  },
  [GOAL_TYPES.EDUCATION]: {
    name: '教育基金',
    icon: '/static/icons/education.png',
    color: '#f5a623'
  },
  [GOAL_TYPES.CAR]: {
    name: '购车计划',
    icon: '/static/icons/car.png',
    color: '#7ed321'
  },
  [GOAL_TYPES.TRAVEL]: {
    name: '旅行计划',
    icon: '/static/icons/travel.png',
    color: '#9013fe'
  },
  [GOAL_TYPES.EMERGENCY]: {
    name: '应急储备',
    icon: '/static/icons/emergency.png',
    color: '#bd10e0'
  },
  [GOAL_TYPES.OTHER]: {
    name: '其他目标',
    icon: '/static/icons/other-goal.png',
    color: '#50e3c2'
  }
};

// 目标进度显示配置
export const GOAL_PROGRESS_CONFIG = {
  COLORS: ['#50e3c2', '#f5a623', '#ef4444'],
  THRESHOLDS: [0.3, 0.7] // 进度阈值：小于30%一种颜色，30%-70%一种颜色，大于70%一种颜色
};

// 目标计算器配置
export const GOAL_CALCULATOR_CONFIG = {
  MIN_TARGET_AMOUNT: 1000,
  MAX_TARGET_AMOUNT: 10000000,
  MIN_CURRENT_AMOUNT: 0,
  MIN_YEARS: 1,
  MAX_YEARS: 50,
  DEFAULT_EXPECTED_RETURN: 0.05 // 默认预期年化收益率 5%
};
