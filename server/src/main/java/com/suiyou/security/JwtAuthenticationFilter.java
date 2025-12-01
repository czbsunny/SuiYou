package com.suiyou.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        // 获取请求路径
        String requestPath = request.getRequestURI();
        
        // 如果是无需认证的路径，直接跳过JWT验证
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 从请求头中获取token
            String token = getJwtFromRequest(request);

            // 如果token为空
            if (token == null) {
                logger.warn("JWT token is missing for path: {}", requestPath);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token is missing");
                return;
            }

            // 如果token存在且有效
            if (jwtTokenProvider.validateToken(token)) {
                // 获取用户ID
                Long userId = jwtTokenProvider.getUserIdFromJWT(token);
                String username = jwtTokenProvider.getUsernameFromJWT(token);

                // 将用户ID设置到请求属性中，以便在控制器中通过@RequestAttribute获取
                request.setAttribute("userId", userId);

                // 创建认证对象 - 将用户ID作为name传递，确保控制器能正确获取用户ID
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId.toString(), null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置认证信息到Spring Security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 继续过滤器链
                filterChain.doFilter(request, response);
            } else {
                // token无效
                logger.warn("JWT token is invalid for path: {}", requestPath);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token is invalid");
            }
        } catch (Exception ex) {
            // 如果token验证失败，清空上下文并记录日志
            logger.error("JWT token validation failed: {}", ex.getMessage(), ex);
            SecurityContextHolder.clearContext();
            // 返回认证错误响应
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token authentication failed: " + ex.getMessage());
        }
    }
    
    // 发送JSON格式的错误响应
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format("{\"error\":\"%s\",\"status\":%d}", message, statusCode);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
    
    // 检查路径是否为无需认证的公开路径
    private boolean isPublicPath(String requestPath) {
        return requestPath.equals("/api/auth/register") || 
               requestPath.equals("/api/auth/login") || 
               requestPath.equals("/api/auth/wechat-login") ||
               requestPath.equals("/api/health");
    }

    // 从请求头中提取token
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}