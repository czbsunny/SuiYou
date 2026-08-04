package com.suiyou.strategy.update;

import com.suiyou.model.Holding;
import com.suiyou.strategy.HoldingUpdateStrategy;
import com.suiyou.strategy.UpdateContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NavUpdateStrategy implements HoldingUpdateStrategy {

    @Override
    public String getStrategyType() {
        return "NAV";
    }

    @Override
    public Holding update(Holding holding, UpdateContext updateContext) {
        BigDecimal price = updateContext.getPrice() != null ? updateContext.getPrice() : BigDecimal.ZERO;
        BigDecimal quantity = updateContext.getQuantity() != null ? updateContext.getQuantity() : BigDecimal.ONE;
        
        BigDecimal newTotalBalance = price.multiply(quantity);
        holding.setTotalBalance(newTotalBalance);
        holding.setValuationMode("CALCULATED");
        holding.setAvailableBalance(holding.getTotalBalance().subtract(holding.getFrozenBalance()));
        return holding;
    }
}