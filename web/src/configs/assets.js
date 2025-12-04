// 资产模块配置

// 资产类型
export const ASSET_TYPES = {
  BANK: 'bank',
  FUND: 'fund',
  STOCK: 'stock',
  REAL_ESTATE: 'real_estate',
  CASH: 'cash',
  OTHER: 'other'
};

// 资产类型显示配置
export const ASSET_TYPE_DISPLAY = {
  [ASSET_TYPES.BANK]: {
    name: '银行账户',
    icon: '/static/icons/bank.png',
    color: '#4a90e2'
  },
  [ASSET_TYPES.FUND]: {
    name: '基金',
    icon: '/static/icons/fund.png',
    color: '#50e3c2'
  },
  [ASSET_TYPES.STOCK]: {
    name: '股票',
    icon: '/static/icons/stock.png',
    color: '#f5a623'
  },
  [ASSET_TYPES.REAL_ESTATE]: {
    name: '房产',
    icon: '/static/icons/real_estate.png',
    color: '#7ed321'
  },
  [ASSET_TYPES.CASH]: {
    name: '现金',
    icon: '/static/icons/cash.png',
    color: '#9013fe'
  },
  [ASSET_TYPES.OTHER]: {
    name: '其他',
    icon: '/static/icons/other.png',
    color: '#bd10e0'
  }
};

// 资产添加表单配置
export const ASSET_FORM_CONFIG = {
  MIN_AMOUNT: 0.01,
  MAX_AMOUNT: 1000000000,
  CURRENCY_LIST: ['CNY', 'USD', 'HKD'],
  DEFAULT_CURRENCY: 'CNY'
};
