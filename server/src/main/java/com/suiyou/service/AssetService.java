package com.suiyou.service;

import com.suiyou.dto.asset.AssetCreateRequest;
import com.suiyou.dto.asset.AssetResponse;
import com.suiyou.dto.asset.AssetUpdateRequest;

import java.math.BigDecimal;
import java.util.List;

public interface AssetService {
    AssetResponse createAsset(AssetCreateRequest request, String strategyType);
    
    AssetResponse updateBasicInfo(Long id, AssetUpdateRequest request, String strategyType);
    
    AssetResponse updateNetWorth(Long id, BigDecimal newTotalBalance, String strategyType);
    
    AssetResponse getAssetById(Long id);
    
    List<AssetResponse> getAssetsByAccountId(Long accountId);
    
    List<AssetResponse> getAssetsByOwnerId(Long ownerId);
    
    void deleteAsset(Long id);
}