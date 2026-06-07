package com.suiyou.dto.account;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AccountModuleDTO {
    
    @NotBlank(message = "资产类型不能为空")
    private String assetType;
    
    @NotBlank(message = "模块名称不能为空")
    private String moduleName;
}