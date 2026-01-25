package com.suiyou.dto.account;

import lombok.Data;

/**
 * 更新账户请求DTO
 */
@Data
public class UpdateAccountDTO {
    private Long accountId; // 账户ID

    private String accountName; // 账户名称
    
    private String institutionIdentifier; // 机构唯一标识

    private Boolean includeInNetWorth; // 是否包含在净值计算中
    
    private String themeColor; // 账户主题颜色
}
