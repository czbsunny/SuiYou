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

        BigDecimal dailyEarning = holding.getAmount().multiply(dailyYield).divide(new BigDecimal("10000"), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal newQty = holding.getQty().add(dailyEarning);

        holding.setQty(newQty);
        return holding;
    }
}