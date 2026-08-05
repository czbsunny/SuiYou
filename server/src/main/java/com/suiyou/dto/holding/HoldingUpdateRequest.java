package com.suiyou.dto.holding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingUpdateRequest {
    private String name;
    private BigDecimal qty;
    private BigDecimal price;
    private BigDecimal costBasis;
    private BigDecimal realizedPnl;
    private String side;
    private String status;
    private String holdingType;
    private String extraAttributes;
}