package com.suiyou.service.impl;

import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.model.Account;
import com.suiyou.model.Family;
import com.suiyou.repository.AccountRepository;
import com.suiyou.service.FamilyService;
import com.suiyou.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private FamilyService familyService;
    
    @Override
    @Transactional
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }
    
    @Override
    @Transactional
    public Account createAccount(CreateAccountDTO createAccountDTO, Long userId) {
        // 创建Account实体
        Account account = new Account();
        account.setInstitution(createAccountDTO.getInstitution());
        account.setInstitutionIdentifier(createAccountDTO.getInstitutionIdentifier());
        account.setStatus(1); // 设置为活跃状态
        account.setVisibleScope("PRIVATE");
        account.setAccountName(createAccountDTO.getAccountName());
        
        // 设置用户ID和家庭ID
        account.setOwnerId(userId);

        Family family = familyService.getFirstActiveFamilyByUserId(userId);
        if (Objects.isNull(family)) {
            throw new IllegalArgumentException("用户未关联任何家庭");
        }
        account.setFamilyId(family.getId());
        
        // 保存账户
        return accountRepository.save(account);
    }
    
    @Override
    @Transactional
    public Account createAccount(String institution, String institutionIdentifier, Long userId) {
        // 检查是否已存在该机构标识的账户
        Account existingAccount = accountRepository.findByInstitutionAndInstitutionIdentifier(institution, institutionIdentifier);
        if (existingAccount != null) {
            throw new IllegalArgumentException("该机构标识已存在账户");
        }
        
        // 创建新账户
        Account account = new Account();
        account.setInstitution(institution);
        account.setInstitutionIdentifier(institutionIdentifier);
        account.setAccountName(institutionIdentifier);
        account.setOwnerId(userId);

        // 这里需要从用户服务获取当前家庭ID
        Family family = familyService.getFirstActiveFamilyByUserId(userId);
        if (Objects.isNull(family)) {
            throw new IllegalArgumentException("用户未关联任何家庭");
        }
        account.setFamilyId(family.getId());
        account.setStatus(1); // 设置为活跃状态
        
        // 保存账户
        return accountRepository.save(account);
    }
    
    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        // 查询状态为1（活跃）的账户，不包括已归档的账户
        return accountRepository.findByOwnerIdAndStatus(userId, 1);
    }

    @Override
    public Account getAccountByInstitutionAndIdentifier(String institution, String institutionIdentifier) {
        return accountRepository.findByInstitutionAndInstitutionIdentifier(institution, institutionIdentifier);
    }

    @Override
    public Account getAccountById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }
}