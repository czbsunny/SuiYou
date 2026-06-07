package com.suiyou.dto.auth;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private UserInfoDTO user;
    
    @Data
    public static class UserInfoDTO {
        private Long id;
        private String phoneNumber;
        private String username;
        private String email;
        private String avatar;
        private Integer gender;
        private String wechatNickname;
        private String wechatAvatar;
    }
}