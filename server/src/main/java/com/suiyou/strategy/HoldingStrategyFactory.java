package com.suiyou.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class HoldingStrategyFactory {
    private final Map<String, HoldingStrategy> strategyMap;

    @Autowired
    public HoldingStrategyFactory(List<HoldingStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(HoldingStrategy::getStrategyType, Function.identity()));
    }

    public HoldingStrategy getStrategy(String strategyType) {
        return strategyMap.getOrDefault(strategyType, strategyMap.get("DEFAULT"));
    }
}