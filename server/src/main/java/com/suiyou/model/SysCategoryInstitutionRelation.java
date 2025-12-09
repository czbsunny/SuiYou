package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_category_institution_relations", uniqueConstraints = {
    @UniqueConstraint(name = "uk_category_institution", columnNames = {"category_id", "institution_id"})
})
@Data
public class SysCategoryInstitutionRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "category_id", nullable = false)
    private Long categoryId;
    
    @Column(name = "institution_id", nullable = false)
    private Long institutionId;
    
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    // 外键关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id", insertable = false, updatable = false)
    private SysAssetCategories sysAssetCategories;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", referencedColumnName = "id", insertable = false, updatable = false)
    private SysInstitution sysInstitution;
}