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

    BANK("BANK", "银行", "各类银行机构", 1),

    SECURITY("SECURITY", "证券", "证券公司", 2),

    INSURANCE("INSURANCE", "保险", "保险公司", 3),

    FINTECH("FINTECH", "互金平台", "金融科技、支付、基金平台", 4),

    INDIVIDUAL("INDIVIDUAL", "独立账户", "独立账户分类", 5);

    private final String code;
    private final String name;
    private final String desc;
    private final int order;

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