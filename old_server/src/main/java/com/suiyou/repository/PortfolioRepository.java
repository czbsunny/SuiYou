package com.suiyou.repository;

import com.suiyou.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByAssetId(Long assetId);
    List<Portfolio> findByUserId(Long userId);
}