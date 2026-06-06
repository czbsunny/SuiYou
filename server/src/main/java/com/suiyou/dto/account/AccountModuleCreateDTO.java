package com.suiyou.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountModuleCreateDTO {

    @NotBlank(message = "资产类型不能为空")
    private String assetType;
    
    @NotBlank(message = "模块名称不能为空")
    private String moduleName;
}
