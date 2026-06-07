package com.suiyou.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum AccountType {

    DEBIT_CARD("借记卡", List.of("BANK", "INTERNET_BANK", "FOREIGN_BANK")),

    CREDIT_CARD("信用卡", List.of("BANK", "INTERNET_BANK", "FOREIGN_BANK")),

    PERSONAL_PENSION("个人养老金", List.of("BANK")),

    SECURITY("证券", List.of("SECURITY")),

    INSURANCE("保险", List.of("INSURANCE")),

    PAYMENT("支付", List.of("PAYMENT")),

    FINTECH("金融科技", List.of("FINTECH")),

    FUND_PLATFORM("基金平台", List.of("FUND_PLATFORM"));

    private final String description;
    private final List<String> supportedInstitutionTypes;

    public static List<AccountType> getByInstitutionType(String typeCode) {
        return Arrays.stream(values())
                .filter(e -> e.supportedInstitutionTypes.contains(typeCode))
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

    public boolean supportsInstitution(String institutionType) {
        return supportedInstitutionTypes.contains(institutionType);
    }
}
