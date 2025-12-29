package com.suiyou.controller;

import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.model.Account;
import com.suiyou.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountDTO createAccountDTO) {
        try {
            // 从Security上下文中获取用户ID
            Long userId = getCurrentUserId();
            
            // 调用服务创建账户
            Account createdAccount = accountService.createAccount(createAccountDTO, userId);
            
            // 返回创建成功的响应
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", createdAccount.getId(),
                "message", "资产添加成功"
            ));
        } catch (Exception e) {
            // 返回错误响应
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "资产添加失败",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAccounts() {
        try {
            // 从Security上下文中获取用户ID
            Long userId = getCurrentUserId();
            
            // 调用服务获取当前用户的所有活跃账户
            List<Account> accounts = accountService.getAccountsByUserId(userId);
            
            // 返回成功响应
            return ResponseEntity.ok(Map.of(
                "accounts", accounts,
                "count", accounts.size()
            ));
        } catch (Exception e) {
            // 返回错误响应
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "获取资产账户失败",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取当前登录用户的ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new RuntimeException("无法从认证信息中获取用户ID");
        }
    }

}