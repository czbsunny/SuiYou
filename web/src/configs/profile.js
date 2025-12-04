// 个人资料模块配置

// 用户角色
export const USER_ROLES = {
  ADMIN: 'admin',
  USER: 'user',
  GUEST: 'guest'
};

// 通知类型
export const NOTIFICATION_TYPES = {
  SYSTEM: 'system',
  TRANSACTION: 'transaction',
  GOAL: 'goal',
  PORTFOLIO: 'portfolio'
};

// 通知配置
export const NOTIFICATION_CONFIG = {
  ENABLED_BY_DEFAULT: {
    [NOTIFICATION_TYPES.SYSTEM]: true,
    [NOTIFICATION_TYPES.TRANSACTION]: true,
    [NOTIFICATION_TYPES.GOAL]: true,
    [NOTIFICATION_TYPES.PORTFOLIO]: false
  }
};

// 隐私设置
export const PRIVACY_SETTINGS = {
  DEFAULT_OPTIONS: {
    hideBalance: false,
    hideTransactions: false,
    hidePortfolio: false,
    hideGoals: false
  }
};

// 语言设置
export const LANGUAGE_CONFIG = {
  SUPPORTED_LANGUAGES: [
    { value: 'zh-CN', label: '简体中文' },
    { value: 'en-US', label: 'English' }
  ],
  DEFAULT_LANGUAGE: 'zh-CN'
};

// 主题设置
export const THEME_CONFIG = {
  SUPPORTED_THEMES: [
    { value: 'light', label: '浅色主题' },
    { value: 'dark', label: '深色主题' },
    { value: 'auto', label: '跟随系统' }
  ],
  DEFAULT_THEME: 'light'
};
