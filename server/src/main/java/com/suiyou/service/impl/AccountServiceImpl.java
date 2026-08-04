package com.suiyou.service.impl;

import com.suiyou.dto.account.AccountListItemRespDTO;
import com.suiyou.dto.account.AssetDTO;
import com.suiyou.dto.account.AssetRespDTO;
import com.suiyou.dto.account.AccountRespDTO;
import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.dto.account.UpdateAccountDTO;
import com.suiyou.enums.AccountType;
import com.suiyou.enums.InstType;
import com.suiyou.enums.AssetType;
import com.suiyou.model.Account;
import com.suiyou.model.Asset;
import com.suiyou.model.SysAccountTemplate;
import com.suiyou.model.SysInstitution;
import com.suiyou.repository.AssetRepository;
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
    private AssetRepository assetRepository;

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

        if (dto.getAssets() != null && !dto.getAssets().isEmpty()) {
            List<Asset> assets = new ArrayList<>();
            int sortOrder = 0;
            for (AssetDTO assetDTO : dto.getAssets()) {
                Asset asset = new Asset();
                asset.setAccountId(savedAccount.getId());
                asset.setAssetType(assetDTO.getAssetType());
                asset.setAssetName(assetDTO.getAssetName());

                SysAccountTemplate template = sysAccountTemplateRepository.findByInstCodeAndAccountTypeAndAssetType(dto.getInstCode(), dto.getAccountType(), assetDTO.getAssetType());
                if (template != null) {
                    asset.setIconUrl(template.getIconUrl());
                    asset.setCanPay(template.getCanPay() ? 1 : 0);
                }
                
                AssetType assetType = AssetType.ofCode(assetDTO.getAssetType());
                if (assetType == null) {
                    throw new IllegalArgumentException("Asset type not found: " + assetDTO.getAssetType() + " for asset: " + assetDTO.getAssetName());
                }
                asset.setBgColor(assetType.getBgColor());
                asset.setIconUrl(assetType.getIconUrl());
                asset.setCanPay(assetType.isCanPay() ? 1 : 0);
                
                asset.setSortOrder(sortOrder++);
                assets.add(asset);
            }
            assetRepository.saveAll(assets);
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
        List<Asset> allAssets = assetRepository.findByAccountId(savedAccount.getId());
        Map<String, Asset> assetMap = allAssets.stream()
                .collect(Collectors.toMap(Asset::getAssetType, Function.identity()));

        Set<String> dtoAssetTypes = dto.getAssets() != null
                ? dto.getAssets().stream().map(AssetDTO::getAssetType).collect(Collectors.toSet())
                : Collections.emptySet();

        List<Asset> assetsToSave = new ArrayList<>();

        // 处理需要禁用或删除的模块（在表中但不在 DTO 中）
        for (Asset existing : allAssets) {
            if (!dtoAssetTypes.contains(existing.getAssetType()) && existing.getIsEnabled() == 1) {
                existing.setIsEnabled(0);
                assetsToSave.add(existing);
            }
        }

        // 处理 DTO 中的模块
        if (!CollectionUtils.isEmpty(dto.getAssets())) {
            int sortOrder = 0;
            for (AssetDTO assetDTO : dto.getAssets()) {
                Asset existing = assetMap.get(assetDTO.getAssetType());
                if (existing != null) {
                    // 已存在，启用并更新
                    existing.setIsEnabled(1);
                    existing.setAssetName(assetDTO.getAssetName());
                    existing.setSortOrder(sortOrder++);
                    assetsToSave.add(existing);
                } else {
                    // 不存在，新建
                    Asset asset = buildNewAsset(savedAccount.getId(), assetDTO, sortOrder++);
                    assetsToSave.add(asset);
                }
            }
        }

        if (!assetsToSave.isEmpty()) {
            assetRepository.saveAll(assetsToSave);
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
        List<Asset> assets = assetRepository.findByAccountId(account.getId());
        if (!assets.isEmpty()) {
            assetRepository.deleteAll(assets);
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
        List<Asset> assets = assetRepository.findByAccountIdAndIsEnabled(account.getId(), 1);
        List<AssetRespDTO> assetDTOs = assets.stream()
                .map(this::toAssetRespDTO)
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
                .assets(assetDTOs)
                .createdAt(account.getCreatedAt())
                .build();
    }

    private AssetRespDTO toAssetRespDTO(Asset asset) {
        return AssetRespDTO.builder()
                .id(asset.getId())
                .assetType(asset.getAssetType())
                .assetName(asset.getAssetName())
                .iconUrl(asset.getIconUrl())
                .bgColor(asset.getBgColor())
                .canPay(asset.getCanPay())
                .sortOrder(asset.getSortOrder())
                .build();
    }

    private Asset buildNewAsset(Long accountId, AssetDTO assetDTO, int sortOrder) {
        Asset asset = new Asset();
        asset.setAccountId(accountId);
        asset.setAssetType(assetDTO.getAssetType());
        asset.setAssetName(assetDTO.getAssetName());

        AssetType assetType = AssetType.ofCode(assetDTO.getAssetType());
        if (assetType == null) {
            throw new IllegalArgumentException("Asset type not found: " + assetDTO.getAssetType() + " for asset: " + assetDTO.getAssetName());
        }
        asset.setIconUrl(assetType.getIconUrl());
        asset.setBgColor(assetType.getBgColor());
        asset.setCanPay(assetType.isCanPay() ? 1 : 0);
        asset.setSortOrder(sortOrder);
        asset.setIsEnabled(1);
        return asset;
    }
}