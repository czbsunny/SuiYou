package com.suiyou.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateContext {
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal dailyYield;
    private BigDecimal annualRate;
    private BigDecimal inflowAmount;
    private BigDecimal repayAmount;
    private Integer days;
    private LocalDate updateDate;
    private String revaluationType;
    private BigDecimal depreciationRate;
    private Integer yearsUsed;
    private BigDecimal principalPart;
    private BigDecimal interestPart;
}