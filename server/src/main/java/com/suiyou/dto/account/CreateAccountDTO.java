package com.suiyou.dto.account;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 创建账户请求DTO
 */
@Data
public class CreateAccountDTO {
    @NotBlank(message = "机构代码不能为空")
    private String institution; // 机构代码
    
    @NotBlank(message = "机构唯一标识不能为空")
    private String institutionIdentifier; // 机构唯一标识
}