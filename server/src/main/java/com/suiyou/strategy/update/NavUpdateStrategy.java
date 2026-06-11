package com.suiyou.strategy.update;

import com.suiyou.model.Asset;
import com.suiyou.strategy.AssetUpdateStrategy;
import com.suiyou.strategy.UpdateContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NavUpdateStrategy implements AssetUpdateStrategy {

    @Override
    public String getStrategyType() {
        return "NAV";
    }

    @Override
    public Asset update(Asset asset, UpdateContext updateContext) {
        BigDecimal price = updateContext.getPrice() != null ? updateContext.getPrice() : BigDecimal.ZERO;
        BigDecimal quantity = updateContext.getQuantity() != null ? updateContext.getQuantity() : BigDecimal.ONE;
        
        BigDecimal newTotalBalance = price.multiply(quantity);
        asset.setTotalBalance(newTotalBalance);
        asset.setValuationMode("CALCULATED");
        asset.setAvailableBalance(asset.getTotalBalance().subtract(asset.getFrozenBalance()));
        return asset;
    }
}