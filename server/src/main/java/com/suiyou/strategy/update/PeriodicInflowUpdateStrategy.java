package com.suiyou.strategy.update;

import com.suiyou.model.Asset;
import com.suiyou.strategy.AssetUpdateStrategy;
import com.suiyou.strategy.UpdateContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PeriodicInflowUpdateStrategy implements AssetUpdateStrategy {

    @Override
    public String getStrategyType() {
        return "PERIODIC_INFLOW";
    }

    @Override
    public Asset update(Asset asset, UpdateContext updateContext) {
        BigDecimal inflowAmount = updateContext.getInflowAmount() != null ? updateContext.getInflowAmount() : BigDecimal.ZERO;
        BigDecimal annualRate = updateContext.getAnnualRate() != null ? updateContext.getAnnualRate() : BigDecimal.ZERO;
        int days = updateContext.getDays() != null ? updateContext.getDays() : 0;
        
        BigDecimal interest = BigDecimal.ZERO;
        if (days > 0) {
            interest = asset.getTotalBalance()
                    .multiply(annualRate)
                    .multiply(new BigDecimal(days))
                    .divide(new BigDecimal("365"), 8, BigDecimal.ROUND_HALF_UP);
        }
        
        BigDecimal newTotalBalance = asset.getTotalBalance().add(inflowAmount).add(interest);
        
        asset.setTotalBalance(newTotalBalance);
        asset.setValuationMode("CALCULATED");
        asset.setAvailableBalance(asset.getTotalBalance().subtract(asset.getFrozenBalance()));
        return asset;
    }
}