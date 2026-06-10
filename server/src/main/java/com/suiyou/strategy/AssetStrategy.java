package com.suiyou.strategy;

import com.suiyou.dto.asset.AssetCreateRequest;
import com.suiyou.dto.asset.AssetUpdateRequest;
import com.suiyou.model.Asset;

import java.math.BigDecimal;

public interface AssetStrategy {
    String getStrategyType();
    
    Asset createAsset(AssetCreateRequest request);
    
    Asset updateBasicInfo(Asset asset, AssetUpdateRequest request);
    
    Asset updateNetWorth(Asset asset, BigDecimal newTotalBalance);
}