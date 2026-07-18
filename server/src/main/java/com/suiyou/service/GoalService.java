package com.suiyou.service;

import com.suiyou.dto.goal.CreateGoalDTO;
import com.suiyou.dto.goal.GoalCategoryRespDTO;
import com.suiyou.dto.goal.GoalRespDTO;
import com.suiyou.dto.goal.GoalTemplateRespDTO;

import java.util.List;

public interface GoalService {
    List<GoalRespDTO> getUserGoals(Long userId);

    GoalRespDTO getGoalById(Long id, Long userId);

    GoalRespDTO createGoal(CreateGoalDTO goalDTO, Long userId);

    void deleteGoal(Long id, Long userId);

    List<GoalCategoryRespDTO> getGoalCategories();

    List<GoalTemplateRespDTO> getGoalTemplates(String categoryCode);
}