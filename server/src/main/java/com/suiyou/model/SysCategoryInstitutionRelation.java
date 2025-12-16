package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
    name = "sys_category_institution_relation", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category_code", "inst_code"}) // 防止重复关联
    },
    indexes = {
        @Index(name = "idx_cat_inst_cat", columnList = "category_code"),
        @Index(name = "idx_cat_inst_inst", columnList = "inst_code")
    }
)
public class SysCategoryInstitutionRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 修改点1：这里存 String 类型的 Code
    @Column(name = "category_code", nullable = false, length = 32)
    private String categoryCode;

    // 修改点2：这里存 String 类型的 InstCode
    @Column(name = "inst_code", nullable = false, length = 32)
    private String instCode;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // --- 若需要级联查询，JPA 配置如下 ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code", referencedColumnName = "category_code", insertable = false, updatable = false)
    private SysAssetCategory sysAssetCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inst_code", referencedColumnName = "inst_code", insertable = false, updatable = false)
    private SysInstitution sysInstitution;
}
