package com.suiyou.dto.asset;

import java.math.BigDecimal;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAssetDTO {
    private Long id;

    private String name;

    private String currency = "CNY";
    
    @NotNull(message = "是否包含在净值计算中不能为空")
    private Boolean includeInNetWorth;
    
    private Map<String, Object> attributes;

    private Long accountId; // 关联的账户ID

    private String institution; // 机构代码

    private String institutionIdentifier; // 机构唯一标识
}
