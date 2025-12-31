package com.suiyou.dto.account;

import lombok.Data;

@Data
public class InstitutionInitDTO {
    private String instCode;
    private String instName;
    private String shortName;
    private String instType;
    private String logoUrl;
    private String themeColor;
    private Integer sortOrder;
    private Boolean isHot;
}