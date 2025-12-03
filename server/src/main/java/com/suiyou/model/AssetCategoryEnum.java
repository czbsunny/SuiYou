package com.suiyou.model;

/**
 * 资产分类枚举类
 * code值对应sys_asset_categories表中的code字段
 */
public enum AssetCategoryEnum {
    // 资产类（ASSET）
    LIQUID("流动资产"),
    BANK_CARD("银行卡"),
    WEALTH_MANAGEMENT("理财产品"),
    STOCK("股票"),
    FUND("基金"),
    REAL_ESTATE("房产"),
    VEHICLE("车辆"),
    OTHER_ASSET("其他资产"),
    
    // 负债类（LIABILITY）
    DEBT("负债"),
    CREDIT_CARD("信用卡"),
    LOAN("贷款"),
    OTHER_DEBT("其他负债");
    
    private final String description;
    
    AssetCategoryEnum(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
