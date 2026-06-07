package com.suiyou.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum AssetType {

    CURRENT("活期"),

    CURRENT_PLUS("活期理财"),

    CASH("现金"),

    XIAOHEBAO("小荷包"),

    WALLET("钱包"),

    TIME_DEPOSIT("定期存款"),

    FUND("基金"),

    STOCK("股票"),

    BANK_PRODUCT("银行理财"),

    GOLD_ACCUMULATION("积存金"),

    PHYSICAL_GOLD("实物黄金"),

    PORTFOLIO("投顾组合"),

    PRIVATE_FUND("私募基金"),

    SAVING_INSURANCE("储蓄型保险"),

    CRYPTO("加密货币"),

    REAL_ESTATE("房产"),

    VEHICLE("车辆"),

    JEWELRY("名表/珠宝/奢品"),

    HOUSING_FUND("住房公积金"),

    MEDICAL_INSURANCE("医保个人账户"),

    PENSION_ACCOUNT("社保养老账户"),

    PRIVATE_PENSION("个人养老金"),

    RECEIVABLE("借出款/应收"),

    MORTGAGE("房贷"),

    CAR_LOAN("车贷"),

    CREDIT_CARD("信用卡"),

    INTERNET_CREDIT("互金贷"),

    CONSUMER_LOAN("银行消费贷"),

    PAYABLE("应付款/欠款"),

    OTHER("其他");

    private final String description;

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
}
