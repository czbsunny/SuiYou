package com.suiyou.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum InstType {

    BANK("BANK", "银行", "各类银行机构", 1, "/static/inst-type/bank.png"),

    INTERNET_BANK("INTERNET_BANK", "互联网银行", "互联网银行机构", 2, "/static/inst-type/internet-bank.png"),

    FOREIGN_BANK("FOREIGN_BANK", "外资银行", "外资银行机构", 3, "/static/inst-type/foreign-bank.png"),

    SECURITY("SECURITY", "证券", "证券公司", 4, "/static/inst-type/security.png"),

    INSURANCE("INSURANCE", "保险", "保险公司", 5, "/static/inst-type/insurance.png"),

    PAYMENT("PAYMENT", "支付平台", "支付平台", 6, "/static/inst-type/payment.png"),

    FINTECH("FINTECH", "金融科技", "金融科技公司", 7, "/static/inst-type/fintech.png"),

    FUND_PLATFORM("FUND_PLATFORM", "基金平台", "基金平台", 8, "/static/inst-type/fund.png");

    private final String code;
    private final String name;
    private final String desc;
    private final int order;
    private final String iconUrl;

    public static InstType ofCode(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static List<InstType> all() {
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(InstType::getOrder))
                .collect(Collectors.toList());
    }

    public static boolean isValid(String code) {
        return ofCode(code) != null;
    }
}
