package com.suiyou.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Formula;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long familyId;
    
    @Column(nullable = false)
    private Long ownerId;
    
    @Column(nullable = false, columnDefinition = "varchar(20) default 'PRIVATE'")
    private String visibleScope = "PRIVATE";

    
    private String institution;
    
    @Column(nullable = false)
    private String institutionIdentifier;
        
    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Integer status = 1;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}