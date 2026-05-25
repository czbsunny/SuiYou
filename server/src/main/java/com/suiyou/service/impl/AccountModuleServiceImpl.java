package com.suiyou.service.impl;

import com.suiyou.dto.account.AccountModuleRespDTO;
import com.suiyou.model.Account;
import com.suiyou.model.AccountModule;
import com.suiyou.model.enums.AssetType;
import com.suiyou.repository.AccountModuleRepository;
import com.suiyou.repository.AccountRepository;
import com.suiyou.service.AccountModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountModuleServiceImpl implements AccountModuleService {

    @Autowired
    private AccountModuleRepository accountModuleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountModule addModule(Long accountId, String assetType, String moduleName, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("账户不存在"));
        
        if (!account.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该账户");
        }
        
        if (account.getDeleted()) {
            throw new IllegalArgumentException("账户已被删除");
        }
        
        AssetType type;
        try {
            type = AssetType.valueOf(assetType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的资产类型");
        }
        
        if (accountModuleRepository.existsByAccountIdAndAssetType(accountId, type)) {
            throw new IllegalArgumentException("该账户已存在此资产类型模块");
        }
        
        AccountModule module = new AccountModule();
        module.setAccount(account);
        module.setAssetType(type);
        module.setModuleName(moduleName);
        module.setStatus(1);
        
        return accountModuleRepository.save(module);
    }

    @Override
    @Transactional
    public boolean removeModule(Long accountId, Long moduleId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("账户不存在"));
        
        if (!account.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该账户");
        }
        
        if (account.getDeleted()) {
            throw new IllegalArgumentException("账户已被删除");
        }
        
        AccountModule module = accountModuleRepository.findByIdAndAccountId(moduleId, accountId)
                .orElseThrow(() -> new IllegalArgumentException("资产模块不存在"));
        
        accountModuleRepository.delete(module);
        return true;
    }

    @Override
    public List<AccountModuleRespDTO> getModulesByAccountId(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("账户不存在"));
        
        if (!account.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该账户");
        }
        
        if (account.getDeleted()) {
            throw new IllegalArgumentException("账户已被删除");
        }
        
        List<AccountModule> modules = accountModuleRepository.findByAccountIdAndStatus(accountId, 1);
        return modules.stream()
                .map(AccountModuleRespDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public AccountModule getModuleById(Long id) {
        return accountModuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("资产模块不存在"));
    }
}