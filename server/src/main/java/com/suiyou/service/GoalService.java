package com.suiyou.service;

import com.suiyou.dto.goal.CreateGoalDTO;
import com.suiyou.model.Goal;
import java.util.List;

public interface GoalService {
    /**
     * 获取用户的所有目标
     */
    List<Goal> getUserGoals(Long userId);
    
    /**
     * 创建新目标
     */
    Goal createGoal(CreateGoalDTO goalDTO, Long userId);
}