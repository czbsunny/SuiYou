// 通用配置

// 时间格式化配置
export const DATE_FORMATS = {
  DATE: 'YYYY-MM-DD',
  TIME: 'HH:mm:ss',
  DATETIME: 'YYYY-MM-DD HH:mm:ss',
  MONTH: 'YYYY-MM',
  YEAR: 'YYYY'
};

// 分页配置
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 10,
  PAGE_SIZE_OPTIONS: [10, 20, 50, 100]
};

// 网络请求配置
export const NETWORK = {
  TIMEOUT: 10000, // 请求超时时间（毫秒）
  RETRY_COUNT: 2  // 请求重试次数
};

// 缓存配置
export const CACHE = {
  DEFAULT_EXPIRE: 3600, // 默认缓存时间（秒）
  MAX_SIZE: 100         // 最大缓存条目数
};
