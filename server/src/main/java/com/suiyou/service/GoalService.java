package com.suiyou.service;

import com.suiyou.dto.goal.CreateGoalDTO;
import com.suiyou.dto.goal.GoalRespDTO;
import java.util.List;

public interface GoalService {
    /**
     * 获取用户的所有目标
     */
    List<GoalRespDTO> getUserGoals(Long userId);
    
    /**
     * 根据ID获取目标
     */
    GoalRespDTO getGoalById(Long id, Long userId);
    
    /**
     * 创建新目标
     */
    GoalRespDTO createGoal(CreateGoalDTO goalDTO, Long userId);
    
    /**
     * 删除目标
     */
    void deleteGoal(Long id, Long userId);
}