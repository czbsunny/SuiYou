package com.suiyou.strategy.update;

import com.suiyou.model.Holding;
import com.suiyou.strategy.HoldingUpdateStrategy;
import com.suiyou.strategy.UpdateContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PeriodicInflowUpdateStrategy implements HoldingUpdateStrategy {

    @Override
    public String getStrategyType() {
        return "PERIODIC_INFLOW";
    }

    @Override
    public Holding update(Holding holding, UpdateContext updateContext) {
        BigDecimal inflowAmount = updateContext.getInflowAmount() != null ? updateContext.getInflowAmount() : BigDecimal.ZERO;
        BigDecimal annualRate = updateContext.getAnnualRate() != null ? updateContext.getAnnualRate() : BigDecimal.ZERO;
        int days = updateContext.getDays() != null ? updateContext.getDays() : 0;
        
        BigDecimal interest = BigDecimal.ZERO;
        if (days > 0) {
            interest = holding.getTotalBalance()
                    .multiply(annualRate)
                    .multiply(new BigDecimal(days))
                    .divide(new BigDecimal("365"), 8, BigDecimal.ROUND_HALF_UP);
        }
        
        BigDecimal newTotalBalance = holding.getTotalBalance().add(inflowAmount).add(interest);
        
        holding.setTotalBalance(newTotalBalance);
        holding.setValuationMode("CALCULATED");
        holding.setAvailableBalance(holding.getTotalBalance().subtract(holding.getFrozenBalance()));
        return holding;
    }
}