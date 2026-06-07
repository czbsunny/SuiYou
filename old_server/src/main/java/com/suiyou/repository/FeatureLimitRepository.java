package com.suiyou.repository;

import com.suiyou.model.FeatureLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureLimitRepository extends JpaRepository<FeatureLimit, Long> {
    FeatureLimit findByFeatureCodeAndPlanCode(String featureCode, String planCode);
}
