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

        BigDecimal newQty = holding.getQty().subtract(repayAmount);
        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            newQty = BigDecimal.ZERO;
        }

        holding.setQty(newQty);
        return holding;
    }
}