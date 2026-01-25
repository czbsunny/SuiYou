package com.suiyou.service;

import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.dto.account.UpdateAccountDTO;

import com.suiyou.model.Account;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface AccountService {
    /**
     * 根据DTO创建账户
     * @param createAccountDTO 账户创建DTO
     * @param userId 用户ID
     * @return 创建的账户
     */
    Account createAccount(CreateAccountDTO createAccountDTO, Long userId);
    
    /**
     * 根据机构和机构标识创建账户
     * @param institution 机构
     * @param institutionIdentifier 机构标识
     * @param userId 用户ID
     * @return 创建的账户
     */
    Account createAccount(String institution, String institutionIdentifier, Long userId);
    
    /**
     * 获取当前用户的所有资产账户
     * @param userId 用户ID
     * @return 资产账户列表
     */
    List<Account> getAccountsByUserId(Long userId);

    /**
     * 根据机构和机构标识获取账户
     * @param institution 机构
     * @param institutionIdentifier 机构标识
     * @return 资产账户
     */
    Account getAccountByInstitutionAndIdentifier(String institution, String institutionIdentifier);

    /**
     * 根据ID获取账户
     * @param id 账户ID
     * @return 资产账户
     */
    Account getAccountById(Long id);

    /**
     * 修改账户信息
     * @param updateAccountDTO 更新账户信息DTO
     * @param userId 用户ID
     * @return 修改后的账户
     */
    Account updateAccount(UpdateAccountDTO updateAccountDTO, Long userId);

    /**
     * 更新账户状态
     * @param id 账户ID
     * @param status 状态值 (1: 活跃, 0: 归档)
     * @param userId 用户ID
     * @return 操作结果
     */
    boolean updateAccountStatus(Long id, Integer status, Long userId);

    /**
     * 删除账户
     * @param id 账户ID
     * @param userId 用户ID
     * @return 操作结果
     */
    boolean deleteAccount(Long id, Long userId);
}