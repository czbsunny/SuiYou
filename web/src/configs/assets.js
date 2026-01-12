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
    icon: '/static/assets/liquid.png',
    iconGray: '/static/assets/liquid-gray.png',
    color: '#2A806C'
  },
  [ASSET_CATEGORY.INVEST]: {
    name: '投资理财',
    icon: '/static/assets/invest.png',
    iconGray: '/static/assets/invest-gray.png',
    color: '#D97706'
  },
  [ASSET_CATEGORY.FIXED]: {
    name: '固定资产',
    icon: '/static/assets/fixed.png',
    iconGray: '/static/assets/fixed-gray.png',
    color: '#4F46E5'
  },
  [ASSET_CATEGORY.OTHER]: {
    name: '其他资产',
    icon: '/static/assets/receipt.png',
    iconGray: '/static/assets/receipt-gray.png',
    color: '#0891B2'
  },
  [ASSET_CATEGORY.LOAN]: {
    name: '负债贷款',
    icon: '/static/assets/debt.png',
    iconGray: '/static/assets/debt-gray.png',
    color: '#374151'
  }
};

// 资产添加表单配置
export const ASSET_FORM_CONFIG = {
  MIN_AMOUNT: 0.01,
  MAX_AMOUNT: 1000000000,
  CURRENCY_LIST: ['CNY', 'USD', 'HKD'],
  DEFAULT_CURRENCY: 'CNY'
};

export const ASSET_INSTITUTION_TYPE = {
  BANK: 'BANK',
  INTERNET_BANK: 'INTERNET_BANK',
  FOREIGN_BANK: 'FOREIGN_BANK',
  SECURITY: 'SECURITY',
  INSURANCE: 'INSURANCE',
  PAYMENT: 'PAYMENT',
  FINTECH: 'FINTECH',
  FUND_PLATFORM: 'FUND_PLATFORM',
  OTHER: 'OTHER'
};

export const ASSET_INSTITUTION_DISPLAY = {
  [ASSET_INSTITUTION_TYPE.BANK]: {
    name: '传统银行',
    icon: '/static/assets/bank.png',
    iconGray: '/static/assets/bank-gray.png',
    color: '#E31D1A'
  },
  [ASSET_INSTITUTION_TYPE.INTERNET_BANK]: {
    name: '互联网银行',
    icon: '/static/assets/internet-bank.png',
    color: '#0FBAD8'
  },
  [ASSET_INSTITUTION_TYPE.FOREIGN_BANK]: {
    name: '外资银行',
    icon: '/static/assets/foreign-bank.png',
    color: '#003366'
  },
  [ASSET_INSTITUTION_TYPE.SECURITY]: {
    name: '证券公司',
    icon: '/static/assets/security.png',
    color: '#334155'
  },
  [ASSET_INSTITUTION_TYPE.INSURANCE]: {
    name: '保险机构',
    icon: '/static/assets/insurance.png',
    color: '#059669'
  },
  [ASSET_INSTITUTION_TYPE.PAYMENT]: {
    name: '支付平台',
    icon: '/static/assets/payment.png',
    color: '#1677FF'
  },
  [ASSET_INSTITUTION_TYPE.FINTECH]: {
    name: '金融科技',
    icon: '/static/assets/fintech.png',
    color: '#6366F1'
  },
  [ASSET_INSTITUTION_TYPE.FUND_PLATFORM]: {
    name: '基金平台',
    icon: '/static/assets/fund-platform.png',
    color: '#F59E0B'
  },
  [ASSET_INSTITUTION_TYPE.OTHER]: {
    name: '其他平台',
    icon: '/static/assets/other.png',
    color: '#9CA3AF'
  }
}