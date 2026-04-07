package com.suiyou.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePortfolioHodingDTO {
    @NotBlank(message = "持仓ID不能为空")
    private Long id;
    
    private Double quantity;
    
    private Double cost;
    
    private Double amount;
    
    private Double returnValue;
}
