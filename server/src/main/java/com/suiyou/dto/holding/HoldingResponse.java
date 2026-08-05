package com.suiyou.dto.holding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingResponse {
    private Long id;
    private Long accountId;
    private Long assetId;
    private Long productId;
    private String name;
    private BigDecimal qty;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal costBasis;
    private BigDecimal realizedPnl;
    private String side;
    private String status;
    private String holdingType;
    private String extraAttributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}