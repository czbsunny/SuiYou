package com.suiyou.dto.asset;

import java.math.BigDecimal;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建资产请求DTO
 */
@Data
public class CreateAssetDTO {
    @NotBlank(message = "资产名称不能为空")
    private String name;
    
    @NotNull(message = "一级分类不能为空")
    private String groupType; // ASSET / LIABILITY
    
    @NotNull(message = "二级分类不能为空")
    private String category; // LIQUID, INVEST...
    
    @NotNull(message = "子分类不能为空")
    private String subCategory; // BANK_CARD, STOCK...
        
    @NotNull(message = "余额不能为空")
    private BigDecimal balance;
    
    private String currency = "CNY"; // 默认值
    
    private String visibleScope = "PRIVATE"; // 默认值
    
    @NotNull(message = "是否包含在净值计算中不能为空")
    private Boolean includeInNetWorth;
    
    // 扩展属性，用于接收前端传来的动态JSON
    private Map<String, Object> attributes;

    private String institution; // 机构代码

    private String institutionIdentifier; // 机构唯一标识
}
