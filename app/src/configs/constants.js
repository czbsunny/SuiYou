export const API_BASE_URL = process.env.NODE_ENV === 'development'
  ? 'http://127.0.0.1:8080'
  : 'https://api.suiyou.com'

export const UPLOAD_URL = `${API_BASE_URL}/api/upload`

export const TIMEOUT = 30000

export const REQUEST_SUCCESS_CODE = [0, 200]

export const STORAGE_TOKEN_KEY = 'token'

export const STORAGE_USER_INFO_KEY = 'userInfo'