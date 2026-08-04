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
    private Long ownerId;
    private String groupType;
    private String category;
    private String subCategory;
    private String name;
    private BigDecimal totalBalance;
    private BigDecimal frozenBalance;
    private BigDecimal availableBalance;
    private String currency;
    private Boolean includeInNetWorth;
    private String valuationMode;
    private Integer status;
    private String attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}