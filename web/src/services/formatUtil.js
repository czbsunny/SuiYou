/**
 * 格式化工具类
 * 提供各种数据格式化功能
 */

/**
 * 格式化货币
 * @param {number|string} value - 要格式化的值
 * @param {string} currencySymbol - 货币符号，默认为"¥"
 * @param {number} minFractionDigits - 最小小数位数
 * @param {number} maxFractionDigits - 最大小数位数
 * @returns {string} 格式化后的货币字符串
 */
export function formatCurrency(value, currencySymbol = "¥", minFractionDigits = 2, maxFractionDigits = 2) {
  try {
    const num = Number(value);
    return currencySymbol + num.toLocaleString("zh-CN", {
      minimumFractionDigits: minFractionDigits,
      maximumFractionDigits: maxFractionDigits
    });
  } catch (error) {
    console.error("货币格式化失败:", error);
    return currencySymbol + "0.00";
  }
}

/**
 * 格式化数字
 * @param {number|string} value - 要格式化的值
 * @param {Object} options - 格式化选项
 * @returns {string} 格式化后的数字字符串
 */
export function formatNumber(value, options = {}) {
  try {
    const num = Number(value || 0);
    const defaultOptions = {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
      useGrouping: true
    };
    const mergedOptions = { ...defaultOptions, ...options };
    return num.toLocaleString("zh-CN", mergedOptions);
  } catch (error) {
    console.error("数字格式化失败:", error);
    return "0";
  }
}

/**
 * 格式化百分比
 * @param {number|string} value - 要格式化的值（小数形式，如0.1表示10%）
 * @param {number} digits - 小数位数
 * @returns {string} 格式化后的百分比字符串
 */
export function formatPercent(value, digits = 2) {
  try {
    const num = Number(value);
    return num.toFixed(digits) + "%";
  } catch (error) {
    console.error("百分比格式化失败:", error);
    return "0.00%";
  }
}

/**
 * 格式化日期
 * @param {Date|string|number} date - 要格式化的日期
 * @param {string} format - 格式化模板，默认为"YYYY-MM-DD"
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = "YYYY-MM-DD") {
  try {
    const d = new Date(date);
    if (isNaN(d.getTime())) {
      return "";
    }
    
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    const hours = String(d.getHours()).padStart(2, "0");
    const minutes = String(d.getMinutes()).padStart(2, "0");
    const seconds = String(d.getSeconds()).padStart(2, "0");
    
    return format
      .replace("YYYY", year)
      .replace("MM", month)
      .replace("DD", day)
      .replace("HH", hours)
      .replace("mm", minutes)
      .replace("ss", seconds);
  } catch (error) {
    console.error("日期格式化失败:", error);
    return "";
  }
}

/**
 * 格式化相对时间
 * @param {Date|string|number} date - 要计算相对时间的日期
 * @returns {string} 相对时间描述
 */
export function formatRelativeTime(date) {
  try {
    const now = new Date();
    const target = new Date(date);
    const diff = now - target;
    
    const minute = 60 * 1000;
    const hour = 60 * minute;
    const day = 24 * hour;
    const month = 30 * day;
    const year = 365 * day;
    
    if (diff < minute) {
      return "刚刚";
    } else if (diff < hour) {
      return Math.floor(diff / minute) + "分钟前";
    } else if (diff < day) {
      return Math.floor(diff / hour) + "小时前";
    } else if (diff < month) {
      return Math.floor(diff / day) + "天前";
    } else if (diff < year) {
      return Math.floor(diff / month) + "个月前";
    } else {
      return Math.floor(diff / year) + "年前";
    }
  } catch (error) {
    console.error("相对时间格式化失败:", error);
    return "";
  }
}

/**
 * 隐藏手机号中间4位
 * @param {string} phone - 手机号
 * @returns {string} 隐藏后的手机号
 */
export function maskPhoneNumber(phone) {
  if (!phone || typeof phone !== 'string') return '';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
}

/**
 * 隐藏银行卡号
 * @param {string} cardNo - 银行卡号
 * @returns {string} 隐藏后的银行卡号
 */
export function maskBankCard(cardNo) {
  if (!cardNo || typeof cardNo !== 'string') return '';
  const len = cardNo.length;
  if (len <= 8) return cardNo;
  const start = cardNo.substring(0, 4);
  const end = cardNo.substring(len - 4);
  return start + ' **** **** ' + end;
}