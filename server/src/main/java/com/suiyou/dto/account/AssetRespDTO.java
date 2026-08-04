package com.suiyou.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetRespDTO {
    private Long id;
    private String assetType;
    private String assetName;
    private String iconUrl;
    private String bgColor;
    private Integer canPay;
    private Integer sortOrder;
}