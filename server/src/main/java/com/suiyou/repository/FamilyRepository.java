package com.suiyou.repository;

import com.suiyou.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {
    Family findByCreatorId(Long creatorId);
}