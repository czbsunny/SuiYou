package com.suiyou.service.impl;

import com.suiyou.dto.account.AccountListItemRespDTO;
import com.suiyou.dto.account.AccountModuleDTO;
import com.suiyou.dto.account.AccountModuleRespDTO;
import com.suiyou.dto.account.AccountRespDTO;
import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.dto.account.UpdateAccountDTO;
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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

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
    public AccountRespDTO getAccountById(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Account account = accountRepository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));
        return toAccountRespDTO(account);
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
                module.setAccountId(savedAccount.getId());
                module.setModuleType(moduleDTO.getModuleType());
                module.setModuleName(moduleDTO.getModuleName());

                SysAccountTemplate template = sysAccountTemplateRepository.findByInstCodeAndAccountTypeAndModuleType(dto.getInstCode(), dto.getAccountType(), moduleDTO.getModuleType());
                if (template != null) {
                    module.setIconUrl(template.getIconUrl());
                    module.setCanPay(template.getCanPay() ? 1 : 0);
                }
                
                ModuleType moduleType = ModuleType.ofCode(moduleDTO.getModuleType());
                if (moduleType == null) {
                    throw new IllegalArgumentException("Module type not found: " + moduleDTO.getModuleType() + " for module: " + moduleDTO.getModuleName());
                }
                module.setBgColor(moduleType.getBgColor());
                module.setIconUrl(moduleType.getIconUrl());
                module.setCanPay(moduleType.isCanPay() ? 1 : 0);
                
                module.setSortOrder(sortOrder++);
                modules.add(module);
            }
            accountModuleRepository.saveAll(modules);
        }

        return toAccountRespDTO(savedAccount);
    }

    @Override
    @Transactional
    public AccountRespDTO updateAccount(UpdateAccountDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Account account = accountRepository.findByIdAndOwnerId(dto.getAccountId(), currentUserId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + dto.getAccountId()));

        account.setAccountNo(dto.getAccountNo());
        account.setAccountName(dto.getAccountName());
        account.setIncludeInNetWorth(dto.getIncludeInNetWorth() != null ? dto.getIncludeInNetWorth() : true);

        Account savedAccount = accountRepository.save(account);

        // 同步模块：已存在的启用，不存在的插入，多余的去禁用
        List<AccountModule> allModules = accountModuleRepository.findByAccountId(savedAccount.getId());
        Map<String, AccountModule> moduleMap = allModules.stream()
                .collect(Collectors.toMap(AccountModule::getModuleType, Function.identity()));

        Set<String> dtoModuleTypes = dto.getModules() != null
                ? dto.getModules().stream().map(AccountModuleDTO::getModuleType).collect(Collectors.toSet())
                : Collections.emptySet();

        List<AccountModule> modulesToSave = new ArrayList<>();

        // 处理需要禁用或删除的模块（在表中但不在 DTO 中）
        for (AccountModule existing : allModules) {
            if (!dtoModuleTypes.contains(existing.getModuleType()) && existing.getEnabled() == 1) {
                existing.setEnabled(0);
                modulesToSave.add(existing);
            }
        }

        // 处理 DTO 中的模块
        if (!CollectionUtils.isEmpty(dto.getModules())) {
            int sortOrder = 0;
            for (AccountModuleDTO moduleDTO : dto.getModules()) {
                AccountModule existing = moduleMap.get(moduleDTO.getModuleType());
                if (existing != null) {
                    // 已存在，启用并更新
                    existing.setEnabled(1);
                    existing.setModuleName(moduleDTO.getModuleName());
                    existing.setSortOrder(sortOrder++);
                    modulesToSave.add(existing);
                } else {
                    // 不存在，新建
                    AccountModule module = buildNewModule(savedAccount.getId(), moduleDTO, sortOrder++);
                    modulesToSave.add(module);
                }
            }
        }

        if (!modulesToSave.isEmpty()) {
            accountModuleRepository.saveAll(modulesToSave);
        }

        return toAccountRespDTO(savedAccount);
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Account account = accountRepository.findByIdAndOwnerId(id, currentUserId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));

        // 先删除账户关联的模块
        List<AccountModule> modules = accountModuleRepository.findByAccountId(account.getId());
        if (!modules.isEmpty()) {
            accountModuleRepository.deleteAll(modules);
        }

        accountRepository.delete(account);
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
                .instName(instName)
                .instType(instType)
                .instTypeName(instTypeName)
                .logoUrl(logoUrl)
                .build();
    }

    private AccountRespDTO toAccountRespDTO(Account account) {
        List<AccountModule> modules = accountModuleRepository.findByAccountIdAndEnabled(account.getId(), 1);
        List<AccountModuleRespDTO> moduleDTOs = modules.stream()
                .map(this::toAccountModuleRespDTO)
                .collect(Collectors.toList());

        SysInstitution institution = sysInstitutionRepository.findByInstCode(account.getInstCode());
        String instType = null;
        String instTypeName = null;
        String instName = null;
        if (institution != null) {
            instType = institution.getInstType();
            instName = institution.getInstName();
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
                .instName(instName)
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
                .bgColor(module.getBgColor())
                .canPay(module.getCanPay())
                .sortOrder(module.getSortOrder())
                .build();
    }

    private AccountModule buildNewModule(Long accountId, AccountModuleDTO moduleDTO, int sortOrder) {
        AccountModule module = new AccountModule();
        module.setId(UUID.randomUUID().toString().replace("-", ""));
        module.setAccountId(accountId);
        module.setModuleType(moduleDTO.getModuleType());
        module.setModuleName(moduleDTO.getModuleName());

        ModuleType moduleType = ModuleType.ofCode(moduleDTO.getModuleType());
        if (moduleType == null) {
            throw new IllegalArgumentException("Module type not found: " + moduleDTO.getModuleType() + " for module: " + moduleDTO.getModuleName());
        }
        module.setIconUrl(moduleType.getIconUrl());
        module.setBgColor(moduleType.getBgColor());
        module.setCanPay(moduleType.isCanPay() ? 1 : 0);
        module.setSortOrder(sortOrder);
        module.setEnabled(1);
        return module;
    }
}