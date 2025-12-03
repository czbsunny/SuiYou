package com.suiyou.service.impl;

import com.suiyou.model.Account;
import com.suiyou.repository.AccountRepository;
import com.suiyou.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }
}
