package com.suiyou.strategy.update;

import com.suiyou.model.Asset;
import com.suiyou.strategy.AssetUpdateStrategy;
import com.suiyou.strategy.UpdateContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DebtRepayUpdateStrategy implements AssetUpdateStrategy {

    @Override
    public String getStrategyType() {
        return "DEBT_REPAY";
    }

    @Override
    public Asset update(Asset asset, UpdateContext updateContext) {
        BigDecimal repayAmount = updateContext.getRepayAmount() != null ? updateContext.getRepayAmount() : BigDecimal.ZERO;
        BigDecimal principalPart = updateContext.getPrincipalPart() != null ? updateContext.getPrincipalPart() : BigDecimal.ZERO;
        BigDecimal interestPart = updateContext.getInterestPart() != null ? updateContext.getInterestPart() : BigDecimal.ZERO;
        
        BigDecimal newTotalBalance = asset.getTotalBalance().subtract(repayAmount);
        if (newTotalBalance.compareTo(BigDecimal.ZERO) < 0) {
            newTotalBalance = BigDecimal.ZERO;
        }
        
        asset.setTotalBalance(newTotalBalance);
        asset.setValuationMode("CALCULATED");
        asset.setAvailableBalance(asset.getTotalBalance().subtract(asset.getFrozenBalance()));
        return asset;
    }
}