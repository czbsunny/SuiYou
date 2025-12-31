package com.suiyou.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionRespDTO {

    private Long id;

    private String instCode;

    private String instName;

    private String shortName;

    private String instType;

    private String logoUrl;

    private String themeColor;

    private String indexLetter;
    
    private Boolean isHot;
}