package com.suiyou.dto.account;

import java.util.List;

import lombok.Data;

/**
 * 同步账户DTO
 * 包含活跃账户的排序列表和归档账户的列表
 */
@Data
public class SyncAccountDTO {
    /**
     * 活跃账户的排序列表（按sortOrder升序）
     */
    private List<Long> activeAccountIds;
    
    /**
     * 归档账户的列表
     */
    private List<Long> archivedAccountIds;
}
