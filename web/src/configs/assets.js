// 资产模块配置

export const ASSET_TYPES = {
  ASSET: 'asset',
  LIABILITY: 'liability'
};

export const ASSET_CATEGORY = {
  LIQUID: 'LIQUID',
  INVEST: 'INVEST',
  FIXED: 'FIXED',
  OTHER: 'OTHER_ASSET',
  LOAN: 'LOAN'
};

export const ASSET_CATEGORY_DISPLAY = {
  [ASSET_CATEGORY.LIQUID]: {
    name: '流动资产',
    icon: '/static/assets/cash.png',
    iconGray: '/static/assets/cash-gray.png',
    color: '#5e6266ff'
  },
  [ASSET_CATEGORY.INVEST]: {
    name: '投资理财',
    icon: '/static/assets/invest.png',
    iconGray: '/static/assets/invest-gray.png',
    color: '#50e3c2'
  },
  [ASSET_CATEGORY.FIXED]: {
    name: '固定资产',
    icon: '/static/assets/fixed.png',
    iconGray: '/static/assets/fixed-gray.png',
    color: '#f5a623'
  },
  [ASSET_CATEGORY.OTHER]: {
    name: '其他资产',
    icon: '/static/assets/receipt.png',
    iconGray: '/static/assets/receipt-gray.png',
    color: '#9013fe'
  },
  [ASSET_CATEGORY.LOAN]: {
    name: '负债贷款',
    icon: '/static/assets/debt.png',
    iconGray: '/static/assets/debt-gray.png',
    color: '#9013fe'
  }
};


// 资产添加表单配置
export const ASSET_FORM_CONFIG = {
  MIN_AMOUNT: 0.01,
  MAX_AMOUNT: 1000000000,
  CURRENCY_LIST: ['CNY', 'USD', 'HKD'],
  DEFAULT_CURRENCY: 'CNY'
};
