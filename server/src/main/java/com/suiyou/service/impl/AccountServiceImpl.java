package com.suiyou.service.impl;

import com.suiyou.dto.account.AccountModuleDTO;
import com.suiyou.dto.account.AccountModuleRespDTO;
import com.suiyou.dto.account.AccountRespDTO;
import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.enums.ModuleType;
import com.suiyou.model.Account;
import com.suiyou.model.AccountModule;
import com.suiyou.repository.AccountModuleRepository;
import com.suiyou.repository.AccountRepository;
import com.suiyou.security.SecurityUtils;
import com.suiyou.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountModuleRepository accountModuleRepository;

    @Override
    public List<AccountRespDTO> getAccountsByOwnerId(Long ownerId) {
        List<Account> accounts = accountRepository.findByOwnerId(ownerId);
        return accounts.stream()
                .map(this::toAccountRespDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountRespDTO createAccount(CreateAccountDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Account account = new Account();
        account.setOwnerId(currentUserId);
        account.setFamilyId(currentUserId);
        account.setInstCode(dto.getInstCode());
        account.setAccountNo(dto.getAccountNo());
        account.setAccountType(dto.getAccountType() != null ? dto.getAccountType() : "DEBIT_CARD");
        account.setAccountName(dto.getAccountName());
        account.setIncludeInNetWorth(dto.getIncludeInNetWorth() != null ? dto.getIncludeInNetWorth() : true);

        Account savedAccount = accountRepository.save(account);

        if (dto.getModules() != null && !dto.getModules().isEmpty()) {
            List<AccountModule> modules = new ArrayList<>();
            int sortOrder = 0;
            for (AccountModuleDTO moduleDTO : dto.getModules()) {
                AccountModule module = new AccountModule();
                module.setId(UUID.randomUUID().toString().replace("-", ""));
                module.setAccountId(String.valueOf(savedAccount.getId()));
                module.setModuleType(moduleDTO.getModuleType());
                module.setModuleName(moduleDTO.getModuleName());

                ModuleType moduleType = ModuleType.ofCode(moduleDTO.getModuleType());
                if (moduleType != null) {
                    module.setIconUrl(moduleType.getIconUrl());
                    module.setCanPay(moduleType.isCanPay() ? 1 : 0);
                } else {
                    module.setIconUrl("/static/assets/modules/default.png");
                    module.setCanPay(0);
                }
                module.setSortOrder(sortOrder++);
                modules.add(module);
            }
            accountModuleRepository.saveAll(modules);
        }

        return toAccountRespDTO(savedAccount);
    }

    private AccountRespDTO toAccountRespDTO(Account account) {
        List<AccountModule> modules = accountModuleRepository.findByAccountId(account.getId());
        List<AccountModuleRespDTO> moduleDTOs = modules.stream()
                .map(this::toAccountModuleRespDTO)
                .collect(Collectors.toList());

        return AccountRespDTO.builder()
                .id(account.getId())
                .instCode(account.getInstCode())
                .accountNo(account.getAccountNo())
                .accountType(account.getAccountType())
                .accountName(account.getAccountName())
                .includeInNetWorth(account.getIncludeInNetWorth())
                .modules(moduleDTOs)
                .createdAt(account.getCreatedAt())
                .build();
    }

    private AccountModuleRespDTO toAccountModuleRespDTO(AccountModule module) {
        return AccountModuleRespDTO.builder()
                .id(module.getId())
                .moduleType(module.getModuleType())
                .moduleName(module.getModuleName())
                .iconUrl(module.getIconUrl())
                .canPay(module.getCanPay())
                .sortOrder(module.getSortOrder())
                .build();
    }
}