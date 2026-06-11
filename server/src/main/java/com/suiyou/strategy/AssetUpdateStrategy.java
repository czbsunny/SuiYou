package com.suiyou.strategy;

import com.suiyou.model.Asset;

public interface AssetUpdateStrategy {
    String getStrategyType();
    
    Asset update(Asset asset, UpdateContext updateContext);
}