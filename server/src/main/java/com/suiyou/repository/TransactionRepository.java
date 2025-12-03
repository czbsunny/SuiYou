package com.suiyou.repository;

import com.suiyou.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 按家庭和时间范围查询流水
    List<Transaction> findByFamilyIdAndTransTimeBetween(Long familyId, LocalDateTime startDate, LocalDateTime endDate);
    
    // 按用户和时间范围查询流水
    List<Transaction> findByUserIdAndTransTimeBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // 按源账户查询流水
    List<Transaction> findBySourceAccountId(Long sourceAccountId);
    
    // 按目标账户查询流水
    List<Transaction> findByTargetAccountId(Long targetAccountId);
    
    // 按家庭、类型和时间范围查询流水
    List<Transaction> findByFamilyIdAndTypeAndTransTimeBetween(Long familyId, String type, LocalDateTime startDate, LocalDateTime endDate);
}
