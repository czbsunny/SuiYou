package com.suiyou.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum AssetType {

    CURRENT("CURRENT", "活期", 1),

    CURRENT_PLUS("CURRENT_PLUS", "活期理财", 2),

    CASH("CASH", "现金", 3),

    XIAOHEBAO("XIAOHEBAO", "小荷包", 4),

    WALLET("WALLET", "钱包", 5),

    TIME_DEPOSIT("TIME_DEPOSIT", "定期存款", 6),

    FUND("FUND", "基金", 7),

    STOCK("STOCK", "股票", 8),

    BANK_PRODUCT("BANK_PRODUCT", "银行理财", 9),

    GOLD_ACCUMULATION("GOLD_ACCUMULATION", "积存金", 10),

    PHYSICAL_GOLD("PHYSICAL_GOLD", "实物黄金", 11),

    PORTFOLIO("PORTFOLIO", "投顾组合", 12),

    PRIVATE_FUND("PRIVATE_FUND", "私募基金", 13),

    SAVING_INSURANCE("SAVING_INSURANCE", "储蓄型保险", 14),

    CRYPTO("CRYPTO", "加密货币", 15),

    REAL_ESTATE("REAL_ESTATE", "房产", 16),

    VEHICLE("VEHICLE", "车辆", 17),

    JEWELRY("JEWELRY", "名表/珠宝/奢品", 18),

    HOUSING_FUND("HOUSING_FUND", "住房公积金", 19),

    MEDICAL_INSURANCE("MEDICAL_INSURANCE", "医保个人账户", 20),

    PENSION_ACCOUNT("PENSION_ACCOUNT", "社保养老账户", 21),

    PRIVATE_PENSION("PRIVATE_PENSION", "个人养老金", 22),

    RECEIVABLE("RECEIVABLE", "借出款/应收", 23),

    MORTGAGE("MORTGAGE", "房贷", 24),

    CAR_LOAN("CAR_LOAN", "车贷", 25),

    CREDIT_CARD("CREDIT_CARD", "信用卡", 26),

    INTERNET_CREDIT("INTERNET_CREDIT", "互金贷", 27),

    CONSUMER_LOAN("CONSUMER_LOAN", "银行消费贷", 28),

    PAYABLE("PAYABLE", "应付款/欠款", 29),

    OTHER("OTHER", "其他", 30);

    private final String code;
    private final String name;
    private final int order;

    private static final Map<String, AssetType> CATEGORY_CODE_TO_ASSET_TYPE = new HashMap<>();

    static {
        CATEGORY_CODE_TO_ASSET_TYPE.put("DEBIT_CARD", CURRENT);
        CATEGORY_CODE_TO_ASSET_TYPE.put("CHANGE", CURRENT);
        CATEGORY_CODE_TO_ASSET_TYPE.put("BALANCE", CURRENT);
        CATEGORY_CODE_TO_ASSET_TYPE.put("CUSTOM_LIQUID", CURRENT);
        CATEGORY_CODE_TO_ASSET_TYPE.put("YUEBAO", CURRENT_PLUS);
        CATEGORY_CODE_TO_ASSET_TYPE.put("CASH_PLUS", CURRENT_PLUS);
        CATEGORY_CODE_TO_ASSET_TYPE.put("CUSTOM_INVEST", OTHER);
        CATEGORY_CODE_TO_ASSET_TYPE.put("CUSTOM_FIXED", OTHER);
        CATEGORY_CODE_TO_ASSET_TYPE.put("CUSTOM_OTHER", OTHER);
        CATEGORY_CODE_TO_ASSET_TYPE.put("XIAOHEBAO", XIAOHEBAO);
    }

    public static AssetType ofCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static List<AssetType> all() {
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(AssetType::getOrder))
                .collect(Collectors.toList());
    }

    public static AssetType fromCategoryCode(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return null;
        }
        AssetType mapped = CATEGORY_CODE_TO_ASSET_TYPE.get(categoryCode);
        if (mapped != null) {
            return mapped;
        }
        try {
            return AssetType.valueOf(categoryCode);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无法识别的资产类型代码: " + categoryCode);
        }
    }

    public static boolean isValid(String code) {
        return ofCode(code) != null;
    }
}
