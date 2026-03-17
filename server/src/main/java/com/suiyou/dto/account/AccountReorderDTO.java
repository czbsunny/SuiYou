package com.suiyou.dto.account;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 账户排序DTO
 */
@Data
public class AccountReorderDTO {
    /**
     * 账户ID列表
     */
    @NotNull(message = "账户ID列表不能为空")
    private List<Long> accountIds;
}
