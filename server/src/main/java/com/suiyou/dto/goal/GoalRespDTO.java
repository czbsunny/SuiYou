package com.suiyou.dto.goal;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GoalRespDTO {
    private Long id;
    private Long userId;
    private Long familyId;
    private Long creatorId;
    private String visibleScope;
    private String name;
    private BigDecimal targetAmount;
    private LocalDate deadline;
    private BigDecimal currentAmount;
    private BigDecimal spentAmount;
    private BigDecimal progressPercent;
    private LocalDate startDate;
    private String status;
    private LocalDate completeDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPrimary;
    private String iconUrl;
    private String bgUrl;

    public static GoalRespDTO fromEntity(com.suiyou.model.Goal goal) {
        GoalRespDTO dto = new GoalRespDTO();
        dto.setId(goal.getId());
        dto.setUserId(goal.getUserId());
        dto.setFamilyId(goal.getFamilyId());
        dto.setCreatorId(goal.getCreatorId());
        dto.setVisibleScope(goal.getVisibleScope());
        dto.setName(goal.getName());
        dto.setTargetAmount(goal.getTargetAmount());
        dto.setDeadline(goal.getDeadline());
        dto.setCurrentAmount(goal.getCurrentAmount());
        dto.setSpentAmount(goal.getSpentAmount());
        dto.setProgressPercent(goal.getProgressPercent());
        dto.setStartDate(goal.getStartDate());
        dto.setStatus(goal.getStatus());
        dto.setCompleteDate(goal.getCompleteDate());
        dto.setCreatedAt(goal.getCreatedAt());
        dto.setUpdatedAt(goal.getUpdatedAt());
        dto.setIsPrimary(goal.getIsPrimary());
        dto.setIconUrl(goal.getIconUrl());
        dto.setBgUrl(goal.getBgUrl());
        return dto;
    }
}
