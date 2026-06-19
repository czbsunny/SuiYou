package com.suiyou.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountListItemRespDTO {
    private Long id;
    private String accountName;
    private String accountNo;
    private String accountTypeName;
    private Double amount;
    private String instCode;
    private String instType;
    private String instTypeName;
    private String instName;
    private String logoUrl;
}
