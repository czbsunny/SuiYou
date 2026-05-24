export const validatePhone = (phone) => {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

export const validateEmail = (email) => {
  const reg = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/
  return reg.test(email)
}

export const validateIdCard = (idCard) => {
  const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  return reg.test(idCard)
}

export const validatePassword = (password) => {
  return password.length >= 6 && password.length <= 20
}

export const validateAmount = (amount) => {
  const reg = /^\d+(\.\d{1,2})?$/
  return reg.test(amount) && Number(amount) > 0
}

export const validateRequired = (value) => {
  if (value === null || value === undefined) return false
  if (typeof value === 'string') return value.trim().length > 0
  return true
}

export const validateLength = (value, min, max) => {
  if (!value) return false
  const length = value.toString().length
  if (min !== undefined && length < min) return false
  if (max !== undefined && length > max) return false
  return true
}

export const validateUrl = (url) => {
  const reg = /^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)$/
  return reg.test(url)
}
