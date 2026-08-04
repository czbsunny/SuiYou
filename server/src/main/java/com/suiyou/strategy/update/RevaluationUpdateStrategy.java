package com.suiyou.strategy.update;

import com.suiyou.model.Holding;
import com.suiyou.strategy.HoldingUpdateStrategy;
import com.suiyou.strategy.UpdateContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RevaluationUpdateStrategy implements HoldingUpdateStrategy {

    @Override
    public String getStrategyType() {
        return "REVALUATION";
    }

    @Override
    public Holding update(Holding holding, UpdateContext updateContext) {
        String revaluationType = updateContext.getRevaluationType();
        
        if ("MANUAL".equalsIgnoreCase(revaluationType)) {
            if (updateContext.getPrice() != null) {
                holding.setTotalBalance(updateContext.getPrice());
            }
        } else if ("DEPRECIATION".equalsIgnoreCase(revaluationType)) {
            BigDecimal depreciationRate = updateContext.getDepreciationRate() != null ? updateContext.getDepreciationRate() : new BigDecimal("0.1");
            int yearsUsed = updateContext.getYearsUsed() != null ? updateContext.getYearsUsed() : 1;
            
            BigDecimal depreciationFactor = BigDecimal.ONE.subtract(depreciationRate).pow(yearsUsed);
            BigDecimal newTotalBalance = holding.getTotalBalance().multiply(depreciationFactor);
            holding.setTotalBalance(newTotalBalance);
        } else if (updateContext.getPrice() != null) {
            holding.setTotalBalance(updateContext.getPrice());
        }
        
        holding.setValuationMode("MANUAL");
        holding.setAvailableBalance(holding.getTotalBalance().subtract(holding.getFrozenBalance()));
        return holding;
    }
}