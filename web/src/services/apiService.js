const BASE_URL = 'http://localhost:8080';

export const request = async (options) => {
  try {
    const token = uni.getStorageSync('token');
    
    const headers = {
      'content-type': 'application/json',
      ...options.header
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    let url = options.url;
    if (!url.startsWith('http')) {
        url = `${BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`;
    }

    // 🟢 修改开始：移除数组解构，直接 await 获取 response
    // UniApp Request (Standard Promise)
    const response = await uni.request({
        url: url,
        method: options.method || 'GET',
        header: headers,
        data: options.data
    });
    
    // 注意：在 Promise 模式下，如果网络请求失败（如断网），uni.request 会直接抛出异常，
    // 从而跳到下方的 catch 块，因此不需要在这里判断 if (error)。
    
    const res = {
        statusCode: response.statusCode,
        data: response.data,
        header: response.header
    };
    // 🟢 修改结束

    // Handle 401 Unauthorized
    if (res.statusCode === 401) {
      const errorDetail = res.data?.detail || '';
      // Check if it's a business logic error masquerading as 401
      const isBusinessError = errorDetail.includes('DataIntegrityViolationException') || 
                           errorDetail.includes('Column') || 
                           errorDetail.includes('cannot be null');
      
      if (isBusinessError) {
        return res;
      } else {
        uni.removeStorageSync('token');
        uni.removeStorageSync('userInfo');
        console.warn('Login expired');
        uni.showToast({ title: '登录已过期', icon: 'none' });
        // Optional: Redirect to login logic could go here
      }
    }
    
    return res;
  } catch (error) {
    // 这里会捕获网络错误或上述代码抛出的异常
    console.error('API请求错误:', error);
    throw error;
  }
};

export const post = (url, data) => request({ url, method: 'POST', data });

export const get = (url, params = {}) => {
  let fullUrl = url;
  const queryString = Object.keys(params)
    .filter(key => params[key] !== undefined && params[key] !== null)
    .map(key => `${key}=${params[key]}`)
    .join('&');
    
  if (queryString) {
    fullUrl += (fullUrl.includes('?') ? '&' : '?') + queryString;
  }

  return request({ url: fullUrl, method: 'GET' });
};

export const put = (url, data) => request({ url, method: 'PUT', data });
export const del = (url) => request({ url, method: 'DELETE' });

export const saveToken = (token) => {
  if (token) uni.setStorageSync('token', token);
};

export const clearToken = () => uni.removeStorageSync('token');
export const getToken = () => uni.getStorageSync('token') || null;
export const isLoggedIn = () => !!getToken();

export default { request, post, get, put, del, saveToken, clearToken, getToken, isLoggedIn };