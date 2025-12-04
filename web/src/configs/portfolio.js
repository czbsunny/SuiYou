// 组合模块配置

// 组合类型
export const PORTFOLIO_TYPES = {
  DEFAULT: 'default',
  CUSTOM: 'custom',
  DCA: 'dca' // 定投组合
};

// 组合风险等级
export const RiskLevel = {
  CONSERVATIVE: 'CONSERVATIVE',
  STEADY: 'STEADY',
  BALANCED: 'BALANCED',
  GROWTH: 'GROWTH',
  AGGRESSIVE: 'AGGRESSIVE',
  CUSTOM: 'CUSTOM'
};

// 风险等级模板
export const RISK_TEMPLATES = [
  { 
    type: RiskLevel.AGGRESSIVE, 
    name: '摘星手', 
    element: '火',
    iconName: '/static/icons/flame.png',
    grayIconName: '/static/icons/flame-gray.png',
    color: 'text-red-500',
    bgColor: 'bg-red-500',
    lightColor: 'bg-red-50',
    desc: '如星火般炽热，博取未来，势成燎原。',
    detail: '【极致绚烂】精选行业龙头与赛道基金，追求高爆发收益，适合风险承受力极强的投资者。',
    tags: ['高收益', '赛道投资', '进取'],
    stars: { return: 5, risk: 5, stability: 1 } 
  },
  { 
    type: RiskLevel.GROWTH, 
    name: '登山阶', 
    element: '金',
    iconName: '/static/icons/pickaxe.png',
    grayIconName: '/static/icons/pickaxe-gray.png',
    color: 'text-yellow-600',
    bgColor: 'bg-yellow-600',
    lightColor: 'bg-yellow-50',
    desc: '如真金般坚韧，大浪淘沙，价值沉淀。',
    detail: '【披沙炼金】优选价值风格与成长白马。如攀登金山，步步为营，长期持有以获取优质企业的成长红利。',
    tags: ['价值成长', '指数增强', '长期持有'],
    stars: { return: 4, risk: 4, stability: 2 } 
  },
  { 
    type: RiskLevel.BALANCED, 
    name: '常青木', 
    element: '木',
    iconName: '/static/icons/trees.png',
    grayIconName: '/static/icons/trees-gray.png',
    color: 'text-green-600',
    bgColor: 'bg-green-600',
    lightColor: 'bg-green-50',
    desc: '如嘉木般葱郁，攻守兼备，穿越四季。',
    detail: '【基业长青】股债平衡配置，注重资产的生长性与韧性。在控制回撤的同时追求超越通胀的稳健增值。',
    tags: ['股债平衡', '稳健增值', '养老'],
    stars: { return: 3, risk: 3, stability: 3 } 
  },
  { 
    type: RiskLevel.STEADY, 
    name: '压舱石', 
    element: '土',
    iconName: '/static/icons/mountain.png',
    grayIconName: '/static/icons/mountain-gray.png',
    color: 'text-stone-600',
    bgColor: 'bg-stone-600',
    lightColor: 'bg-stone-50',
    desc: '如大地般厚重，稳健生息，守住基业。',
    detail: '【厚得载物】以纯债和固收类资产打底，构建家庭资产的安全垫。波动极低，注重资产的绝对安全。',
    tags: ['绝对收益', '抗通胀', '极低波动'],
    stars: { return: 2, risk: 1, stability: 5 } 
  },
  { 
    type: RiskLevel.CONSERVATIVE, 
    name: '源头水', 
    element: '水',
    iconName: '/static/icons/droplets.png',
    grayIconName: '/static/icons/droplets-gray.png',
    color: 'text-blue-500',
    bgColor: 'bg-blue-500',
    lightColor: 'bg-blue-50',
    desc: '如活水般灵动，随时取用，润泽日常。',
    detail: '【上善若水】主要配置货币基金与短期理财。保障资金的高流动性，随取随用，满足日常开销与应急需求。',
    tags: ['现金管理', '高流动性', '零钱理财'],
    stars: { return: 1, risk: 0, stability: 5 } 
  },
];

// 组合配置参数
export const PORTFOLIO_CONFIG = {
  MIN_ASSET_COUNT: 1,
  MAX_ASSET_COUNT: 20,
  MIN_WEIGHT: 0.01, // 最小权重 1%
  MAX_WEIGHT: 1.0,  // 最大权重 100%
  DEFAULT_REBALANCE_INTERVAL: 90 // 默认再平衡周期（天）
};

// 定投策略配置
export const DCA_CONFIG = {
  INTERVAL_OPTIONS: [
    { value: 1, label: '每天' },
    { value: 7, label: '每周' },
    { value: 14, label: '每两周' },
    { value: 30, label: '每月' }
  ],
  DEFAULT_INTERVAL: 30,
  MIN_AMOUNT: 100,
  MAX_AMOUNT: 100000
};
