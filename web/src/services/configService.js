const STORAGE_KEY_PREFIX = 'cfg_data_';
const HASH_MAP_KEY = 'cfg_hashes';

/**
 * 配置管理服务
 * 提供配置的版本控制和本地缓存功能
 */
class ConfigService {
  constructor() {
		// 本地缓存的 hash 映射表 { moduleKey: hash }
		this.localHashes = uni.getStorageSync(HASH_MAP_KEY) || {};
    this.manifestPromise = null; // 用于防止重复请求 manifest
	}

  /**
	 * 获取指定配置模块
	 * @param {Array} keys 需要获取的配置键列表，如 ['banks', 'categories']
	 */
	async getConfigs(keys) {
      try {
          // 防止并发请求 manifest
          if (!this.manifestPromise) {
              this.manifestPromise = this._fetchManifest();
          }
          const manifest = await this.manifestPromise;
          // 请求完后清空，方便下次手动刷新
          this.manifestPromise = null;

          if (!manifest) return this._loadAllFromCache(keys);

          const { modules } = manifest;
          const keysToFetch = keys.filter(key => {
              const isExpired = this.localHashes[key] !== modules[key];
              const notInCache = !uni.getStorageSync(STORAGE_KEY_PREFIX + key);
              return isExpired || notInCache;
          });

          if (keysToFetch.length > 0) {
              const newData = await this._fetchRemoteConfigs(keysToFetch);
              Object.keys(newData).forEach(key => {
                  uni.setStorageSync(STORAGE_KEY_PREFIX + key, newData[key]);
                  this.localHashes[key] = modules[key];
              });
              uni.setStorageSync(HASH_MAP_KEY, this.localHashes);
          }

          return this._loadAllFromCache(keys);
      } catch (e) {
          this.manifestPromise = null;
          return this._loadAllFromCache(keys);
      }
  }

  async _fetchManifest() {
		return new Promise((resolve) => {
			uni.request({
				url: '/api/config/manifest', // 替换为实际地址
				method: 'GET',
				success: (res) => resolve(res.data),
				fail: () => resolve(null)
			});
		});
	}

  // 从后端批量获取配置数据
	async _fetchRemoteConfigs(keys) {
		return new Promise((resolve, reject) => {
			uni.request({
				url: '/api/config/fetch',
				method: 'POST',
				data: keys,
				success: (res) => resolve(res.data),
				fail: (err) => reject(err)
			});
		});
	}
}

export default new ConfigService();
