package com.suiyou.repository;

import com.suiyou.model.FamilyDailySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyDailySnapshotRepository extends JpaRepository<FamilyDailySnapshot, Long> {
    Optional<FamilyDailySnapshot> findByFamilyIdAndDate(Long familyId, LocalDate date);
    List<FamilyDailySnapshot> findByFamilyIdAndDateBetween(Long familyId, LocalDate startDate, LocalDate endDate);
    void deleteByFamilyIdAndDate(Long familyId, LocalDate date);
}
