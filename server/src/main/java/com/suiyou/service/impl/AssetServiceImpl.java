package com.suiyou.service.impl;

import com.suiyou.dto.asset.AssetRespDTO;
import com.suiyou.dto.asset.CreateAssetDTO;
import com.suiyou.dto.asset.UpdateAssetDTO;
import com.suiyou.dto.portfolio.CreatePortfolioDTO;
import com.suiyou.model.Asset;
import com.suiyou.model.AccountModule;
import com.suiyou.model.SysAssetCategory;
import com.suiyou.model.enums.PortfolioType;

import com.suiyou.repository.AssetRepository;
import com.suiyou.repository.SysAssetCategoryRepository;

import com.suiyou.service.AssetService;
import com.suiyou.service.AccountModuleService;
import com.suiyou.service.PortfolioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private AccountModuleService accountModuleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PortfolioService portfolioService;

    @Override
    @Transactional
    public AssetRespDTO createAsset(CreateAssetDTO assetDTO, Long userId) {
        AccountModule accountModule = accountModuleService.getModuleById(assetDTO.getModuleId());
        
        if (accountModule == null) {
            throw new IllegalArgumentException("资产模块不存在");
        }
        
        if (!accountModule.getAccount().getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该资产模块");
        }
        
        SysAssetCategory category = assetCategoryRepository.findByCategoryCode(assetDTO.getSubCategory());
        if (category == null) {
            throw new IllegalArgumentException("无效的资产分类: " + assetDTO.getSubCategory());
        }
        
        Asset asset = new Asset();
        asset.setAccountModule(accountModule);
        asset.setOwnerId(userId);
        asset.setGroupType(category.getGroupType());
        asset.setCategory(category.getParentCode());
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
            createPortfolioDTO.setType(PortfolioType.FUND);
            portfolioService.createPortfolio(createPortfolioDTO, userId);
        } else if (assetRespDTO.getSubCategory().equals("STOCK")) {
            CreatePortfolioDTO createPortfolioDTO = new CreatePortfolioDTO();
            createPortfolioDTO.setAssetId(assetRespDTO.getId());
            createPortfolioDTO.setName("默认组合");
            createPortfolioDTO.setDescription("系统创建的股票组合");
            createPortfolioDTO.setType(PortfolioType.STOCK);
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
        return assetRepository.findByAccountModule_AccountId(accountId).stream()
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
    
    @Override
    public BigDecimal getCurrentNetWorth(Long userId) {
        // 获取用户所有有效资产
        List<Asset> assets = assetRepository.findByOwnerIdAndStatus(userId, 1);
        
        // 计算总资产和总负债
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        
        for (Asset asset : assets) {
            if ("ASSET".equals(asset.getGroupType())) {
                // 资产类别的余额累加
                totalAssets = totalAssets.add(asset.getTotalBalance());
            } else if ("LIABILITY".equals(asset.getGroupType())) {
                // 负债类别的余额累加
                totalLiabilities = totalLiabilities.add(asset.getTotalBalance());
            }
        }
        
        // 计算净资产：总资产 - 总负债
        return totalAssets.subtract(totalLiabilities);
    }
}
