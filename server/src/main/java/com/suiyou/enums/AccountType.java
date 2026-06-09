package com.suiyou.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import com.suiyou.enums.InstType;

@Slf4j
@Getter
@AllArgsConstructor
public enum AccountType {

    DEBIT_CARD("DEBIT_CARD", "借记卡", 1, InstType.BANK),

    CREDIT_CARD("CREDIT_CARD", "信用卡", 2, InstType.BANK),

    PERSONAL_PENSION("PERSONAL_PENSION", "个人养老金", 3, InstType.BANK),

    SECURITY("SECURITY", "证券", 4, InstType.SECURITY),

    INSURANCE("INSURANCE", "保险", 5, InstType.INSURANCE),

    FINTECH("FINTECH", "互金平台", 6, InstType.FINTECH),

    INDIVIDUAL("INDIVIDUAL", "独立账户", 7, InstType.INDIVIDUAL);

    private static final Set<String> PERSONAL_PENSION_SUPPORTED_BANKS = new HashSet<>(Arrays.asList(
            "ICBC", "CCB", "ABC", "BOC", "BOCOM", "PSBC",
            "CMB", "CNCB", "CEB", "HXB", "CMBC", "SPDB", "CIB", "PAB", "CGB",
            "CZB", "HFB", "CBHB", "BOB", "BOS", "JSB", "NBCB", "NJCB"
    ));

    private final String code;
    private final String name;
    private final int order;
    private final InstType instType;

    public static Collection<AccountType> all() {
        return Arrays.asList(values());
    }

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

    public static boolean isValid(String name) {
        try {
            valueOf(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean supportsInstitution(String instCode, String instType) {
        if (instCode == null) {
            return false;
        }

        if (this == PERSONAL_PENSION) {
            return PERSONAL_PENSION_SUPPORTED_BANKS.contains(instCode);
        }
        
        InstType instTypeEnum = InstType.ofCode(instType);
        return this.instType.equals(instTypeEnum);
    }
}
