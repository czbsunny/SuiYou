package com.suiyou.service;

import com.suiyou.model.FeatureLimit;
import com.suiyou.model.UserSubscription;

public interface SubscriptionService {
    
    UserSubscription getUserSubscription(Long userId);
    
    UserSubscription subscribeToPlan(Long userId, String planCode);
    
    UserSubscription cancelSubscription(Long userId);
    
    boolean checkFeatureLimit(Long userId, String featureCode);
    
    void recordFeatureUsage(Long userId, String featureCode);
    
    FeatureLimit getFeatureLimit(Long userId, String featureCode);
}
