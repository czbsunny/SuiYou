package com.suiyou.service;

import com.suiyou.model.Account;
import org.springframework.stereotype.Service;

@Service
public interface AccountService {
    Account createAccount(Account account);
}
