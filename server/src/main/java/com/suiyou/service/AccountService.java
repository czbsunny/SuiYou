package com.suiyou.service;

import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.model.Account;
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
}