package com.suiyou.service.impl;

import com.suiyou.dto.asset.CreateAssetDTO;
import com.suiyou.dto.asset.UpdateAssetDTO;
import com.suiyou.model.Asset;
import com.suiyou.repository.AssetRepository;
import com.suiyou.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.suiyou.service.AccountService;
import com.suiyou.model.Account;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;

@Service
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Asset createAsset(CreateAssetDTO assetDTO, Long userId) {
        Account account = accountService.getAccountByInstitutionAndIdentifier(assetDTO.getInstitution(), assetDTO.getInstitutionIdentifier());
        if (account == null) {
            account = accountService.createAccount(assetDTO.getInstitution(), assetDTO.getInstitutionIdentifier(), userId);
            if (account == null) {
                throw new IllegalArgumentException("创建资产账户失败");
            }
        }
        Asset asset = new Asset();
        asset.setAccountId(account.getId());
        asset.setGroupType(assetDTO.getGroupType());
        asset.setCategory(assetDTO.getCategory());
        asset.setSubCategory(assetDTO.getSubCategory());
        asset.setName(assetDTO.getName());

        asset.setTotalBalance(assetDTO.getBalance());
        asset.setFrozenBalance(BigDecimal.ZERO);
        asset.setAvailableBalance(assetDTO.getBalance());

        asset.setCurrency(assetDTO.getCurrency());

        asset.setIncludeInNetWorth(assetDTO.getIncludeInNetWorth());
        asset.setStatus(1);
        
        if (assetDTO.getAttributes() != null && !assetDTO.getAttributes().isEmpty()) {
            try {
                String attributesJson = objectMapper.writeValueAsString(assetDTO.getAttributes());
                asset.setAttributes(attributesJson);
            } catch (Exception e) {
                // 如果序列化失败，记录错误但继续创建资产
                System.err.println("Failed to serialize attributes: " + e.getMessage());
            }
        }

        return assetRepository.save(asset);
    }

    @Override
    @Transactional
    public Asset updateAsset(UpdateAssetDTO assetDTO, Long userId) {
        Asset asset = assetRepository.findById(assetDTO.getId()).orElse(null);
        if (asset == null) {
            throw new IllegalArgumentException("资产不存在");
        }
        asset.setName(assetDTO.getName());
        asset.setCurrency(assetDTO.getCurrency());
        asset.setIncludeInNetWorth(assetDTO.getIncludeInNetWorth());

        // 更新资产属性
        if (assetDTO.getAttributes() != null && !assetDTO.getAttributes().isEmpty()) {
            try {
                String attributesJson = objectMapper.writeValueAsString(assetDTO.getAttributes());
                asset.setAttributes(attributesJson);
            } catch (Exception e) {
                // 如果序列化失败，记录错误但继续更新资产
                System.err.println("Failed to serialize attributes: " + e.getMessage());
            }
        }

        // 更新资产账户ID
        Account account = accountService.getAccountById(assetDTO.getAccountId());
        if (Objects.isNull(account)) {
            account = accountService.createAccount(assetDTO.getInstitution(), assetDTO.getInstitutionIdentifier(), userId);
            if (Objects.isNull(account)) {
                throw new IllegalArgumentException("创建资产账户失败");
            }
        }
        asset.setAccountId(account.getId());

        return assetRepository.save(asset);
    }

    @Override
    public Asset getAssetById(Long id) {
        return assetRepository.findById(id).orElse(null);
    }

    @Override
    public List<Asset> getAssetsByAccountId(Long accountId) {
        return assetRepository.findByAccountId(accountId);
    }

    @Override
    public List<Asset> getAssetsByUserId(Long userId) {
        return assetRepository.findByOwnerIdAndStatus(userId, 1);
    }

    @Override
    public List<Asset> getAssetsByAccountIdAndStatus(Long accountId, Integer status) {
        return assetRepository.findByAccountIdAndStatus(accountId, status);
    }

    @Override
    public List<Asset> getAssetsByAccountIdAndCategory(Long accountId, String category) {
        return assetRepository.findByAccountIdAndCategory(accountId, category);
    }

    @Override
    public List<Asset> getAssetsByAccountIdAndGroupType(Long accountId, String groupType) {
        return assetRepository.findByAccountIdAndGroupType(accountId, groupType);
    }

    @Override
    @Transactional
    public boolean deleteAsset(Long id) {
        return assetRepository.findById(id)
                .map(asset -> {
                    asset.setStatus(0);
                    assetRepository.save(asset);
                    return true;
                })
                .orElse(false);
    }
}
