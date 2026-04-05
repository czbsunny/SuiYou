package com.suiyou.service.impl;

import com.suiyou.dto.asset.AssetRespDTO;
import com.suiyou.dto.asset.CreateAssetDTO;
import com.suiyou.dto.asset.UpdateAssetDTO;
import com.suiyou.dto.portfolio.CreatePortfolioDTO;
import com.suiyou.model.Asset;
import com.suiyou.model.Account;
import com.suiyou.model.SysAssetCategory;
import com.suiyou.model.SysInstitution;

import com.suiyou.repository.AssetRepository;
import com.suiyou.repository.SysAssetCategoryRepository;
import com.suiyou.repository.SysInstitutionRepository;

import com.suiyou.service.AssetService;
import com.suiyou.service.AccountService;
import com.suiyou.service.PortfolioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suiyou.config.DefaultAssetConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private SysAssetCategoryRepository assetCategoryRepository;

    @Autowired
    private SysInstitutionRepository institutionRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PortfolioService portfolioService;

    @Override
    @Transactional
    public AssetRespDTO createAsset(CreateAssetDTO assetDTO, Long userId) {
        Account account = accountService.getAccountByInstitutionAndIdentifier(userId, assetDTO.getAccountDTO().getInstitution(), assetDTO.getAccountDTO().getInstitutionIdentifier());
        if (account == null) {
            account = accountService.createAccount(assetDTO.getAccountDTO(), userId);
            if (account == null) {
                throw new IllegalArgumentException("创建资产账户失败");
            }

            SysInstitution institution = institutionRepository.findByInstCode(account.getInstitution());
            String baseAssetType = DefaultAssetConfig.getBaseAssetType(institution.getInstType(), institution.getInstCode());
            SysAssetCategory category = assetCategoryRepository.findByCategoryCode(baseAssetType);
            Asset baseAsset = new Asset();
            baseAsset.setAccount(account);
            baseAsset.setOwnerId(userId);
            baseAsset.setGroupType(category.getGroupType());
            baseAsset.setCategory(category.getParentCode());
            baseAsset.setSubCategory(category.getCategoryCode());
            baseAsset.setName(category.getName());
            baseAsset.setTotalBalance(BigDecimal.ZERO);
            baseAsset.setFrozenBalance(BigDecimal.ZERO);
            baseAsset.setAvailableBalance(BigDecimal.ZERO);
            baseAsset.setCurrency("CNY");
            baseAsset.setIncludeInNetWorth(true);
            baseAsset.setStatus(1);

            assetRepository.save(baseAsset);
        }
        // 创建Account实体用于关联
        Asset asset = new Asset();
        asset.setAccount(account);
        asset.setOwnerId(userId);
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

        AssetRespDTO assetRespDTO = AssetRespDTO.fromEntity(assetRepository.save(asset));
        if (assetRespDTO.getSubCategory().equals("FUND")) {
            CreatePortfolioDTO createPortfolioDTO = new CreatePortfolioDTO();
            createPortfolioDTO.setAssetId(assetRespDTO.getId());
            createPortfolioDTO.setName("默认组合");
            createPortfolioDTO.setDescription("系统创建的基金组合");
            portfolioService.createPortfolio(createPortfolioDTO, userId);
        } else if (assetRespDTO.getSubCategory().equals("STOCK")) {
            CreatePortfolioDTO createPortfolioDTO = new CreatePortfolioDTO();
            createPortfolioDTO.setAssetId(assetRespDTO.getId());
            createPortfolioDTO.setName("默认组合");
            createPortfolioDTO.setDescription("系统创建的股票组合");
            portfolioService.createPortfolio(createPortfolioDTO, userId);
        }
        return assetRespDTO;
    }

    @Override
    @Transactional
    public AssetRespDTO updateAsset(UpdateAssetDTO assetDTO, Long userId) {
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

        return AssetRespDTO.fromEntity(assetRepository.save(asset));
    }

    @Override
    public AssetRespDTO getAssetById(Long id) {
        return AssetRespDTO.fromEntity(assetRepository.findById(id).orElse(null));
    }

    @Override
    public List<AssetRespDTO> getAssetsByAccountId(Long accountId) {
        return assetRepository.findByAccountId(accountId).stream()
                .map(AssetRespDTO::fromEntity)
                .collect(Collectors.toList());  
    }

    @Override
    public List<AssetRespDTO> getAssetsByUserId(Long userId) {
        List<Asset> assets = assetRepository.findByOwnerIdAndStatus(userId, 1);

        List<AssetRespDTO> assetRespDTOs = assets.stream()
                .map(AssetRespDTO::fromEntity)
                .collect(Collectors.toList());
        return assetRespDTOs;
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
