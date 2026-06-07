package com.suiyou.dto.account;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量更新账户DTO（排序+归档+恢复）
 */
@Data
public class BatchUpdateAccountsDTO {
    /**
     * 活跃账户ID列表（按排序顺序）
     */
    @NotEmpty(message = "活跃账户ID列表不能为空")
    private List<Long> activeAccountIds;

    /**
     * 需要归档的账户ID列表
     */
    private List<Long> archivedAccountIds;
}
