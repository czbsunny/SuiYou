package com.suiyou.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UpdateUserInfoDTO {
    
    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 20, message = "昵称长度不能超过20个字符")
    private String username;
}
