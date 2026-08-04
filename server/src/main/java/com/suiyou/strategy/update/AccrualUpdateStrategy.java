package com.suiyou.strategy.update;

import com.suiyou.model.Holding;
import com.suiyou.strategy.HoldingUpdateStrategy;
import com.suiyou.strategy.UpdateContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccrualUpdateStrategy implements HoldingUpdateStrategy {

    @Override
    public String getStrategyType() {
        return "ACCRUAL";
    }

    @Override
    public Holding update(Holding holding, UpdateContext updateContext) {
        BigDecimal annualRate = updateContext.getAnnualRate() != null ? updateContext.getAnnualRate() : BigDecimal.ZERO;
        int days = updateContext.getDays() != null ? updateContext.getDays() : 1;
        
        BigDecimal interest = holding.getTotalBalance()
                .multiply(annualRate)
                .multiply(new BigDecimal(days))
                .divide(new BigDecimal("365"), 8, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal newTotalBalance = holding.getTotalBalance().add(interest);
        
        holding.setTotalBalance(newTotalBalance);
        holding.setValuationMode("CALCULATED");
        holding.setAvailableBalance(holding.getTotalBalance().subtract(holding.getFrozenBalance()));
        return holding;
    }
}