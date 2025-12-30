package com.suiyou.dto.account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryInstitutionRelationRespDTO {
    private String categoryCode;
    private String instCode;
}