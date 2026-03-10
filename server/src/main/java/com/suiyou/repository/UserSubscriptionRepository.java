package com.suiyou.repository;

import com.suiyou.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    UserSubscription findByUserIdAndStatus(Long userId, String status);
    UserSubscription findTopByUserIdOrderByStartAtDesc(Long userId);
}
