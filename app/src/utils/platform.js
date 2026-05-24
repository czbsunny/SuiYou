export const usePlatform = () => {
  const systemInfo = uni.getSystemInfoSync()
  const platform = systemInfo.platform

  const isWechat = platform === 'wxwork' || platform === 'weixin'
  const isDouyin = platform === 'ttworker' || platform === 'toutiao'
  const isAndroid = platform === 'android'
  const isIOS = platform === 'ios'
  const isH5 = platform === 'h5' || !platform

  return {
    platform,
    isWechat,
    isDouyin,
    isAndroid,
    isIOS,
    isH5,
    isApp: isAndroid || isIOS,
    systemInfo
  }
}

export const getLoginMethod = () => {
  const { isWechat, isDouyin, isApp } = usePlatform()

  if (isWechat) {
    return 'wechat'
  } else if (isDouyin) {
    return 'douyin'
  } else if (isApp) {
    return 'app'
  } else {
    return 'h5'
  }
}

export const getShareMethod = () => {
  const { isWechat, isDouyin, isApp } = usePlatform()

  if (isWechat) {
    return 'wechat'
  } else if (isDouyin) {
    return 'douyin'
  } else if (isApp) {
    return 'app'
  } else {
    return 'h5'
  }
}

export const getPaymentMethod = () => {
  const { isWechat, isDouyin, isApp } = usePlatform()

  if (isWechat) {
    return 'wechat_pay'
  } else if (isDouyin) {
    return 'douyin_pay'
  } else if (isApp) {
    return 'app_pay'
  } else {
    return 'h5_pay'
  }
}

export const getUserInfoMethod = () => {
  const { isWechat, isDouyin, isApp } = usePlatform()

  if (isWechat) {
    return 'wechat'
  } else if (isDouyin) {
    return 'douyin'
  } else if (isApp) {
    return 'app'
  } else {
    return 'h5'
  }
}
