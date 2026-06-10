package com.suiyou.service.impl;

import com.suiyou.dto.asset.AssetCreateRequest;
import com.suiyou.dto.asset.AssetResponse;
import com.suiyou.dto.asset.AssetUpdateRequest;
import com.suiyou.model.Asset;
import com.suiyou.repository.AssetRepository;
import com.suiyou.strategy.AssetStrategy;
import com.suiyou.strategy.AssetStrategyFactory;
import com.suiyou.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetStrategyFactory strategyFactory;

    @Override
    @Transactional
    public AssetResponse createAsset(AssetCreateRequest request, String strategyType) {
        AssetStrategy strategy = strategyFactory.getStrategy(strategyType);
        Asset asset = strategy.createAsset(request);
        Asset savedAsset = assetRepository.save(asset);
        return toAssetResponse(savedAsset);
    }

    @Override
    @Transactional
    public AssetResponse updateBasicInfo(Long id, AssetUpdateRequest request, String strategyType) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));
        
        AssetStrategy strategy = strategyFactory.getStrategy(strategyType);
        Asset updatedAsset = strategy.updateBasicInfo(asset, request);
        Asset savedAsset = assetRepository.save(updatedAsset);
        return toAssetResponse(savedAsset);
    }

    @Override
    @Transactional
    public AssetResponse updateNetWorth(Long id, BigDecimal newTotalBalance, String strategyType) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));
        
        AssetStrategy strategy = strategyFactory.getStrategy(strategyType);
        Asset updatedAsset = strategy.updateNetWorth(asset, newTotalBalance);
        Asset savedAsset = assetRepository.save(updatedAsset);
        return toAssetResponse(savedAsset);
    }

    @Override
    public AssetResponse getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));
        return toAssetResponse(asset);
    }

    @Override
    public List<AssetResponse> getAssetsByAccountId(Long accountId) {
        List<Asset> assets = assetRepository.findByAccountId(accountId);
        return assets.stream()
                .map(this::toAssetResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssetResponse> getAssetsByOwnerId(Long ownerId) {
        List<Asset> assets = assetRepository.findByOwnerId(ownerId);
        return assets.stream()
                .map(this::toAssetResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAsset(Long id) {
        if (!assetRepository.existsById(id)) {
            throw new IllegalArgumentException("Asset not found: " + id);
        }
        assetRepository.deleteById(id);
    }

    private AssetResponse toAssetResponse(Asset asset) {
        return AssetResponse.builder()
                .id(asset.getId())
                .accountId(asset.getAccountId())
                .accountModuleId(asset.getAccountModuleId())
                .ownerId(asset.getOwnerId())
                .groupType(asset.getGroupType())
                .category(asset.getCategory())
                .subCategory(asset.getSubCategory())
                .name(asset.getName())
                .totalBalance(asset.getTotalBalance())
                .frozenBalance(asset.getFrozenBalance())
                .availableBalance(asset.getAvailableBalance())
                .currency(asset.getCurrency())
                .includeInNetWorth(asset.getIncludeInNetWorth())
                .valuationMode(asset.getValuationMode())
                .status(asset.getStatus())
                .attributes(asset.getAttributes())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }
}