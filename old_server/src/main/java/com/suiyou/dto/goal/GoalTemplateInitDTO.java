package com.suiyou.dto.goal;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalTemplateInitDTO {
        private String categoryCode;
        private String templateCode;
        private String name;
        private String iconUrl;
        private BigDecimal defaultAmount;
        private Integer defaultPeriodDays;
        private String inputMode;
        private String milestoneStrategy;
        private Integer sortOrder;
        private Integer isHot;
}
