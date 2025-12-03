package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sys_asset_categories")
@Data
public class SysAssetCategories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(name = "parent_code", length = 50)
    private String parentCode;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    
    @Column(name = "icon_url", length = 255)
    private String iconUrl;
    
    @Column(name = "sort_order", nullable = false, columnDefinition = "int default 0")
    private Integer sortOrder = 0;
    
    @Column(name = "color", length = 20)
    private String color;
    
    @Column(name = "group_type", nullable = false, length = 20)
    private String groupType;
    
    @Column(name = "is_system", columnDefinition = "tinyint(1) default 1")
    private Boolean isSystem = true;
}
