package com.suiyou.dto.goal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalCategoryInitDTO {
    private String categoryCode;
    private String name;
    private String slogan;
    private String iconUrl;
    private String bgColor;
    private Integer sortOrder;
    private Boolean isEnabled;
}
