package com.suiyou.repository;

import com.suiyou.model.Transaction;
import com.suiyou.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT t FROM Transaction t WHERE t.sourceHoldingId = :holdingId OR t.targetHoldingId = :holdingId")
    Page<Transaction> findByHoldingId(@Param("holdingId") Long holdingId, Pageable pageable);

    Page<Transaction> findByFamilyIdAndTransTimeBetween(Long familyId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    Page<Transaction> findByFamilyIdAndType(Long familyId, TransactionType type, Pageable pageable);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.familyId = :familyId AND t.type = 'EXPENSE' AND t.transTime BETWEEN :start AND :end")
    BigDecimal sumExpense(@Param("familyId") Long familyId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}