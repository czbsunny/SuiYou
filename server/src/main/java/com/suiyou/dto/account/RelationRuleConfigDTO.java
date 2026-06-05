package com.suiyou.dto.account;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@Data
public class RelationRuleConfigDTO {
    private Map<String, ModuleRuleDTO> typeRules;
    private Map<String, ModuleRuleDTO> instRules;
    private Map<String, List<String>> categoryRules;
    
    @Data
    public static class ModuleRuleDTO {
        private List<String> required;
        
        @JsonProperty("default")
        private List<String> defaultList;
        
        private List<String> optional;
    }
}