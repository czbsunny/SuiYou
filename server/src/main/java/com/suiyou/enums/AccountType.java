package com.suiyou.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum AccountType {

    DEBIT_CARD("DEBIT_CARD", "借记卡", 1,
            List.of(InstType.BANK, InstType.INTERNET_BANK, InstType.FOREIGN_BANK)),

    CREDIT_CARD("CREDIT_CARD", "信用卡", 2,
            List.of(InstType.BANK, InstType.INTERNET_BANK, InstType.FOREIGN_BANK)),

    PERSONAL_PENSION("PERSONAL_PENSION", "个人养老金", 3,
            List.of(InstType.BANK)),

    SECURITY("SECURITY", "证券", 4,
            List.of(InstType.SECURITY)),

    INSURANCE("INSURANCE", "保险", 5,
            List.of(InstType.INSURANCE)),

    PAYMENT("PAYMENT", "支付", 6,
            List.of(InstType.PAYMENT)),

    FINTECH("FINTECH", "金融科技", 7,
            List.of(InstType.FINTECH)),

    FUND_PLATFORM("FUND_PLATFORM", "基金平台", 8,
            List.of(InstType.FUND_PLATFORM));

    private final String code;
    private final String name;
    private final int order;
    private final List<InstType> supportedInstTypes;

    public static AccountType ofCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static List<AccountType> all() {
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(AccountType::getOrder))
                .collect(Collectors.toList());
    }

    public static List<AccountType> getByInstitutionType(String typeCode) {
        InstType instType = InstType.ofCode(typeCode);
        return Arrays.stream(values())
                .filter(e -> e.supportedInstTypes.contains(instType))
                .collect(Collectors.toList());
    }

    public static boolean isValid(String name) {
        try {
            valueOf(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean supportsInstitution(InstType instType) {
        return supportedInstTypes.contains(instType);
    }
}
