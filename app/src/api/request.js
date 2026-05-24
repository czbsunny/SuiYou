const BASE_URL = process.env.NODE_ENV === 'development'
  ? 'http://127.0.0.1:8000'
  : 'https://api.suiyou.com'

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 0 || res.data.success) {
            resolve(res.data.data)
          } else {
            handleError(res.data, reject)
          }
        } else if (res.statusCode === 401) {
          handleUnauthorized(reject)
        } else if (res.statusCode === 403) {
          handleForbidden(reject)
        } else if (res.statusCode >= 500) {
          handleServerError(res, reject)
        } else {
          reject(res.data || { message: '请求失败' })
        }
      },
      fail: (err) => {
        handleNetworkError(err, reject)
      }
    })
  })
}

const handleError = (data, reject) => {
  const message = data.message || data.msg || '请求失败'
  uni.showToast({
    title: message,
    icon: 'none',
    duration: 2000
  })
  reject({ code: data.code, message })
}

const handleUnauthorized = (reject) => {
  uni.showToast({
    title: '登录已过期，请重新登录',
    icon: 'none',
    duration: 2000
  })
  uni.removeStorageSync('token')
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/profile/index' })
  }, 2000)
  reject({ code: 401, message: '未授权' })
}

const handleForbidden = (reject) => {
  uni.showToast({
    title: '没有权限访问该资源',
    icon: 'none',
    duration: 2000
  })
  reject({ code: 403, message: '禁止访问' })
}

const handleServerError = (res, reject) => {
  uni.showToast({
    title: '服务器错误，请稍后重试',
    icon: 'none',
    duration: 2000
  })
  reject({ code: res.statusCode, message: '服务器错误' })
}

const handleNetworkError = (err, reject) => {
  uni.showToast({
    title: '网络连接失败，请检查网络',
    icon: 'none',
    duration: 2000
  })
  reject({ code: -1, message: '网络错误' })
}

export const get = (url, data, options = {}) => {
  return request({
    url,
    method: 'GET',
    data,
    ...options
  })
}

export const post = (url, data, options = {}) => {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  })
}

export const put = (url, data, options = {}) => {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

export const del = (url, data, options = {}) => {
  return request({
    url,
    method: 'DELETE',
    data,
    ...options
  })
}

export default request
