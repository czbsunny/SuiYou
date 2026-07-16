package com.suiyou.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRespDTO {
    private Long id;
    private String accountNo;
    private String accountName;
    private String accountType;    
    private String accountTypeName;
    private String instCode;
    private String instName;
    private String instType;
    private String instTypeName;
    private Boolean includeInNetWorth;
    private List<AccountModuleRespDTO> modules;
    private LocalDateTime createdAt;
}