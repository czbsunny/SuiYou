package com.suiyou.dto.goal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalCategoryRespDTO {
    private String code;
    private String name;
    private String slogan;
    private String iconUrl;
    private String bgColor;
    private Integer sortOrder;
    private Boolean isHot;
}