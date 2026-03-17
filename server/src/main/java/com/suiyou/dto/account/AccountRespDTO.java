package com.suiyou.dto.account;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.suiyou.dto.asset.AssetRespDTO;
import com.suiyou.model.Account;

@Data
public class AccountRespDTO {
    private Long id;
    private Long ownerId;
    private Long familyId;
    private String institution;
    private String institutionIdentifier;
    private String accountName;
    private Integer status;
    private String visibleScope;
    private Boolean includeInNetWorth;
    private String themeColor;
    private Integer sortOrder;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<AssetRespDTO> assets;
    
    public static AccountRespDTO fromEntity(Account account) {
        AccountRespDTO dto = new AccountRespDTO();
        dto.setId(account.getId());
        dto.setOwnerId(account.getOwnerId());
        dto.setFamilyId(account.getFamilyId());
        dto.setInstitution(account.getInstitution());
        dto.setInstitutionIdentifier(account.getInstitutionIdentifier());
        dto.setAccountName(account.getAccountName());
        dto.setStatus(account.getStatus());
        dto.setVisibleScope(account.getVisibleScope());
        dto.setIncludeInNetWorth(account.getIncludeInNetWorth());
        dto.setThemeColor(account.getThemeColor());
        dto.setSortOrder(account.getSortOrder());
        dto.setDeleted(account.getDeleted());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }
}