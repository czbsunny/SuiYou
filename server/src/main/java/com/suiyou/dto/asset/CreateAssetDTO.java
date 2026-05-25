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
    
    @NotNull(message = "子分类不能为空")
    private String subCategory; // BANK_CARD, STOCK, FUND...
        
    @NotNull(message = "余额不能为空")
    private BigDecimal balance;
    
    private String currency = "CNY";
    
    @NotNull(message = "是否包含在净值计算中不能为空")
    private Boolean includeInNetWorth;
    
    private Map<String, Object> attributes;

    @NotNull(message = "资产模块ID不能为空")
    private Long moduleId;
}