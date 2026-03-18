// 格式化工具函数

/**
 * 格式化货币数值
 * @param {number|string} value - 要格式化的数值
 * @param {string} currencySymbol - 货币符号，默认为¥
 * @param {number} minFractionDigits - 最小小数位数
 * @param {number} maxFractionDigits - 最大小数位数
 * @returns {string} 格式化后的货币字符串
 */
export function formatCurrency(value, currencySymbol = '¥', minFractionDigits = 2, maxFractionDigits = 2) {
  if (value === null || value === undefined || value === '' || isNaN(Number(value))) {
    return 'NaN';
  }

  const num = Number(value);
  const roundedNum = Number(num.toFixed(maxFractionDigits));

  return currencySymbol + roundedNum.toLocaleString('zh-CN', {
    minimumFractionDigits: minFractionDigits,
    maximumFractionDigits: maxFractionDigits
  });
}

/**
 * 格式化百分比
 * @param {number|string} value - 要格式化的数值（小数形式，如0.05表示5%）
 * @param {number} digits - 小数位数
 * @returns {string} 格式化后的百分比字符串
 */
export function formatPercentage(value, digits = 2) {
  try {
    const num = Number(value || 0);
    return num.toLocaleString('zh-CN', {
      minimumFractionDigits: digits,
      maximumFractionDigits: digits
    }) + '%';
  } catch (error) {
    console.error('百分比格式化失败:', error);
    return '0.00%';
  }
}

/**
 * 格式化数字（通用）
 * @param {number|string} value - 要格式化的数值
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
    return num.toLocaleString('zh-CN', mergedOptions);
  } catch (error) {
    console.error('数字格式化失败:', error);
    return '0';
  }
}