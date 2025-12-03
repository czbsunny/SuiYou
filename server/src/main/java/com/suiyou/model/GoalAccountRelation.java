package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_account_relations", uniqueConstraints = {
    @UniqueConstraint(name = "uk_goal_account", columnNames = {"goalId", "accountId"})
})
@Data
public class GoalAccountRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "goal_id", nullable = false)
    private Long goalId;
    
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    // 外键关系
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "goal_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Goal goal;
    
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "account_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Account account;
}
