package com.suiyou.dto.account;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 创建账户请求DTO
 */
@Data
public class CreateAccountDTO {
    
    @NotBlank(message = "账户名称不能为空")
    private String name;
    
    @NotNull(message = "一级分类不能为空")
    private String groupType; // ASSET / LIABILITY
    
    @NotNull(message = "二级分类不能为空")
    private String category; // LIQUID, INVEST...
    
    @NotNull(message = "子分类不能为空")
    private String subCategory; // BANK_CARD, STOCK...
    
    private String institution; // 机构代码
    
    @NotNull(message = "余额不能为空")
    private BigDecimal balance;
    
    private String currency = "CNY"; // 默认值
    
    private String visibleScope = "PRIVATE"; // 默认值
    
    // 扩展属性，用于接收前端传来的动态JSON
    private Map<String, Object> attributes;
}