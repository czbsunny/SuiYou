package com.suiyou.repository;

import com.suiyou.model.UserFeatureUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFeatureUsageRepository extends JpaRepository<UserFeatureUsage, Long> {
    UserFeatureUsage findByUserIdAndFeatureCode(Long userId, String featureCode);
}
