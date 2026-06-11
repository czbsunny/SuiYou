package com.suiyou.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AssetUpdateStrategyFactory {
    private final Map<String, AssetUpdateStrategy> strategyMap;

    @Autowired
    public AssetUpdateStrategyFactory(List<AssetUpdateStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(AssetUpdateStrategy::getStrategyType, Function.identity()));
    }

    public AssetUpdateStrategy getStrategy(String strategyType) {
        return strategyMap.get(strategyType);
    }
}