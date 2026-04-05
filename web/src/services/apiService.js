import { useAuthStore } from '@/stores/auth.js'

const BASE_URL = 'http://localhost:8080';
const PROD_URL = 'https://www.zhitouying.cn';

/**
 * 辅助函数：构建完整 URL
 */
const buildUrl = (url, params) => {
  // 对于 /api/fund/search 使用生产环境 URL
  if (url === '/api/fund/search') {
    let fullUrl = `${PROD_URL}${url.startsWith('/') ? '' : '/'}${url}`;
    if (params && Object.keys(params).length > 0) {
      const queryString = Object.entries(params)
        .filter(([_, v]) => v !== undefined && v !== null)
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
        .join('&');
      if (queryString) {
        fullUrl += (fullUrl.includes('?') ? '&' : '?') + queryString;
      }
    }
    return fullUrl;
  }
  
  // 其他 API 使用原有逻辑
  let fullUrl = url.startsWith('http') ? url : `${BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`;
  if (params && Object.keys(params).length > 0) {
    const queryString = Object.entries(params)
      .filter(([_, v]) => v !== undefined && v !== null)
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
      .join('&');
    if (queryString) {
      fullUrl += (fullUrl.includes('?') ? '&' : '?') + queryString;
    }
  }
  return fullUrl;
};

/**
 * 辅助函数：获取请求头
 */
const getHeaders = (options, token) => {
  const headers = {
    'content-type': 'application/json',
    ...options.header
  };
  if (token && !options.skipAuth) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
};

/**
 * 核心请求函数 (封装 uni.request)
 */
const performRequest = (url, method, data, header) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method,
      data,
      header,
      success: (res) => resolve(res),
      fail: (err) => reject(err)
    });
  });
};

/**
 * 统一请求入口
 */
export const request = async (options) => {
  const authStore = useAuthStore();
  
  // 1. 登录锁等待
  if (!options.skipAuth && authStore.isLoggingIn) {
    await authStore.loginPromise;
  }

  const { url, method = 'GET', data, params, skipAuth } = options;
  const targetUrl = buildUrl(url, params);
  
  // 2. 发起请求逻辑（定义为闭包方便重试）
  const doRequest = async () => {
    const headers = getHeaders(options, authStore.token);
    return await performRequest(targetUrl, method, data, headers);
  };

  try {
    let res = await doRequest();

    // 3. 处理 401 逻辑
    if (res.statusCode === 401 && !skipAuth) {
      console.log('Token失效，尝试静默登录...');
      const success = await authStore.silentLogin();
      
      if (success) {
        res = await doRequest(); // 重试
        if (res.statusCode !== 401) return res; // 重试成功
      }
      
      // 静默登录失败或重试后依然401
      authStore.logout();
      uni.showToast({ title: '登录异常请重新进入小程序', icon: 'none' });
      throw new Error('Unauthorized');
    }

    return res;
  } catch (error) {
    console.error('API请求错误:', error);
    throw error;
  }
};

/**
 * 快捷方法封装
 */
export const get = (url, params = {}, options = {}) => request({ ...options, url, method: 'GET', params });
export const post = (url, data, options = {}) => request({ ...options, url, method: 'POST', data });
export const put = (url, data, options = {}) => request({ ...options, url, method: 'PUT', data });
export const del = (url, options = {}) => request({ ...options, url, method: 'DELETE' });

export const isLoggedIn = () => useAuthStore().isLoggedIn;

export default { request, get, post, put, del, isLoggedIn };