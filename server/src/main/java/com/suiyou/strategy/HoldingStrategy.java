package com.suiyou.strategy;

import com.suiyou.dto.holding.HoldingCreateRequest;
import com.suiyou.dto.holding.HoldingUpdateRequest;
import com.suiyou.model.Holding;

import java.math.BigDecimal;

public interface HoldingStrategy {
    String getStrategyType();

    Holding createHolding(HoldingCreateRequest request);

    Holding updateBasicInfo(Holding holding, HoldingUpdateRequest request);

    Holding updatePrice(Holding holding, BigDecimal newPrice);
}