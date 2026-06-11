package com.suiyou.strategy.update;

import com.suiyou.model.Asset;
import com.suiyou.strategy.AssetUpdateStrategy;
import com.suiyou.strategy.UpdateContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RevaluationUpdateStrategy implements AssetUpdateStrategy {

    @Override
    public String getStrategyType() {
        return "REVALUATION";
    }

    @Override
    public Asset update(Asset asset, UpdateContext updateContext) {
        String revaluationType = updateContext.getRevaluationType();
        
        if ("MANUAL".equalsIgnoreCase(revaluationType)) {
            if (updateContext.getPrice() != null) {
                asset.setTotalBalance(updateContext.getPrice());
            }
        } else if ("DEPRECIATION".equalsIgnoreCase(revaluationType)) {
            BigDecimal depreciationRate = updateContext.getDepreciationRate() != null ? updateContext.getDepreciationRate() : new BigDecimal("0.1");
            int yearsUsed = updateContext.getYearsUsed() != null ? updateContext.getYearsUsed() : 1;
            
            BigDecimal depreciationFactor = BigDecimal.ONE.subtract(depreciationRate).pow(yearsUsed);
            BigDecimal newTotalBalance = asset.getTotalBalance().multiply(depreciationFactor);
            asset.setTotalBalance(newTotalBalance);
        } else if (updateContext.getPrice() != null) {
            asset.setTotalBalance(updateContext.getPrice());
        }
        
        asset.setValuationMode("MANUAL");
        asset.setAvailableBalance(asset.getTotalBalance().subtract(asset.getFrozenBalance()));
        return asset;
    }
}