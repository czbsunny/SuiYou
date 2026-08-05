package com.suiyou.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum HoldingType {

    CASH("CASH", "现金", "现金/活期存款", 1),

    DEPOSIT("DEPOSIT", "定存", "定期存款", 2),

    BANK_PRODUCT("BANK_PRODUCT", "银行理财", "银行理财产品", 3),

    STOCK("STOCK", "股票", "股票持仓", 4),

    FUND("FUND", "基金", "基金持仓", 5),

    FUTURES("FUTURES", "期货", "期货合约", 6),

    OPTION("OPTION", "期权", "期权合约", 7),

    FOREX("FOREX", "外汇", "外汇持仓", 8),

    GOLD("GOLD", "黄金", "贵金属", 9),

    LOAN("LOAN", "贷款", "贷款持仓", 10),

    CREDIT("CREDIT", "信用卡", "信用卡负债", 11),

    INSURANCE("INSURANCE", "保险", "保险持仓", 12);

    private final String code;
    private final String name;
    private final String desc;
    private final int order;

    public static HoldingType ofCode(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static List<HoldingType> all() {
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(HoldingType::getOrder))
                .collect(Collectors.toList());
    }

    public static boolean isValid(String code) {
        return ofCode(code) != null;
    }
}