package com.suiyou.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ModuleType {

    CURRENT("CURRENT", "活期", 1, "/static/assets/modules/current.png"),

    CURRENT_PLUS("CURRENT_PLUS", "活期+", 2, "/static/assets/modules/current-plus.png"),

    BANK_PRODUCT("BANK_PRODUCT", "银行理财", 3, "/static/assets/modules/bank-product.png"),

    TIME_DEPOSIT("TIME_DEPOSIT", "定存", 4, "/static/assets/modules/time-deposit.png"),

    FUND("FUND", "基金", 5, "/static/assets/modules/fund.png"),

    STOCK("STOCK", "股票", 6, "/static/assets/modules/stock.png"),

    GOLD("GOLD", "黄金", 7, "/static/assets/modules/gold.png"),

    LOAN("LOAN", "贷款", 8, "/static/assets/modules/loan.png"),

    CREDIT("CREDIT", "信用卡", 9, "/static/assets/modules/credit.png"),

    INSURANCE("INSURANCE", "保险", 10, "/static/assets/modules/insurance.png");

    private final String code;
    private final String name;
    private final int order;
    private final String iconUrl;

    public static ModuleType ofCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static List<ModuleType> all() {
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(ModuleType::getOrder))
                .collect(Collectors.toList());
    }

    public static boolean isValid(String code) {
        return ofCode(code) != null;
    }
}