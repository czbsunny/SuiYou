import { useAuthStore } from '@/stores/auth'

const BASE_URL = process.env.NODE_ENV === 'development'
  ? 'http://localhost:8080'
  : 'https://www.zhitouying.cn'

const buildUrl = (url, params) => {
  let fullUrl = url.startsWith('http') ? url : `${BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
  if (params && Object.keys(params).length > 0) {
    const queryString = Object.entries(params)
      .filter(([_, v]) => v !== undefined && v !== null)
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
      .join('&')
    if (queryString) {
      fullUrl += (fullUrl.includes('?') ? '&' : '?') + queryString
    }
  }
  return fullUrl
}

const getHeaders = (options, token) => {
  const headers = {
    'Content-Type': 'application/json',
    ...options.header
  }
  if (token && !options.skipAuth) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
}

const performRequest = (url, method, data, header) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method,
      data,
      header,
      success: (res) => resolve(res),
      fail: (err) => reject(err)
    })
  })
}

export const request = async (options) => {
  const authStore = useAuthStore()

  if (!options.skipAuth && authStore.isLoggingIn) {
    await authStore.loginPromise
  }

  const { url, method = 'GET', data, params, skipAuth } = options
  const targetUrl = buildUrl(url, params)

  const doRequest = async () => {
    const headers = getHeaders(options, authStore.token)
    return await performRequest(targetUrl, method, data, headers)
  }

  try {
    let res = await doRequest()

    if (res.statusCode === 401 && !skipAuth) {
      console.log('Token失效，尝试静默登录...')
      const success = await authStore.silentLogin()
      
      if (success) {
        res = await doRequest()
        if (res.statusCode !== 401) return res
      }
      
      authStore.logout()
      uni.showToast({ title: '登录异常请重新进入小程序', icon: 'none' })
      throw new Error('Unauthorized')
    }

    return res
  } catch (error) {
    console.error('API请求错误:', error)
    throw error
  }
}

export const get = (url, params = {}, options = {}) => request({ ...options, url, method: 'GET', params })

export const post = (url, data, options = {}) => request({ ...options, url, method: 'POST', data })

export const put = (url, data, options = {}) => request({ ...options, url, method: 'PUT', data })

export const del = (url, options = {}) => request({ ...options, url, method: 'DELETE' })

export default request