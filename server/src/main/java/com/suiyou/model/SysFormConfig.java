package com.suiyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sys_form_config")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysFormConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "biz_type", nullable = false, length = 20)
    private String bizType;
    
    @Column(name = "category_code", nullable = false, length = 50)
    private String categoryCode;
    
    @Column(name = "field_config", nullable = false, columnDefinition = "TEXT")
    private String fieldConfig;
    
    @Column(name = "version", nullable = false)
    private Long version;
    
    @Column(name = "is_deleted", nullable = false, columnDefinition = "tinyint(1) default 0")
    private Boolean isDeleted;
}