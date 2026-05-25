package com.suiyou.dto.account;

import lombok.Data;
import com.suiyou.model.AccountModule;
import java.time.LocalDateTime;

@Data
public class AccountModuleRespDTO {
    private Long id;
    private Long accountId;
    private String assetType;
    private String assetTypeDescription;
    private String moduleName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static AccountModuleRespDTO fromEntity(AccountModule module) {
        AccountModuleRespDTO dto = new AccountModuleRespDTO();
        dto.setId(module.getId());
        dto.setAccountId(module.getAccount().getId());
        dto.setAssetType(module.getAssetType().name());
        dto.setAssetTypeDescription(module.getAssetType().getDescription());
        dto.setModuleName(module.getModuleName());
        dto.setStatus(module.getStatus());
        dto.setCreatedAt(module.getCreatedAt());
        dto.setUpdatedAt(module.getUpdatedAt());
        return dto;
    }
}