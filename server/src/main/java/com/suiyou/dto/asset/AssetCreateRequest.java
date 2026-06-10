package com.suiyou.dto.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetCreateRequest {
    private Long accountId;
    private String accountModuleId;
    private Long ownerId;
    private String groupType;
    private String category;
    private String subCategory;
    private String name;
    private BigDecimal totalBalance;
    private BigDecimal frozenBalance;
    private String currency;
    private Boolean includeInNetWorth;
    private String valuationMode;
    private Integer status;
    private String attributes;
}