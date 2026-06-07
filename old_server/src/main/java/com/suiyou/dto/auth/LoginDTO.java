package com.suiyou.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank(message = "手机号不能为空")
    private String phoneNumber;
    
    @NotBlank(message = "密码不能为空")
    private String password;
}