
package com.suiyou.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionTypeRespDTO {
    private Long id;
    private String typeCode;
    private String typeName;
    private String description;
    private Integer sortOrder;
    private String iconUrl;
    private String themeColor;
}
