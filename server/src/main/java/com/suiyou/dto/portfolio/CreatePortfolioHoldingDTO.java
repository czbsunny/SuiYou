package com.suiyou.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePortfolioHoldingDTO {
    @NotBlank(message = "资产代码不能为空")
    @Size(max = 50, message = "资产代码不能超过50个字符")
    private String symbol;
    
    @Size(max = 100, message = "资产名称不能超过100个字符")
    private String name;
    
    @NotNull(message = "持有数量不能为空")
    @Positive(message = "持有数量必须大于0")
    private Double quantity;
    
    @NotNull(message = "单位成本不能为空")
    private Double cost;
    
    private Double amount;
    
    private Double returnValue;
}