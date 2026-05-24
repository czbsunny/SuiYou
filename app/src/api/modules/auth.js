import { get, post } from '../request'

export const login = (data) => {
  return post('/api/auth/login', data)
}

export const logout = () => {
  return post('/api/auth/logout')
}

export const register = (data) => {
  return post('/api/auth/register', data)
}

export const getUserInfo = () => {
  return get('/api/auth/userinfo')
}

export const updateUserInfo = (data) => {
  return post('/api/auth/update', data)
}

export const wechatLogin = (code) => {
  return post('/api/auth/wechat', { code })
}

export const douyinLogin = (code) => {
  return post('/api/auth/douyin', { code })
}

export const resetPassword = (data) => {
  return post('/api/auth/reset-password', data)
}

export const sendVerifyCode = (phone) => {
  return post('/api/auth/send-code', { phone })
}
