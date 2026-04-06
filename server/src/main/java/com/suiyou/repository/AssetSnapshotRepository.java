package com.suiyou.repository;

import com.suiyou.model.AssetSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AssetSnapshotRepository extends JpaRepository<AssetSnapshot, Long> {
    
    @Query("SELECT a FROM AssetSnapshot a WHERE a.ownerId = :ownerId AND a.snapshotDate = :snapshotDate")
    AssetSnapshot findByOwnerIdAndSnapshotDate(@Param("ownerId") Long ownerId, @Param("snapshotDate") LocalDate snapshotDate);
    
    @Query("SELECT a FROM AssetSnapshot a WHERE a.ownerId = :ownerId AND a.snapshotDate BETWEEN :startDate AND :endDate ORDER BY a.snapshotDate ASC")
    List<AssetSnapshot> findByOwnerIdAndSnapshotDateBetween(@Param("ownerId") Long ownerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT DISTINCT a.snapshotDate FROM AssetSnapshot a WHERE a.ownerId = :ownerId ORDER BY a.snapshotDate DESC")
    List<LocalDate> findDistinctSnapshotDatesByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT a FROM AssetSnapshot a WHERE a.ownerId = :ownerId ORDER BY a.snapshotDate DESC")
    List<AssetSnapshot> findAllByOwnerIdOrderBySnapshotDateDesc(@Param("ownerId") Long ownerId);
    
    @Query("SELECT a FROM AssetSnapshot a WHERE a.ownerId = :ownerId ORDER BY a.snapshotDate ASC")
    List<AssetSnapshot> findAllByOwnerIdOrderBySnapshotDateAsc(@Param("ownerId") Long ownerId);
}