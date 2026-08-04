package com.suiyou.strategy.update;

import com.suiyou.model.Holding;
import com.suiyou.strategy.HoldingUpdateStrategy;
import com.suiyou.strategy.UpdateContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DailyYieldUpdateStrategy implements HoldingUpdateStrategy {

    @Override
    public String getStrategyType() {
        return "DAILY_YIELD";
    }

    @Override
    public Holding update(Holding holding, UpdateContext updateContext) {
        BigDecimal dailyYield = updateContext.getDailyYield() != null ? updateContext.getDailyYield() : BigDecimal.ZERO;
        
        BigDecimal dailyEarning = holding.getTotalBalance().multiply(dailyYield).divide(new BigDecimal("10000"), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal newTotalBalance = holding.getTotalBalance().add(dailyEarning);
        
        holding.setTotalBalance(newTotalBalance);
        holding.setValuationMode("CALCULATED");
        holding.setAvailableBalance(holding.getTotalBalance().subtract(holding.getFrozenBalance()));
        return holding;
    }
}