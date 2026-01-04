package com.suiyou.service.impl;

import com.suiyou.dto.goal.CreateGoalDTO;
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

@Service
public class GoalServiceImpl implements GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Goal> getUserGoals(Long userId) {
        // 验证用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在");
        }

        // 获取用户创建的所有目标
        return goalRepository.findByCreatorId(userId);
    }

    @Override
    @Transactional
    public Goal createGoal(CreateGoalDTO goalDTO, Long userId) {
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

        return goalRepository.save(goal);
    }
}