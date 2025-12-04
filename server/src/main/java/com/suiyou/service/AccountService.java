package com.suiyou.service;

import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.model.Account;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface AccountService {
    Account createAccount(Account account);
    
    /**
     * 根据DTO创建账户
     * @param createAccountDTO 账户创建DTO
     * @param userId 用户ID
     * @return 创建的账户
     */
    Account createAccount(CreateAccountDTO createAccountDTO, Long userId);
    
    /**
     * 获取当前用户的所有资产账户
     * @param userId 用户ID
     * @return 资产账户列表
     */
    List<Account> getAccountsByUserId(Long userId);
}