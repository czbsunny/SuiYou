package com.suiyou.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTemplateRespDTO {
    private String moduleType;
    private String moduleName;
    private Boolean required;
    private Boolean enabled;
    private Integer sortOrder;
    private String iconUrl;
}