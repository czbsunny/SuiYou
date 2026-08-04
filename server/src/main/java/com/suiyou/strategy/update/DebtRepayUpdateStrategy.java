package com.suiyou.strategy.update;

import com.suiyou.model.Holding;
import com.suiyou.strategy.HoldingUpdateStrategy;
import com.suiyou.strategy.UpdateContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DebtRepayUpdateStrategy implements HoldingUpdateStrategy {

    @Override
    public String getStrategyType() {
        return "DEBT_REPAY";
    }

    @Override
    public Holding update(Holding holding, UpdateContext updateContext) {
        BigDecimal repayAmount = updateContext.getRepayAmount() != null ? updateContext.getRepayAmount() : BigDecimal.ZERO;
        BigDecimal principalPart = updateContext.getPrincipalPart() != null ? updateContext.getPrincipalPart() : BigDecimal.ZERO;
        BigDecimal interestPart = updateContext.getInterestPart() != null ? updateContext.getInterestPart() : BigDecimal.ZERO;
        
        BigDecimal newTotalBalance = holding.getTotalBalance().subtract(repayAmount);
        if (newTotalBalance.compareTo(BigDecimal.ZERO) < 0) {
            newTotalBalance = BigDecimal.ZERO;
        }
        
        holding.setTotalBalance(newTotalBalance);
        holding.setValuationMode("CALCULATED");
        holding.setAvailableBalance(holding.getTotalBalance().subtract(holding.getFrozenBalance()));
        return holding;
    }
}