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
    private String instCode;
    private String accountNo;
    private String accountType;
    private String accountName;
    private Integer status;
    private String visibleScope;
    private Boolean includeInNetWorth;
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
        dto.setInstCode(account.getInstCode());
        dto.setAccountNo(account.getAccountNo());
        dto.setAccountType(account.getAccountType());
        dto.setAccountName(account.getAccountName());
        dto.setStatus(account.getStatus());
        dto.setVisibleScope(account.getVisibleScope());
        dto.setIncludeInNetWorth(account.getIncludeInNetWorth());
        dto.setSortOrder(account.getSortOrder());
        dto.setDeleted(account.getDeleted());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }
}
