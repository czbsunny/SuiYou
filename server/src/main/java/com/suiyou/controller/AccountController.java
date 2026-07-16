package com.suiyou.controller;

import com.suiyou.dto.account.AccountListItemRespDTO;
import com.suiyou.dto.account.AccountRespDTO;
import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.dto.account.UpdateAccountDTO;
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

    @GetMapping("/{id}")
    public ResponseEntity<AccountRespDTO> getAccountById(@PathVariable Long id) {
        AccountRespDTO account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @PutMapping
    public ResponseEntity<AccountRespDTO> updateAccount(@RequestBody UpdateAccountDTO dto) {
        AccountRespDTO account = accountService.updateAccount(dto);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}