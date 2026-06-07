package com.suiyou.dto.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginDTOSimpleTest {

    @Test
    void testLoginDTOSettersAndGetters() {
        // 创建LoginDTO实例
        LoginDTO loginDTO = new LoginDTO();
        
        // 测试手机号设置和获取
        String phoneNumber = "13800138000";
        loginDTO.setPhoneNumber(phoneNumber);
        assertEquals(phoneNumber, loginDTO.getPhoneNumber());
        
        // 测试密码设置和获取
        String password = "test123456";
        loginDTO.setPassword(password);
        assertEquals(password, loginDTO.getPassword());
    }
}