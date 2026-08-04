package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_family_member_family_user", columnNames = {"family_id", "user_id"})
})
@Data
public class FamilyMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'MEMBER'")
    private String role = "MEMBER";

    @Column(name = "is_primary", nullable = false, columnDefinition = "tinyint(1) default 0")
    private Boolean isPrimary = false;

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
