
package com.suiyou.dto.account;

import lombok.Data;

@Data
public class InstitutionTypeInitDTO {
    private String typeCode;
    private String typeName;
    private String description;
    private Integer sortOrder;
    private String iconUrl;
    private String themeColor;
}
