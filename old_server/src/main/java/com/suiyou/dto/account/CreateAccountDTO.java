package com.suiyou.dto.account;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class CreateAccountDTO {
    @NotBlank(message = "机构代码不能为空")
    private String instCode;

    @NotBlank(message = "账号/卡号不能为空")
    private String accountNo;

    @NotBlank(message = "账户类型不能为空")
    private String accountType;

    private String accountName;

    private Boolean includeInNetWorth = true;

    private List<AccountModuleDTO> modules;
}
