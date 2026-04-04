package com.suiyou.dto.portfolio;

import lombok.Data;

@Data
public class CreatePortfolioHoldingDTO {
    private String symbol;
    private String name;
    private Double quantity;
    private Double cost;
}