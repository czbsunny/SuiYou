package com.suiyou.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AssetStrategyFactory {
    private final Map<String, AssetStrategy> strategyMap;

    @Autowired
    public AssetStrategyFactory(List<AssetStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(AssetStrategy::getStrategyType, Function.identity()));
    }

    public AssetStrategy getStrategy(String strategyType) {
        return strategyMap.getOrDefault(strategyType, strategyMap.get("DEFAULT"));
    }
}