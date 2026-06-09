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

    CURRENT("CURRENT", "活期", 1, "/static/assets/modules/current.png", true),

    CURRENT_PLUS("CURRENT_PLUS", "活期+", 2, "/static/assets/modules/current-plus.png", true),

    BANK_PRODUCT("BANK_PRODUCT", "银行理财", 3, "/static/assets/modules/bank-product.png", false),

    TIME_DEPOSIT("TIME_DEPOSIT", "定存", 4, "/static/assets/modules/time-deposit.png", false),

    FUND("FUND", "基金", 5, "/static/assets/modules/fund.png", false),

    STOCK("STOCK", "股票", 6, "/static/assets/modules/stock.png", false),

    GOLD("GOLD", "黄金", 7, "/static/assets/modules/gold.png", false),

    LOAN("LOAN", "贷款", 8, "/static/assets/modules/loan.png", false),

    CREDIT("CREDIT", "信用卡", 9, "/static/assets/modules/credit.png", true),

    INSURANCE("INSURANCE", "保险", 10, "/static/assets/modules/insurance.png", false);  

    private final String code;
    private final String name;
    private final int order;
    private final String iconUrl;
    private final boolean canPay;

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