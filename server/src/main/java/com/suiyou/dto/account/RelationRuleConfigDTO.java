package com.suiyou.dto.account;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RelationRuleConfigDTO {
    // 对应 JSON 中的 "defaultRules": { "BANK": [...] }
    private Map<String, List<String>> defaultRules;
    
    // 对应 JSON 中的 "specialCases": [ ... ]
    private List<SpecialCaseDTO> specialCases;

    @Data
    public static class SpecialCaseDTO {
        private String instCode;
        private List<String> categories;
    }
}