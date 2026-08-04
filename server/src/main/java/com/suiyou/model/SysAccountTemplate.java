package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sys_account_template")
@Data
public class SysAccountTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "inst_code", nullable = false, length = 32)
    private String instCode;
    
    @Column(name = "account_type", nullable = false, length = 32)
    private String accountType;
    
    @Column(name = "asset_type", nullable = false, length = 32)
    private String assetType;

    @Column(name = "asset_name", length = 100)
    private String assetName;
    
    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(nullable = false, columnDefinition = "tinyint(1) default 0")
    private Boolean canPay = false;
    
    @Column(name = "is_required", nullable = false, columnDefinition = "tinyint(1) default 0")
    private Boolean isRequired = false;
    
    @Column(name = "is_enabled", nullable = false, columnDefinition = "tinyint(1) default 1")
    private Boolean isEnabled = true;
    
    @Column(name = "sort_order", nullable = false, columnDefinition = "int default 0")
    private Integer sortOrder = 0;
}