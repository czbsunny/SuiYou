package com.suiyou.service;

import com.suiyou.dto.account.AccountListItemRespDTO;
import com.suiyou.dto.account.AccountRespDTO;
import com.suiyou.dto.account.CreateAccountDTO;

import java.util.List;

public interface AccountService {
    List<AccountListItemRespDTO> getAccountsByOwnerId(Long ownerId);
    AccountRespDTO createAccount(CreateAccountDTO dto);
    AccountRespDTO getAccountById(Long id);
}