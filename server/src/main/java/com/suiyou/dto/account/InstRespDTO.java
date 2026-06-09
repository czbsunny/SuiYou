package com.suiyou.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstRespDTO {
    private String instCode;
    private String instName;
    private String shortName;
    private String instType;
    private String instTypeName;
    private String logoUrl;
    private String indexLetter;
    private Boolean isHot;
    private List<AccountTypeDTO> accountTypes;
}