package com.suiyou.strategy.impl;

import com.suiyou.dto.holding.HoldingCreateRequest;
import com.suiyou.dto.holding.HoldingUpdateRequest;
import com.suiyou.model.Holding;
import com.suiyou.strategy.HoldingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PensionStrategy implements HoldingStrategy {

    @Override
    public String getStrategyType() {
        return "PENSION";
    }

    @Override
    public Holding createHolding(HoldingCreateRequest request) {
        Holding holding = new Holding();
        holding.setAccountId(request.getAccountId());
        holding.setAssetId(request.getAssetId());
        holding.setOwnerId(request.getOwnerId());
        holding.setGroupType("SOCIAL_SECURITY");
        holding.setCategory("PENSION");
        holding.setSubCategory(request.getSubCategory() != null ? request.getSubCategory() : "INDIVIDUAL");
        holding.setName(request.getName());
        holding.setTotalBalance(request.getTotalBalance() != null ? request.getTotalBalance() : BigDecimal.ZERO);
        holding.setFrozenBalance(request.getTotalBalance() != null ? request.getTotalBalance() : BigDecimal.ZERO);
        holding.setCurrency(request.getCurrency() != null ? request.getCurrency() : "CNY");
        holding.setIncludeInNetWorth(request.getIncludeInNetWorth() != null ? request.getIncludeInNetWorth() : true);
        holding.setValuationMode("CALCULATED");
        holding.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        holding.setAttributes(request.getAttributes());
        
        holding.setAvailableBalance(BigDecimal.ZERO);
        return holding;
    }

    @Override
    public Holding updateBasicInfo(Holding holding, HoldingUpdateRequest request) {
        if (request.getName() != null) {
            holding.setName(request.getName());
        }
        if (request.getSubCategory() != null) {
            holding.setSubCategory(request.getSubCategory());
        }
        if (request.getCurrency() != null) {
            holding.setCurrency(request.getCurrency());
        }
        if (request.getIncludeInNetWorth() != null) {
            holding.setIncludeInNetWorth(request.getIncludeInNetWorth());
        }
        if (request.getAttributes() != null) {
            holding.setAttributes(request.getAttributes());
        }
        return holding;
    }

    @Override
    public Holding updateNetWorth(Holding holding, BigDecimal newTotalBalance) {
        holding.setTotalBalance(newTotalBalance != null ? newTotalBalance : BigDecimal.ZERO);
        holding.setFrozenBalance(holding.getTotalBalance());
        holding.setValuationMode("CALCULATED");
        holding.setAvailableBalance(BigDecimal.ZERO);
        return holding;
    }
}