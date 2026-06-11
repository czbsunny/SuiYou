package com.suiyou.strategy.impl;

import com.suiyou.dto.asset.AssetCreateRequest;
import com.suiyou.dto.asset.AssetUpdateRequest;
import com.suiyou.model.Asset;
import com.suiyou.strategy.AssetStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PensionStrategy implements AssetStrategy {

    @Override
    public String getStrategyType() {
        return "PENSION";
    }

    @Override
    public Asset createAsset(AssetCreateRequest request) {
        Asset asset = new Asset();
        asset.setAccountId(request.getAccountId());
        asset.setAccountModuleId(request.getAccountModuleId());
        asset.setOwnerId(request.getOwnerId());
        asset.setGroupType("SOCIAL_SECURITY");
        asset.setCategory("PENSION");
        asset.setSubCategory(request.getSubCategory() != null ? request.getSubCategory() : "INDIVIDUAL");
        asset.setName(request.getName());
        asset.setTotalBalance(request.getTotalBalance() != null ? request.getTotalBalance() : BigDecimal.ZERO);
        asset.setFrozenBalance(request.getTotalBalance() != null ? request.getTotalBalance() : BigDecimal.ZERO);
        asset.setCurrency(request.getCurrency() != null ? request.getCurrency() : "CNY");
        asset.setIncludeInNetWorth(request.getIncludeInNetWorth() != null ? request.getIncludeInNetWorth() : true);
        asset.setValuationMode("CALCULATED");
        asset.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        asset.setAttributes(request.getAttributes());
        
        asset.setAvailableBalance(BigDecimal.ZERO);
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
        asset.setFrozenBalance(asset.getTotalBalance());
        asset.setValuationMode("CALCULATED");
        asset.setAvailableBalance(BigDecimal.ZERO);
        return asset;
    }
}