package com.suiyou.service;

import com.suiyou.dto.goal.*;

import java.util.List;

public interface SysGoalConfigService {
    /**
     * 获取所有目标分类
     * @return 目标分类列表
     */
    List<GoalCategoryRespDTO> getAllCategories();

    /**
     * 获取所有目标模板
     * @return 目标模板列表
     */
    List<GoalTemplateRespDTO> getAllTemplates();

    /**
     * 初始化目标分类
     * @param dtos 目标分类初始化数据
     */
    void initCategories(List<GoalCategoryInitDTO> dtos);

    /**
     * 初始化目标模板
     * @param dtos 目标模板初始化数据
     */
    void initTemplates(List<GoalTemplateInitDTO> dtos);

}
