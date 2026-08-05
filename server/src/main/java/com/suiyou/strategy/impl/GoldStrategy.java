package com.suiyou.strategy.impl;

import com.suiyou.dto.holding.HoldingCreateRequest;
import com.suiyou.dto.holding.HoldingUpdateRequest;
import com.suiyou.model.Holding;
import com.suiyou.enums.HoldingType;
import com.suiyou.strategy.HoldingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class GoldStrategy implements HoldingStrategy {

    @Override
    public String getStrategyType() {
        return "GOLD";
    }

    @Override
    public Holding createHolding(HoldingCreateRequest request) {
        Holding holding = new Holding();
        holding.setAccountId(request.getAccountId());
        holding.setAssetId(request.getAssetId());
        holding.setProductId(request.getProductId());
        holding.setName(request.getName());
        holding.setQty(request.getQty() != null ? request.getQty() : BigDecimal.ZERO);
        holding.setPrice(request.getPrice() != null ? request.getPrice() : BigDecimal.ONE);
        holding.setCostBasis(request.getCostBasis() != null ? request.getCostBasis() : BigDecimal.ZERO);
        holding.setRealizedPnl(request.getRealizedPnl() != null ? request.getRealizedPnl() : BigDecimal.ZERO);
        holding.setSide(request.getSide() != null ? request.getSide() : "asset");
        holding.setStatus(request.getStatus() != null ? request.getStatus() : "active");
        holding.setHoldingType(HoldingType.GOLD.getCode());
        holding.setExtraAttributes(request.getExtraAttributes());

        if (request.getAmount() != null) {
            holding.setAmount(request.getAmount());
        }
        return holding;
    }

    @Override
    public Holding updateBasicInfo(Holding holding, HoldingUpdateRequest request) {
        if (request.getName() != null) {
            holding.setName(request.getName());
        }
        if (request.getQty() != null) {
            holding.setQty(request.getQty());
        }
        if (request.getPrice() != null) {
            holding.setPrice(request.getPrice());
        }
        if (request.getCostBasis() != null) {
            holding.setCostBasis(request.getCostBasis());
        }
        if (request.getExtraAttributes() != null) {
            holding.setExtraAttributes(request.getExtraAttributes());
        }
        return holding;
    }

    @Override
    public Holding updatePrice(Holding holding, BigDecimal newPrice) {
        holding.setPrice(newPrice != null ? newPrice : BigDecimal.ONE);
        return holding;
    }
}