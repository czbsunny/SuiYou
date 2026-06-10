package com.suiyou.dto.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetUpdateRequest {
    private String name;
    private String category;
    private String subCategory;
    private String groupType;
    private String currency;
    private Boolean includeInNetWorth;
    private String valuationMode;
    private String attributes;
}