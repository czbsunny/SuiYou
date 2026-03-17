package com.suiyou.service;

import com.suiyou.dto.account.AccountRespDTO;
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
    Account createAccount(CreateAccountDTO accountDTO, Long userId);
    
    /**
     * 根据机构和机构标识获取账户
     * @param institution 机构
     * @param institutionIdentifier 机构标识
     * @return 资产账户
     */
    Account getAccountByInstitutionAndIdentifier(Long userId, String institution, String institutionIdentifier);

    /**
     * 获取当前用户的所有资产账户
     * @param userId 用户ID
     * @return 资产账户列表
     */
    List<AccountRespDTO> getAccountsByUserId(Long userId);
    
    /**
     * 获取当前用户指定机构的所有资产账户
     * @param userId 用户ID
     * @param institution 机构代码
     * @return 资产账户列表
     */
    List<AccountRespDTO> getAccountsByUserIdAndInstitution(Long userId, String institution);

    /**
     * 根据ID获取账户
     * @param id 账户ID
     * @return 资产账户
     */
    AccountRespDTO getAccountById(Long id);

    /**
     * 修改账户信息
     * @param updateAccountDTO 更新账户信息DTO
     * @param userId 用户ID
     * @return 修改后的账户
     */
    AccountRespDTO updateAccount(UpdateAccountDTO updateAccountDTO, Long userId);

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

    /**
     * 批量更新账户（排序+归档+恢复）
     * @param userId 用户ID
     * @param activeAccountIds 活跃账户ID列表（按排序顺序）
     * @param archivedAccountIds 需要归档的账户ID列表
     */
    void batchUpdateAccounts(Long userId, List<Long> activeAccountIds, List<Long> archivedAccountIds);
}