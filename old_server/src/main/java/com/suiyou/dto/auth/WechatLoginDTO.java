package com.suiyou.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatLoginDTO {
    @NotBlank(message = "微信登录凭证不能为空")
    private String code;
    
    private String nickname;
    private String avatar;
    private Integer gender;
}