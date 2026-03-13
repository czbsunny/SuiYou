package com.suiyou.service.impl;

import com.suiyou.dto.goal.CreateGoalDTO;
import com.suiyou.dto.goal.GoalRespDTO;
import com.suiyou.model.Goal;
import com.suiyou.model.User;
import com.suiyou.repository.GoalRepository;
import com.suiyou.repository.UserRepository;
import com.suiyou.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalServiceImpl implements GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GoalRespDTO> getUserGoals(Long userId) {
        // 获取用户创建的所有目标并转换为DTO
        List<Goal> goals = goalRepository.findByCreatorId(userId);
        return goals.stream()
                .map(GoalRespDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GoalRespDTO getGoalById(Long id, Long userId) {
        // 获取目标
        Goal goal = goalRepository.findById(id).orElse(null);
        if (goal == null) {
            throw new RuntimeException("目标不存在");
        }

        // 验证目标是否属于当前用户
        if (!goal.getCreatorId().equals(userId)) {
            throw new RuntimeException("无权访问该目标");
        }

        return GoalRespDTO.fromEntity(goal);
    }

    @Override
    @Transactional
    public GoalRespDTO createGoal(CreateGoalDTO goalDTO, Long userId) {
        // 验证目标金额必须大于0
        if (goalDTO.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("目标金额必须大于0");
        }

        // 验证截止日期必须晚于开始日期
        if (goalDTO.getDeadline().isBefore(goalDTO.getStartDate())) {
            throw new RuntimeException("截止日期必须晚于开始日期");
        }

        // 创建目标对象
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setFamilyId(1L);
        goal.setCreatorId(userId);
        goal.setName(goalDTO.getName());
        goal.setTargetAmount(goalDTO.getTargetAmount());
        goal.setDeadline(goalDTO.getDeadline());
        goal.setStartDate(goalDTO.getStartDate());
        goal.setVisibleScope(goalDTO.getVisibleScope());
        
        // 设置默认值
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setSpentAmount(BigDecimal.ZERO);
        goal.setProgressPercent(BigDecimal.ZERO);
        goal.setStatus("ON_GOING");
        
        // 设置图标和背景
        goal.setIconUrl(goalDTO.getIconUrl());
        goal.setBgUrl(goalDTO.getBgUrl());
        goal.setIsPrimary(goalDTO.getIsPrimary());

        // 设置时间戳
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());

        // 保存目标到数据库
        goal = goalRepository.save(goal);
        return GoalRespDTO.fromEntity(goal);
    }

    @Override
    @Transactional
    public void deleteGoal(Long id, Long userId) {
        // 获取目标
        Goal goal = goalRepository.findById(id).orElse(null);
        if (goal == null) {
            throw new RuntimeException("目标不存在");
        }

        // 验证目标是否属于当前用户
        if (!goal.getCreatorId().equals(userId)) {
            throw new RuntimeException("无权删除该目标");
        }

        // 删除目标
        goalRepository.delete(goal);
    }
}