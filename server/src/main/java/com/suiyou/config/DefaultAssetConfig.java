package com.suiyou.config;

import java.util.HashMap;
import java.util.Map;

public class DefaultAssetConfig {
    private static final Map<String, String> TYPE_TO_CATEGORY_MAP = new HashMap<>();
    private static final Map<String, String> CODE_TO_CATEGORY_MAP = new HashMap<>();
    
    static {
        TYPE_TO_CATEGORY_MAP.put("BANK", "DEBIT_CARD");
    }
    
    static {
        CODE_TO_CATEGORY_MAP.put("WECHAT", "CHANGE");
        CODE_TO_CATEGORY_MAP.put("ALIPAY", "BALANCE");
    }

    public static String getBaseAssetType(String instType, String instCode) {
        if (CODE_TO_CATEGORY_MAP.containsKey(instCode)) {
            return CODE_TO_CATEGORY_MAP.get(instCode);
        }
        
        if (TYPE_TO_CATEGORY_MAP.containsKey(instType)) {
            return TYPE_TO_CATEGORY_MAP.get(instType);
        }

        return "";
    }
}
