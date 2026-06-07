package com.suiyou.dto.account;

import lombok.Data;

@Data
public class UpdateAccountDTO {
    private Long accountId;

    private String accountName;

    private String accountNo;

    private Boolean includeInNetWorth;
}
