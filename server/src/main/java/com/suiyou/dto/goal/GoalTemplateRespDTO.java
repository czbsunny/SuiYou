package com.suiyou.dto.goal;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoalTemplateRespDTO {
    private String code;
    private String categoryCode;
    private String name;
    private String iconUrl;
    private BigDecimal defaultAmount;
    private Integer defaultPeriodDays;
    private String inputMode;
    private Object wizardConfig;
    private String milestoneStrategy;
    private Object milestoneConfig;
    private Boolean isHot;
    private Integer sortOrder;
}