package com.suiyou.service;

import com.suiyou.dto.GoalCategoryRespDTO;

import java.util.List;

public interface SysGoalConfigService {
    /**
     * 获取所有目标分类
     * @return 目标分类列表
     */
    List<GoalCategoryRespDTO> getAllCategories();
}
