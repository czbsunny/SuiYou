package com.suiyou.strategy;

import com.suiyou.model.Holding;

public interface HoldingUpdateStrategy {
    String getStrategyType();
    
    Holding update(Holding holding, UpdateContext updateContext);
}