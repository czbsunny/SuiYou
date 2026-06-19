package com.suiyou.controller;

import com.suiyou.dto.account.AccountListItemRespDTO;
import com.suiyou.dto.account.AccountRespDTO;
import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.security.SecurityUtils;
import com.suiyou.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountListItemRespDTO>> getAccounts() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<AccountListItemRespDTO> accounts = accountService.getAccountsByOwnerId(userId);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping
    public ResponseEntity<AccountRespDTO> createAccount(@RequestBody CreateAccountDTO dto) {
        AccountRespDTO account = accountService.createAccount(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }
}