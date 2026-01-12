package com.suiyou.dto.asset;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.suiyou.model.Account;
import com.suiyou.model.Asset;

@Data
public class AssetRespDTO {
    private Long id;
    private Long ownerId;
    private String groupType;
    private String category;
    private String subCategory;
    private String name;

    private String institution;
    private String institutionIdentifier;
    private String accountName;
    private Long accountId;

    private String attributes;
    private BigDecimal totalBalance;
    private BigDecimal frozenBalance;
    private BigDecimal availableBalance;
    private String currency;
    private Boolean includeInNetWorth;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AssetRespDTO fromEntity(Asset asset) {
        AssetRespDTO dto = new AssetRespDTO();
        dto.setInstitution(asset.getAccount().getInstitution());
        dto.setInstitutionIdentifier(asset.getAccount().getInstitutionIdentifier());
        dto.setAccountName(asset.getAccount().getAccountName());
        dto.setAccountId(asset.getAccount().getId());
        dto.setAttributes(asset.getAttributes());
        dto.setAvailableBalance(asset.getAvailableBalance());
        dto.setCategory(asset.getCategory());
        dto.setCreatedAt(asset.getCreatedAt());
        dto.setCurrency(asset.getCurrency());
        dto.setFrozenBalance(asset.getFrozenBalance());
        dto.setGroupType(asset.getGroupType());
        dto.setId(asset.getId());
        dto.setIncludeInNetWorth(asset.getIncludeInNetWorth());
        dto.setName(asset.getName());
        dto.setOwnerId(asset.getOwnerId());
        dto.setSubCategory(asset.getSubCategory());
        dto.setTotalBalance(asset.getTotalBalance());
        dto.setUpdatedAt(asset.getUpdatedAt());
        return dto;
    }
}
