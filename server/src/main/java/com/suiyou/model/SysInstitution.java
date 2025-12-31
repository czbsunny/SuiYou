package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sys_institution")
@Data
public class SysInstitution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "inst_code", nullable = false, unique = true, length = 32)
    private String instCode;
    
    @Column(name = "inst_name", nullable = false, length = 64)
    private String instName;
    
    @Column(name = "short_name", length = 32)
    private String shortName;
    
    @Column(name = "inst_type", nullable = false, length = 32)
    private String instType;
    
    @Column(name = "logo_url", length = 255)
    private String logoUrl;
    
    @Column(name = "theme_color", length = 16)
    private String themeColor;

    @Column(name = "index_letter", length = 1)
    private String indexLetter;
    
    @Column(name = "is_hot", nullable = false, columnDefinition = "tinyint(1) default 0")
    private Boolean isHot = false;
    
    @Column(name = "sort_order", nullable = false, columnDefinition = "int default 0")
    private Integer sortOrder = 0;
}