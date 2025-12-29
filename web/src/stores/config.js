import { defineStore } from 'pinia'
import configService from '@/services/configService.js'

export const useConfigStore = defineStore('config', {
  state: () => ({
    assetCategories: [], // 资产分类
    goalCategories: [], // 目标分类
    goalTemplates: [], // 目标模板
    institutionData: [], // 机构数据
    relations: [], // 关联关系
    isLoaded: false // 是否加载完成
  }),
  getters: {
    // Getter: 直接获取 5 大类，给轮播组件用
    topCategories: (state) => {
      return state.assetCategories.map(item => ({
        id: item.id,
        name: item.name,
        categoryCode: item.categoryCode,
        iconUrl: item.iconUrl,
        color: item.color,
        sortOrder: item.sortOrder,
      }))
    },
    
    getSubCategoriesByCode: (state) => (code) => {
      const category = state.assetCategories.find(item => item.categoryCode === code);

      if (category) {
        return category.children || [];
      }
      return [];
    }
  },

  actions: {
    // 这就是你刚才问的 initConfigs
    async initConfigs() {
      // 这里的 keys 对应后端 manifest 里的 modules 的 key
      const keys = ['asset_category', 'goal_category', 'goal_template', 'institution_data', 'relation'];
      const data = await configService.getConfigs(keys);
      
      this.assetCategories = data.asset_category || [];
      this.goalCategories = data.goal_category || [];
      this.goalTemplates = data.goal_template || [];
      this.institutionData = data.institution_data || [];
      this.relations = data.relation || [];

      return data;
    }
  }
})