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
    
    private Double quantity;
    
    private Double cost;
    
    private Double amount;
    
    private Double returnValue;
}