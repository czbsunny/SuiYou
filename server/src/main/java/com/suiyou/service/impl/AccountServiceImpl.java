package com.suiyou.service.impl;

import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.dto.account.UpdateAccountDTO;
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
    
    /**
     * 检查机构和机构标识符是否已被未删除的账户使用
     * @param institution 机构
     * @param institutionIdentifier 机构标识符
     * @throws IllegalArgumentException 如果已存在相同机构和标识符的未删除账户
     */
    private void checkAccountUniqueness(String institution, String institutionIdentifier) {
        Account existingAccount = accountRepository.findByInstitutionAndInstitutionIdentifierAndDeletedFalse(institution, institutionIdentifier);
        if (existingAccount != null) {
            throw new IllegalArgumentException("该机构标识已存在账户");
        }
    }

    @Override
    @Transactional
    public Account createAccount(CreateAccountDTO createAccountDTO, Long userId) {
        // 检查账户唯一性
        checkAccountUniqueness(createAccountDTO.getInstitution(), createAccountDTO.getInstitutionIdentifier());
        
        // 创建Account实体
        Account account = new Account();
        account.setInstitution(createAccountDTO.getInstitution());
        account.setInstitutionIdentifier(createAccountDTO.getInstitutionIdentifier());
        account.setStatus(1); // 设置为活跃状态
        account.setVisibleScope("PRIVATE");
        account.setAccountName(createAccountDTO.getAccountName());
        account.setIncludeInNetWorth(createAccountDTO.getIncludeInNetWorth());
        account.setThemeColor(createAccountDTO.getThemeColor());
        
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
        // 检查账户唯一性
        checkAccountUniqueness(institution, institutionIdentifier);
        
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
        // 查询所有未删除的账户（包括活跃和归档状态的）
        return accountRepository.findByOwnerIdAndDeletedFalse(userId);
    }

    @Override
    public Account getAccountByInstitutionAndIdentifier(String institution, String institutionIdentifier) {
        return accountRepository.findByInstitutionAndInstitutionIdentifierAndDeletedFalse(institution, institutionIdentifier);
    }

    @Override
    public Account getAccountById(Long id) {
        Account account = accountRepository.findById(id).orElse(null);
        // 如果账户存在且已删除，则返回null
        if (account != null && account.getDeleted()) {
            return null;
        }
        return account;
    }

    @Override
    @Transactional
    public Account updateAccount(UpdateAccountDTO updateAccountDTO, Long userId) {
        // 根据ID获取账户
        Account account = accountRepository.findById(updateAccountDTO.getAccountId()).orElse(null);
        if (account == null) {
            throw new IllegalArgumentException("账户不存在");
        }
        
        // 验证账户是否属于当前用户
        if (!account.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该账户");
        }
        
        // 检查账户是否已被删除
        if (account.getDeleted()) {
            throw new IllegalArgumentException("账户已被删除，无法修改");
        }
        
        // 更新账户信息
        if (updateAccountDTO.getAccountName() != null) {
            account.setAccountName(updateAccountDTO.getAccountName());
        }
        if (updateAccountDTO.getInstitutionIdentifier() != null) {
            // 检查新的机构识别码是否已被其他账户使用
            Account existingAccount = accountRepository.findByInstitutionAndInstitutionIdentifier(account.getInstitution(), updateAccountDTO.getInstitutionIdentifier());
            if (existingAccount != null && !existingAccount.getId().equals(updateAccountDTO.getAccountId())) {
                throw new IllegalArgumentException("该机构识别码已被其他账户使用");
            }
            account.setInstitutionIdentifier(updateAccountDTO.getInstitutionIdentifier());
        }
        if (updateAccountDTO.getIncludeInNetWorth() != null) {
            account.setIncludeInNetWorth(updateAccountDTO.getIncludeInNetWorth());
        }
        if (updateAccountDTO.getThemeColor() != null) {
            account.setThemeColor(updateAccountDTO.getThemeColor());
        }
        
        // 保存并返回更新后的账户
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public boolean updateAccountStatus(Long id, Integer status, Long userId) {  
        // 验证状态值
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("无效的状态值，只能是 0 (归档) 或 1 (活跃)");
        }
        
        // 根据ID获取账户
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            throw new IllegalArgumentException("账户不存在");
        }
        
        // 验证账户是否属于当前用户
        if (!account.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该账户");
        }
        
        // 检查账户是否已被删除
        if (account.getDeleted()) {
            throw new IllegalArgumentException("账户已被删除，无法更新状态");
        }
        
        // 检查账户是否已处于目标状态
        if (account.getStatus().equals(status)) {
            String statusText = status == 1 ? "活跃" : "归档";
            throw new IllegalArgumentException("账户已处于" + statusText + "状态，无需更新");
        }
        
        // 更新账户状态
        account.setStatus(status);
        accountRepository.save(account);
        
        return true;
    }

    @Override
    @Transactional
    public boolean deleteAccount(Long id, Long userId) {
        // 根据ID获取账户（包括未删除的）
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            throw new IllegalArgumentException("账户不存在");
        }
        
        // 验证账户是否属于当前用户
        if (!account.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该账户");
        }
        
        // 检查账户是否已被删除
        if (account.getDeleted()) {
            throw new IllegalArgumentException("账户已被删除");
        }
        
        // 软删除：标记为已删除
        account.setDeleted(true);
        accountRepository.save(account);
        
        return true;
    }
}