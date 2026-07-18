package com.suiyou.service.impl;

import com.suiyou.dto.goal.CreateGoalDTO;
import com.suiyou.dto.goal.GoalCategoryRespDTO;
import com.suiyou.dto.goal.GoalRespDTO;
import com.suiyou.dto.goal.GoalTemplateRespDTO;
import com.suiyou.model.Goal;
import com.suiyou.model.SysGoalCategory;
import com.suiyou.model.SysGoalTemplate;
import com.suiyou.repository.GoalRepository;
import com.suiyou.repository.SysGoalCategoryRepository;
import com.suiyou.repository.SysGoalTemplateRepository;
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
    private SysGoalCategoryRepository sysGoalCategoryRepository;

    @Autowired
    private SysGoalTemplateRepository sysGoalTemplateRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GoalRespDTO> getUserGoals(Long userId) {
        List<Goal> goals = goalRepository.findByCreatorId(userId);
        return goals.stream()
                .map(GoalRespDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GoalRespDTO getGoalById(Long id, Long userId) {
        Goal goal = goalRepository.findById(id).orElse(null);
        if (goal == null) {
            throw new RuntimeException("目标不存在");
        }

        if (!goal.getCreatorId().equals(userId)) {
            throw new RuntimeException("无权访问该目标");
        }

        return GoalRespDTO.fromEntity(goal);
    }

    @Override
    @Transactional
    public GoalRespDTO createGoal(CreateGoalDTO goalDTO, Long userId) {
        if (goalDTO.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("目标金额必须大于0");
        }

        if (goalDTO.getDeadline().isBefore(goalDTO.getStartDate())) {
            throw new RuntimeException("截止日期必须晚于开始日期");
        }

        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setFamilyId(1L);
        goal.setCreatorId(userId);
        goal.setName(goalDTO.getName());
        goal.setTargetAmount(goalDTO.getTargetAmount());
        goal.setDeadline(goalDTO.getDeadline());
        goal.setStartDate(goalDTO.getStartDate());
        goal.setVisibleScope(goalDTO.getVisibleScope());

        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setSpentAmount(BigDecimal.ZERO);
        goal.setProgressPercent(BigDecimal.ZERO);
        goal.setStatus("ON_GOING");

        goal.setIconUrl(goalDTO.getIconUrl());
        goal.setBgUrl(goalDTO.getBgUrl());
        goal.setIsPrimary(goalDTO.getIsPrimary());

        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());

        goal = goalRepository.save(goal);
        return GoalRespDTO.fromEntity(goal);
    }

    @Override
    @Transactional
    public void deleteGoal(Long id, Long userId) {
        Goal goal = goalRepository.findById(id).orElse(null);
        if (goal == null) {
            throw new RuntimeException("目标不存在");
        }

        if (!goal.getCreatorId().equals(userId)) {
            throw new RuntimeException("无权删除该目标");
        }

        goalRepository.delete(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalCategoryRespDTO> getGoalCategories() {
        List<SysGoalCategory> categories = sysGoalCategoryRepository.findByIsEnabledTrueOrderBySortOrderAsc();
        return categories.stream()
                .map(cat -> GoalCategoryRespDTO.builder()
                        .code(cat.getCategoryCode())
                        .name(cat.getName())
                        .slogan(cat.getSlogan())
                        .iconUrl(cat.getIconUrl())
                        .bgColor(cat.getBgColor())
                        .sortOrder(cat.getSortOrder())
                        .isHot(false)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalTemplateRespDTO> getGoalTemplates(String categoryCode) {
        List<SysGoalTemplate> templates;
        if (categoryCode == null || categoryCode.isEmpty()) {
            templates = sysGoalTemplateRepository.findAllByOrderBySortOrderAsc();
        } else {
            templates = sysGoalTemplateRepository.findByCategoryCodeOrderBySortOrderAsc(categoryCode);
        }

        return templates.stream()
                .map(tpl -> GoalTemplateRespDTO.builder()
                        .code(tpl.getTemplateCode())
                        .categoryCode(tpl.getCategoryCode())
                        .name(tpl.getName())
                        .iconUrl(tpl.getIconUrl())
                        .defaultAmount(tpl.getDefaultAmount())
                        .defaultPeriodDays(tpl.getDefaultPeriodDays())
                        .inputMode(tpl.getInputMode())
                        .milestoneStrategy(tpl.getMilestoneStrategy())
                        .isHot(tpl.getIsHot() != null && tpl.getIsHot() == 1)
                        .sortOrder(tpl.getSortOrder())
                        .build())
                .collect(Collectors.toList());
    }
}