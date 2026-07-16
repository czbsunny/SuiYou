package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;

// TODO: 处理冗余字段

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
    
    @Column(name = "module_type", nullable = false, length = 32)
    private String moduleType;

    @Column(name = "module_name", length = 100)
    private String moduleName;
    
    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(nullable = false, columnDefinition = "tinyint(1) default 0")
    private Boolean canPay = false;
    
    @Column(nullable = false, columnDefinition = "tinyint(1) default 0")
    private Boolean required = false;
    
    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Boolean enabled = true;
    
    @Column(name = "sort_order", nullable = false, columnDefinition = "int default 0")
    private Integer sortOrder = 0;
}