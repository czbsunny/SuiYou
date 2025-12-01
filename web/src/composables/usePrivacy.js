import { ref } from 'vue';

// 定义全局状态
const isPrivacyOn = ref(false);

export function usePrivacy() {
  // 切换隐私模式
  const togglePrivacy = () => {
    isPrivacyOn.value = !isPrivacyOn.value;
  };

  // 格式化数据的辅助函数
  // 如果隐私开启，返回 '****'，否则返回原值
  const formatPrivacy = (value, mask = '****') => {
    return isPrivacyOn.value ? mask : value;
  };

  return {
    isPrivacyOn,
    togglePrivacy,
    formatPrivacy
  };
}