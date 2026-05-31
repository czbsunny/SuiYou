
package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sys_institution_type")
@Data
public class SysInstitutionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_code", nullable = false, unique = true, length = 32)
    private String typeCode;

    @Column(name = "type_name", nullable = false, length = 64)
    private String typeName;

    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "sort_order", nullable = false, columnDefinition = "int default 0")
    private Integer sortOrder = 0;

    @Column(name = "icon_url", length = 256)
    private String iconUrl;

    @Column(name = "theme_color", length = 16)
    private String themeColor;
}

