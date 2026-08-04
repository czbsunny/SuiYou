package com.suiyou.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "invitation", uniqueConstraints = {
        @UniqueConstraint(name = "uk_invitation_credential", columnNames = "credential")
})
@Data
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "inviter_id", nullable = false)
    private Long inviterId;

    @Column(name = "invitation_type", nullable = false, length = 20,
            columnDefinition = "varchar(20) default 'CODE'")
    private String invitationType = "CODE";

    @Column(name = "credential", nullable = false, unique = true, length = 6,
            columnDefinition = "varchar(6) CHARACTER SET ascii COLLATE ascii_bin")
    private String credential;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (invitationType == null) {
            invitationType = "CODE";
        }
        createdAt = LocalDateTime.now();
    }
}
