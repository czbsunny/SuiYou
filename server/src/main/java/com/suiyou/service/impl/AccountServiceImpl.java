package com.suiyou.service.impl;

import com.suiyou.dto.account.AccountListItemRespDTO;
import com.suiyou.dto.account.AccountModuleDTO;
import com.suiyou.dto.account.AccountModuleRespDTO;
import com.suiyou.dto.account.AccountRespDTO;
import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.enums.AccountType;
import com.suiyou.enums.InstType;
import com.suiyou.enums.ModuleType;
import com.suiyou.model.Account;
import com.suiyou.model.AccountModule;
import com.suiyou.model.SysAccountTemplate;
import com.suiyou.model.SysInstitution;
import com.suiyou.repository.AccountModuleRepository;
import com.suiyou.repository.AccountRepository;
import com.suiyou.repository.SysAccountTemplateRepository;
import com.suiyou.repository.SysInstitutionRepository;
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

    @Autowired
    private SysAccountTemplateRepository sysAccountTemplateRepository;

    @Autowired
    private SysInstitutionRepository sysInstitutionRepository;

    @Override
    public List<AccountListItemRespDTO> getAccountsByOwnerId(Long ownerId) {
        List<Account> accounts = accountRepository.findByOwnerId(ownerId);
        return accounts.stream()
                .map(this::toAccountListItemRespDTO)
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

                SysAccountTemplate template = sysAccountTemplateRepository.findByInstCodeAndAccountTypeAndModuleType(dto.getInstCode(), dto.getAccountType(), moduleDTO.getModuleType());
                if (template != null) {
                    module.setIconUrl(template.getIconUrl());
                    module.setCanPay(template.getCanPay() ? 1 : 0);
                } else {
                    ModuleType moduleType = ModuleType.ofCode(moduleDTO.getModuleType());
                    if (moduleType == null) {
                        throw new IllegalArgumentException("Module type not found: " + moduleDTO.getModuleType() + " for module: " + moduleDTO.getModuleName());
                    }
                    module.setIconUrl(moduleType.getIconUrl());
                    module.setCanPay(moduleType.isCanPay() ? 1 : 0);
                }
                module.setSortOrder(sortOrder++);
                modules.add(module);
            }
            accountModuleRepository.saveAll(modules);
        }

        return toAccountRespDTO(savedAccount);
    }

    private AccountListItemRespDTO toAccountListItemRespDTO(Account account) {
        SysInstitution institution = sysInstitutionRepository.findByInstCode(account.getInstCode());
        String instType = null;
        String instTypeName = null;
        String instName = null;
        String logoUrl = null;
        if (institution != null) {
            instType = institution.getInstType();
            instName = institution.getInstName();
            logoUrl = institution.getLogoUrl();
            InstType instTypeEnum = InstType.ofCode(instType);
            instTypeName = instTypeEnum != null ? instTypeEnum.getName() : null;
        }

        String accountTypeName = null;
        AccountType accountTypeEnum = AccountType.ofCode(account.getAccountType());
        if (accountTypeEnum != null) {
            accountTypeName = accountTypeEnum.getName();
        }

        return AccountListItemRespDTO.builder()
                .id(account.getId())
                .accountName(account.getAccountName())
                .accountNo(account.getAccountNo())
                .accountTypeName(accountTypeName)
                .amount(account.getAmount())
                .instCode(account.getInstCode())
                .instType(instType)
                .instTypeName(instTypeName)
                .instName(instName)
                .logoUrl(logoUrl)
                .build();
    }

    private AccountRespDTO toAccountRespDTO(Account account) {
        List<AccountModule> modules = accountModuleRepository.findByAccountIdAndEnabled(String.valueOf(account.getId()), 1);
        List<AccountModuleRespDTO> moduleDTOs = modules.stream()
                .map(this::toAccountModuleRespDTO)
                .collect(Collectors.toList());

        SysInstitution institution = sysInstitutionRepository.findByInstCode(account.getInstCode());
        String instType = null;
        String instTypeName = null;
        if (institution != null) {
            instType = institution.getInstType();
            InstType instTypeEnum = InstType.ofCode(instType);
            instTypeName = instTypeEnum != null ? instTypeEnum.getName() : null;
        }

        String accountTypeName = null;
        AccountType accountTypeEnum = AccountType.ofCode(account.getAccountType());
        if (accountTypeEnum != null) {
            accountTypeName = accountTypeEnum.getName();
        }

        return AccountRespDTO.builder()
                .id(account.getId())
                .instCode(account.getInstCode())
                .instType(instType)
                .instTypeName(instTypeName)
                .accountNo(account.getAccountNo())
                .accountType(account.getAccountType())
                .accountTypeName(accountTypeName)
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