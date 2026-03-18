// API请求服务，统一处理token和请求拦截
import { 
  hasLocalDataToSync, 
  syncLocalDataToServer, 
  setSyncStatus, 
  getSyncStatus 
} from './localDataService.js';

/**
 * 发起请求，自动添加token到请求头
 * @param {Object} options - 请求选项
 * @returns {Promise} 请求结果
 */
export const request = async (options) => {
  try {
    const token = uni.getStorageSync('token');
    
    // 如果有token，添加到请求头
    if (token) {
      options.header = options.header || {};
      options.header['Authorization'] = `Bearer ${token}`;
    }
    
    // 设置默认的content-type
    if (!options.header?.['content-type']) {
      options.header = options.header || {};
      options.header['content-type'] = 'application/json';
    }
    
    // 设置默认的超时时间（如果未指定）
    if (!options.timeout) {
      options.timeout = 5000; // 默认 5 秒超时
    }
    
    // 发送请求
    const res = await uni.request(options);
    
    // 检查响应状态
    if (res.statusCode === 401) {
      // 检查响应数据是否包含业务相关的错误信息，而不是真正的认证过期
      const errorDetail = res.data?.detail || '';
      const isBusinessError = errorDetail.includes('DataIntegrityViolationException') || 
                           errorDetail.includes('Column') || 
                           errorDetail.includes('cannot be null');
      
      if (isBusinessError) {
        // 业务相关的401错误，不清除token和跳转到登录页
        console.log('业务相关的401错误，不进行退出登录处理');
        // 直接返回响应，让业务代码处理具体的错误
        return res;
      } else {
        // 真正的认证过期错误，清除token并跳转到登录页
        uni.removeStorageSync('token');
        uni.removeStorageSync('userInfo');
        
        // 提示用户登录过期
        uni.showToast({
          title: '登录已过期，请重新登录',
          icon: 'none'
        });
        
        // 延迟跳转到登录页
        setTimeout(() => {
          // 跳转到登录页
          // #ifdef H5
          uni.navigateTo({
            url: '/pages/auth/login-h5'
          });
          // #endif
          // #ifndef H5
          uni.navigateTo({
            url: '/pages/auth/login-wechat'
          });
          // #endif
        }, 1500);
        
        // 抛出错误
        throw new Error('登录已过期');
      }
    }
    
    return res;
  } catch (error) {
    console.error('API请求错误:', error);
    throw error;
  }
};

/**
 * POST请求
 * @param {string} url - 请求地址
 * @param {Object} data - 请求数据
 * @returns {Promise} 请求结果
 */
export const post = (url, data) => {
  return request({
    url,
    method: 'POST',
    data
  });
};

/**
 * GET请求
 * @param {string} url - 请求地址
 * @param {Object} params - 请求参数
 * @returns {Promise} 请求结果
 */
export const get = (url, params = {}) => {
  let query = Object.entries(params)
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&');
  if (query) url += (url.includes('?') ? '&' : '?') + query;

  return request({
    url,
    method: 'GET'
  });
};


/**
 * PUT请求
 * @param {string} url - 请求地址
 * @param {Object} data - 请求数据
 * @returns {Promise} 请求结果
 */
export const put = (url, data) => {
  return request({
    url,
    method: 'PUT',
    data
  });
};

/**
 * DELETE请求
 * @param {string} url - 请求地址
 * @returns {Promise} 请求结果
 */
export const del = (url) => {
  return request({
    url,
    method: 'DELETE'
  });
};

/**
 * 保存token到本地缓存
 * @param {string} token - token值
 */
export const saveToken = (token) => {
  if (token) {
    uni.setStorageSync('token', token);
    
    // 检查是否有本地数据需要同步
    if (hasLocalDataToSync()) {
      console.log('检测到本地数据，准备在登录后同步');
      // 标记需要同步，但不立即执行，让登录页面处理
      setSyncStatus({ needsSync: true, isSyncing: false });
    }
  }
};

/**
 * 清除token缓存
 */
export const clearToken = () => {
  uni.removeStorageSync('token');
};

/**
 * 检查是否需要同步本地数据
 * @returns {boolean} 是否需要同步
 */
export const needsDataSync = () => {
  const syncStatus = getSyncStatus();
  return syncStatus.needsSync === true && syncStatus.isSyncing !== true;
};

/**
 * 执行本地数据同步
 * @param {Function} createAccountFn - 创建账户的API函数
 * @param {Function} addHoldingFn - 添加持仓的API函数
 * @returns {Promise<Object>} 同步结果
 */
export const performDataSync = async (createAccountFn, addHoldingFn) => {
  try {
    setSyncStatus({ needsSync: true, isSyncing: true });
    const result = await syncLocalDataToServer(createAccountFn, addHoldingFn);
    
    if (result.success) {
      setSyncStatus({ needsSync: false, isSyncing: false });
    } else {
      setSyncStatus({ needsSync: true, isSyncing: false });
    }
    
    return result;
  } catch (error) {
    console.error('执行数据同步失败:', error);
    setSyncStatus({ needsSync: true, isSyncing: false });
    return {
      success: false,
      message: `同步失败: ${error.message}`,
      syncedAccounts: 0,
      syncedHoldings: 0
    };
  }
};

/**
 * 获取当前token
 * @returns {string|null} token值
 */
export const getToken = () => {
  return uni.getStorageSync('token') || null;
};

/**
 * 检查是否已登录
 * @returns {boolean} 是否已登录
 */
export const isLoggedIn = () => {
  return !!getToken();
};

export default {
  request,
  post,
  get,
  put,
  del,
  saveToken,
  clearToken,
  getToken,
  isLoggedIn,
  needsDataSync,
  performDataSync
};