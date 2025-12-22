package com.suiyou.service;

import com.suiyou.dto.GoalCategoryRespDTO;

import java.util.List;

public interface SysGoalCategoryService {
    List<GoalCategoryRespDTO> getGoalCategories();
}
