package com.suiyou.strategy.update;

import com.suiyou.model.Asset;
import com.suiyou.strategy.AssetUpdateStrategy;
import com.suiyou.strategy.UpdateContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DailyYieldUpdateStrategy implements AssetUpdateStrategy {

    @Override
    public String getStrategyType() {
        return "DAILY_YIELD";
    }

    @Override
    public Asset update(Asset asset, UpdateContext updateContext) {
        BigDecimal dailyYield = updateContext.getDailyYield() != null ? updateContext.getDailyYield() : BigDecimal.ZERO;
        
        BigDecimal dailyEarning = asset.getTotalBalance().multiply(dailyYield).divide(new BigDecimal("10000"), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal newTotalBalance = asset.getTotalBalance().add(dailyEarning);
        
        asset.setTotalBalance(newTotalBalance);
        asset.setValuationMode("CALCULATED");
        asset.setAvailableBalance(asset.getTotalBalance().subtract(asset.getFrozenBalance()));
        return asset;
    }
}