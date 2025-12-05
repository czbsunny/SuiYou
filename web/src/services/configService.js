import { get } from './apiService';

// 本地存储键名
const CONFIG_STORAGE_KEY = 'system_config';
const CONFIG_VERSION_KEY = 'system_config_version';

/**
 * 配置管理服务
 * 提供配置的版本控制和本地缓存功能
 */
class ConfigService {
  /**
   * 检查配置版本
   * @returns {Promise<boolean>} 是否需要更新配置
   */
  async checkConfigVersion() {
    try {
      const response = await get('/sys/config/version');
      if (response.statusCode === 200) {
        const serverVersion = response.data.version;
        const localVersion = uni.getStorageSync(CONFIG_VERSION_KEY);
        
        return serverVersion !== localVersion;
      }
      return true; // 默认需要更新
    } catch (error) {
      console.error('检查配置版本失败:', error);
      return false; // 网络错误时使用本地缓存
    }
  }

  /**
   * 获取最新配置
   * @param {boolean} forceUpdate 是否强制更新
   * @returns {Promise<Object>} 配置对象
   */
  async getConfig(forceUpdate = false) {
    // 先尝试从本地获取配置
    const localConfig = uni.getStorageSync(CONFIG_STORAGE_KEY);
    
    // 如果不需要强制更新且本地有配置
    if (!forceUpdate && localConfig) {
      return localConfig;
    }
    
    // 检查版本是否需要更新
    const needUpdate = await this.checkConfigVersion();
    
    if (needUpdate || forceUpdate) {
      try {
        // 从服务器获取全量配置
        const response = await get('/sys/config/all');
        if (response.statusCode === 200) {
          const newConfig = response.data;
          
          // 保存到本地存储
          uni.setStorageSync(CONFIG_STORAGE_KEY, newConfig);
          uni.setStorageSync(CONFIG_VERSION_KEY, newConfig.version);
          
          return newConfig;
        }
      } catch (error) {
        console.error('获取配置失败:', error);
        // 如果获取失败且本地有配置，使用本地配置
        if (localConfig) {
          return localConfig;
        }
        throw error;
      }
    }
    
    return localConfig || {};
  }

  /**
   * 获取特定配置项
   * @param {string} key 配置项键名
   * @param {any} defaultValue 默认值
   * @returns {Promise<any>} 配置项值
   */
  async getConfigItem(key, defaultValue = null) {
    const config = await this.getConfig();
    return config[key] || defaultValue;
  }

  /**
   * 清除本地配置缓存
   */
  clearCache() {
    uni.removeStorageSync(CONFIG_STORAGE_KEY);
    uni.removeStorageSync(CONFIG_VERSION_KEY);
  }

  /**
   * 获取账户创建相关配置
   * @returns {Promise<Object>} 账户创建配置
   */
  async getAccountCreationConfig() {
    return this.getConfigItem('accountCreation', {});
  }

  /**
   * 获取目标创建相关配置
   * @returns {Promise<Object>} 目标创建配置
   */
  async getGoalCreationConfig() {
    return this.getConfigItem('goalCreation', {});
  }
}

export default new ConfigService();
