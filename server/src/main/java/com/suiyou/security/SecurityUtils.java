package com.suiyou.security;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SecurityUtils {

    public static Long getCurrentUserId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("用户未登录");
        }

        Object value = attributes.getRequest().getAttribute("userId");
        if (value instanceof Long) {
            return (Long) value;
        }

        throw new RuntimeException("无法从认证信息中获取用户ID");
    }
}
