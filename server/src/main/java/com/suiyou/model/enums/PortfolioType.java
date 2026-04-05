package com.suiyou.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PortfolioType {
    FUND("基金组合"),
    STOCK("股票组合");
    private final String desc;
}
