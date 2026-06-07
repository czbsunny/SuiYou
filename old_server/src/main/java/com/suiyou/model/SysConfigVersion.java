package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_config_version")
@Data
public class SysConfigVersion {
    @Id
    @Column(name = "module_key", nullable = false, length = 50)
    private String moduleKey;
    
    @Column(name = "version_hash", nullable = false, length = 32)
    private String versionHash;
    
    @Column(name = "updated_at", columnDefinition = "datetime default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}