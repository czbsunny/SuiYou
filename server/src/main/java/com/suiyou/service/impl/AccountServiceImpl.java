package com.suiyou.service.impl;

import com.suiyou.dto.account.AccountModuleDTO;
import com.suiyou.dto.account.AccountRespDTO;
import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.dto.account.InstitutionRespDTO;
import com.suiyou.dto.account.UpdateAccountDTO;
import com.suiyou.dto.asset.AssetRespDTO;
import com.suiyou.model.Account;
import com.suiyou.model.AccountModule;
import com.suiyou.model.enums.AccountType;
import com.suiyou.model.enums.AssetType;
import com.suiyou.model.Asset;
import com.suiyou.model.Family;
import com.suiyou.model.SysInstitution;
import com.suiyou.repository.AccountModuleRepository;
import com.suiyou.repository.AccountRepository;
import com.suiyou.repository.AssetRepository;
import com.suiyou.repository.SysInstitutionRepository;
import com.suiyou.service.FamilyService;
import com.suiyou.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private FamilyService familyService;

    @Autowired
    private AccountModuleRepository accountModuleRepository;

    @Autowired
    private SysInstitutionRepository institutionRepository;

    private void checkAccountUniqueness(Long userId, String instCode, String accountType, String accountNo) {
        Account existingAccount = accountRepository
                .findByOwnerIdAndInstCodeAndAccountTypeAndAccountNoAndDeletedFalse(userId, instCode, accountType, accountNo);
        if (existingAccount != null) {
            throw new IllegalArgumentException("该账号已存在账户");
        }
    }

    private void validateAccountType(String instCode, String accountType) {
        if (!AccountType.isValid(accountType)) {
            throw new IllegalArgumentException("账户类型不存在: " + accountType);
        }
        SysInstitution institution = institutionRepository.findByInstCode(instCode);
        if (institution == null) {
            throw new IllegalArgumentException("机构代码不存在: " + instCode);
        }
        String instType = institution.getInstType();
        AccountType at = AccountType.valueOf(accountType);
        if (!at.supportsInstitution(instType)) {
            throw new IllegalArgumentException("账户类型 " + accountType + " 不支持机构类型 " + instType);
        }
    }

    @Override
    @Transactional
    public Account createAccount(CreateAccountDTO createAccountDTO, Long userId) {
        validateAccountType(createAccountDTO.getInstCode(), createAccountDTO.getAccountType());
        checkAccountUniqueness(userId, createAccountDTO.getInstCode(),
                createAccountDTO.getAccountType(), createAccountDTO.getAccountNo());

        Account account = new Account();
        account.setInstCode(createAccountDTO.getInstCode());
        account.setAccountNo(createAccountDTO.getAccountNo());
        account.setAccountType(createAccountDTO.getAccountType());
        account.setStatus(1);
        account.setVisibleScope("PRIVATE");
        account.setAccountName(createAccountDTO.getAccountName());
        account.setIncludeInNetWorth(createAccountDTO.getIncludeInNetWorth());

        Integer maxSortOrder = accountRepository.findMaxSortOrderByOwnerIdAndStatusAndDeletedFalse(userId, 1);
        int newSortOrder = maxSortOrder != null ? maxSortOrder + 1 : 0;
        account.setSortOrder(newSortOrder);

        account.setOwnerId(userId);

        Family family = familyService.getFamilyByUserId(userId);
        if (Objects.isNull(family)) {
            throw new IllegalArgumentException("用户未关联任何家庭");
        }
        account.setFamilyId(family.getId());

        Account saved = accountRepository.save(account);

        List<AccountModuleDTO> modules = createAccountDTO.getModules();
        if (modules != null && !modules.isEmpty()) {
            for (AccountModuleDTO m : modules) {
                AccountModule module = new AccountModule();
                module.setAccount(saved);
                try {
                    module.setAssetType(AssetType.fromCategoryCode(m.getAssetType()));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("无效的资产类型: " + m.getAssetType());
                }
                module.setModuleName(m.getModuleName());
                module.setStatus(1);
                accountModuleRepository.save(module);
            }
        }

        return saved;
    }

    @Override
    public Account getAccountByInstCodeAndAccountNo(Long userId, String instCode, String accountNo) {
        return accountRepository.findByOwnerIdAndInstCodeAndAccountNoAndDeletedFalse(userId, instCode, accountNo);
    }

    @Override
    public List<AccountRespDTO> getAccountsByUserId(Long userId) {
        List<Account> accounts = accountRepository.findByOwnerIdAndDeletedFalse(userId);
        return accounts.stream()
                .map(account -> {
                    AccountRespDTO dto = AccountRespDTO.fromEntity(account);
                    fillInstitution(dto, account.getInstCode());
                    List<Asset> assets = assetRepository.findByAccountModule_AccountIdAndStatus(account.getId(), 1);
                    dto.setAssets(assets.stream()
                            .map(AssetRespDTO::fromEntity)
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountRespDTO> getAccountsByUserIdAndInstCode(Long userId, String instCode) {
        List<Account> accounts = accountRepository.findByOwnerIdAndInstCodeAndDeletedFalse(userId, instCode);
        return accounts.stream()
                .map(account -> {
                    AccountRespDTO dto = AccountRespDTO.fromEntity(account);
                    fillInstitution(dto, account.getInstCode());
                    List<Asset> assets = assetRepository.findByAccountModule_AccountIdAndStatus(account.getId(), 1);
                    dto.setAssets(assets.stream()
                            .map(AssetRespDTO::fromEntity)
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public AccountRespDTO getAccountById(Long id) {
        Account account = accountRepository.findById(id).orElse(null);
        if (account != null && account.getDeleted()) {
            return null;
        }
        AccountRespDTO dto = AccountRespDTO.fromEntity(account);
        if (account != null) {
            fillInstitution(dto, account.getInstCode());
        }
        List<Asset> assets = assetRepository.findByAccountModule_AccountIdAndStatus(account.getId(), 1);
        dto.setAssets(assets.stream()
                .map(AssetRespDTO::fromEntity)
                .collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional
    public AccountRespDTO updateAccount(UpdateAccountDTO updateAccountDTO, Long userId) {
        Account account = accountRepository.findById(updateAccountDTO.getAccountId()).orElse(null);
        if (account == null) {
            throw new IllegalArgumentException("账户不存在");
        }

        if (!account.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该账户");
        }

        if (account.getDeleted()) {
            throw new IllegalArgumentException("账户已被删除，无法修改");
        }

        if (updateAccountDTO.getAccountName() != null) {
            account.setAccountName(updateAccountDTO.getAccountName());
        }

        if (updateAccountDTO.getAccountNo() != null) {
            Account existingAccount = accountRepository
                    .findByOwnerIdAndInstCodeAndAccountTypeAndAccountNoAndDeletedFalse(
                            userId, account.getInstCode(), account.getAccountType(), updateAccountDTO.getAccountNo());
            if (existingAccount != null && !existingAccount.getId().equals(updateAccountDTO.getAccountId())) {
                throw new IllegalArgumentException("该账号已被其他账户使用");
            }
            account.setAccountNo(updateAccountDTO.getAccountNo());
        }

        if (updateAccountDTO.getIncludeInNetWorth() != null) {
            account.setIncludeInNetWorth(updateAccountDTO.getIncludeInNetWorth());
        }

        Account updatedAccount = accountRepository.save(account);
        AccountRespDTO dto = AccountRespDTO.fromEntity(updatedAccount);
        fillInstitution(dto, updatedAccount.getInstCode());
        return dto;
    }

    @Override
    @Transactional
    public boolean updateAccountStatus(Long id, Integer status, Long userId) {
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("无效的状态值，只能是 0 (归档) 或 1 (活跃)");
        }

        Account account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            throw new IllegalArgumentException("账户不存在");
        }

        if (!account.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该账户");
        }

        if (account.getDeleted()) {
            throw new IllegalArgumentException("账户已被删除，无法更新状态");
        }

        if (account.getStatus().equals(status)) {
            String statusText = status == 1 ? "活跃" : "归档";
            throw new IllegalArgumentException("账户已处于" + statusText + "状态，无需更新");
        }

        account.setStatus(status);
        Integer sortOrder = 9999;
        if (status == 1) {
            sortOrder = accountRepository.findMaxSortOrderByOwnerIdAndStatusAndDeletedFalse(userId, status == 1 ? 0 : 1);
        }
        account.setSortOrder(sortOrder);
        accountRepository.save(account);

        return true;
    }

    @Override
    @Transactional
    public boolean deleteAccount(Long id, Long userId) {
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            throw new IllegalArgumentException("账户不存在");
        }

        if (!account.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该账户");
        }

        if (account.getDeleted()) {
            throw new IllegalArgumentException("账户已被删除");
        }

        account.setDeleted(true);
        accountRepository.save(account);

        return true;
    }

    @Override
    @Transactional
    public void batchUpdateAccounts(Long userId, List<Long> activeAccountIds, List<Long> archivedAccountIds) {
        if (archivedAccountIds != null && !archivedAccountIds.isEmpty()) {
            for (Long accountId : archivedAccountIds) {
                Account account = accountRepository.findById(accountId).orElse(null);
                if (account == null) {
                    throw new IllegalArgumentException("账户不存在: " + accountId);
                }
                if (!account.getOwnerId().equals(userId)) {
                    throw new IllegalArgumentException("无权操作该账户: " + accountId);
                }
                if (account.getDeleted()) {
                    throw new IllegalArgumentException("账户已被删除: " + accountId);
                }

                account.setStatus(0);
                account.setSortOrder(9999);
                accountRepository.save(account);
            }
        }

        if (activeAccountIds != null && !activeAccountIds.isEmpty()) {
            for (int i = 0; i < activeAccountIds.size(); i++) {
                Long accountId = activeAccountIds.get(i);
                Account account = accountRepository.findById(accountId).orElse(null);

                if (account == null) {
                    throw new IllegalArgumentException("账户不存在: " + accountId);
                }
                if (!account.getOwnerId().equals(userId)) {
                    throw new IllegalArgumentException("无权操作该账户: " + accountId);
                }

                if (account.getDeleted()) {
                    throw new IllegalArgumentException("账户已被删除: " + accountId);
                }

                if (account.getStatus() == 0) {
                    account.setStatus(1);
                }

                account.setSortOrder(i);
                accountRepository.save(account);
            }
        }
    }

    private void fillInstitution(AccountRespDTO dto, String instCode) {
        if (instCode == null || instCode.isBlank()) {
            return;
        }
        SysInstitution inst = institutionRepository.findByInstCode(instCode);
        if (inst != null) {
            dto.setInstitution(InstitutionRespDTO.builder()
                    .id(inst.getId())
                    .instCode(inst.getInstCode())
                    .instName(inst.getInstName())
                    .shortName(inst.getShortName())
                    .instType(inst.getInstType())
                    .logoUrl(inst.getLogoUrl())
                    .themeColor(inst.getThemeColor())
                    .indexLetter(inst.getIndexLetter())
                    .isHot(inst.getIsHot())
                    .build());
        }
    }
}

