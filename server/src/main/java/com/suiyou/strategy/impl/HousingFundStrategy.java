package com.suiyou.strategy.impl;

import com.suiyou.dto.asset.AssetCreateRequest;
import com.suiyou.dto.asset.AssetUpdateRequest;
import com.suiyou.model.Asset;
import com.suiyou.strategy.AssetStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HousingFundStrategy implements AssetStrategy {

    @Override
    public String getStrategyType() {
        return "HOUSING_FUND";
    }

    @Override
    public Asset createAsset(AssetCreateRequest request) {
        Asset asset = new Asset();
        asset.setAccountId(request.getAccountId());
        asset.setAccountModuleId(request.getAccountModuleId());
        asset.setOwnerId(request.getOwnerId());
        asset.setGroupType("SOCIAL_SECURITY");
        asset.setCategory("HOUSING_FUND");
        asset.setSubCategory(request.getSubCategory() != null ? request.getSubCategory() : "INDIVIDUAL");
        asset.setName(request.getName());
        asset.setTotalBalance(request.getTotalBalance() != null ? request.getTotalBalance() : BigDecimal.ZERO);
        asset.setFrozenBalance(request.getFrozenBalance() != null ? request.getFrozenBalance() : BigDecimal.ZERO);
        asset.setCurrency(request.getCurrency() != null ? request.getCurrency() : "CNY");
        asset.setIncludeInNetWorth(request.getIncludeInNetWorth() != null ? request.getIncludeInNetWorth() : true);
        asset.setValuationMode("CALCULATED");
        asset.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        asset.setAttributes(request.getAttributes());
        
        asset.setAvailableBalance(asset.getTotalBalance().subtract(asset.getFrozenBalance()));
        return asset;
    }

    @Override
    public Asset updateBasicInfo(Asset asset, AssetUpdateRequest request) {
        if (request.getName() != null) {
            asset.setName(request.getName());
        }
        if (request.getSubCategory() != null) {
            asset.setSubCategory(request.getSubCategory());
        }
        if (request.getCurrency() != null) {
            asset.setCurrency(request.getCurrency());
        }
        if (request.getIncludeInNetWorth() != null) {
            asset.setIncludeInNetWorth(request.getIncludeInNetWorth());
        }
        if (request.getAttributes() != null) {
            asset.setAttributes(request.getAttributes());
        }
        return asset;
    }

    @Override
    public Asset updateNetWorth(Asset asset, BigDecimal newTotalBalance) {
        asset.setTotalBalance(newTotalBalance != null ? newTotalBalance : BigDecimal.ZERO);
        asset.setValuationMode("CALCULATED");
        asset.setAvailableBalance(asset.getTotalBalance().subtract(asset.getFrozenBalance()));
        return asset;
    }
}