package com.suiyou.service;

import com.suiyou.dto.account.AccountRespDTO;
import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.dto.account.UpdateAccountDTO;

import com.suiyou.model.Account;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface AccountService {
    Account createAccount(CreateAccountDTO accountDTO, Long userId);

    Account getAccountByInstCodeAndAccountNo(Long userId, String instCode, String accountNo);

    List<AccountRespDTO> getAccountsByUserId(Long userId);

    List<AccountRespDTO> getAccountsByUserIdAndInstCode(Long userId, String instCode);

    AccountRespDTO getAccountById(Long id);

    AccountRespDTO updateAccount(UpdateAccountDTO updateAccountDTO, Long userId);

    boolean updateAccountStatus(Long id, Integer status, Long userId);

    boolean deleteAccount(Long id, Long userId);

    void batchUpdateAccounts(Long userId, List<Long> activeAccountIds, List<Long> archivedAccountIds);
}
