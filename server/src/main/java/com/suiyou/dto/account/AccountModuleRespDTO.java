package com.suiyou.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountModuleRespDTO {
    private String id;
    private String moduleType;
    private String moduleName;
    private String iconUrl;
    private String bgColor;
    private Integer canPay;
    private Integer sortOrder;
}