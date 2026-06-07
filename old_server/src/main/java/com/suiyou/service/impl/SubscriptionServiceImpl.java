package com.suiyou.service.impl;

import com.suiyou.model.Feature;
import com.suiyou.model.FeatureLimit;
import com.suiyou.model.UserSubscription;
import com.suiyou.model.UserFeatureUsage;
import com.suiyou.repository.FeatureLimitRepository;
import com.suiyou.repository.FeatureRepository;
import com.suiyou.repository.UserSubscriptionRepository;
import com.suiyou.repository.UserFeatureUsageRepository;
import com.suiyou.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionServiceImpl.class);
    
    @Autowired
    private FeatureRepository featureRepository;
    
    @Autowired
    private FeatureLimitRepository featureLimitRepository;
    
    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;
    
    @Autowired
    private UserFeatureUsageRepository userFeatureUsageRepository;
    
    @Override
    public UserSubscription getUserSubscription(Long userId) {
        return userSubscriptionRepository.findTopByUserIdOrderByStartAtDesc(userId);
    }
    
    @Override
    @Transactional
    public UserSubscription subscribeToPlan(Long userId, String planCode) {
        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(userId);
        subscription.setPlanCode(planCode);
        subscription.setStartAt(LocalDateTime.now());
        subscription.setStatus("ACTIVE");
        userSubscriptionRepository.save(subscription);
        
        logger.info("用户 {} 订阅了套餐 {}", userId, planCode);
        return subscription;
    }
    
    @Override
    @Transactional
    public UserSubscription cancelSubscription(Long userId) {
        UserSubscription subscription = getUserSubscription(userId);
        if (subscription == null) {
            throw new RuntimeException("用户没有订阅");
        }
        
        subscription.setEndAt(LocalDateTime.now());
        subscription.setStatus("EXPIRED");
        userSubscriptionRepository.save(subscription);
        
        logger.info("用户 {} 取消了订阅", userId);
        return subscription;
    }
    
    public FeatureLimit getFeatureLimit(Long userId, String featureCode) {
        UserSubscription subscription = getUserSubscription(userId);
        if (subscription == null) {
            return null;
        }
        
        Feature feature = featureRepository.findByCode(featureCode);
        if (feature == null) {
            return null;
        }
        
        return featureLimitRepository.findByFeatureCodeAndPlanCode(featureCode, subscription.getPlanCode());
    }
    
    public boolean checkFeatureLimit(Long userId, String featureCode) {
        FeatureLimit limit = getFeatureLimit(userId, featureCode);
        if (limit == null) {
            return true;
        }
        
        LocalDate today = LocalDate.now();
        
        if (limit.getDailyLimit() != null) {
            UserFeatureUsage usage = userFeatureUsageRepository.findByUserIdAndFeatureCode(userId, featureCode);
            int dailyTotal = 0;
            if (usage != null) {
                LocalDate lastUpdateDate = usage.getUpdatedAt().toLocalDate();
                if (lastUpdateDate.equals(today)) {
                    dailyTotal = usage.getDailyUsed();
                }
            }
            if (dailyTotal >= limit.getDailyLimit()) {
                logger.warn("用户 {} 功能 {} 当日使用次数已达上限: {}/{}", userId, featureCode, dailyTotal, limit.getDailyLimit());
                return false;
            }
        }
        
        if (limit.getTotalLimit() != null) {
            UserFeatureUsage totalUsage = userFeatureUsageRepository.findByUserIdAndFeatureCode(userId, featureCode);
            int totalUsed = totalUsage != null ? totalUsage.getTotalUsed() : 0;
            if (totalUsed >= limit.getTotalLimit()) {
                logger.warn("用户 {} 功能 {} 总使用次数已达上限: {}/{}", userId, featureCode, totalUsed, limit.getTotalLimit());
                return false;
            }
        }
        
        return true;
    }
    
    @Transactional
    public void recordFeatureUsage(Long userId, String featureCode) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        
        // 按用户和功能代码查询唯一记录
        UserFeatureUsage usage = userFeatureUsageRepository.findByUserIdAndFeatureCode(userId, featureCode);
        
        if (usage == null) {
            usage = new UserFeatureUsage();
            usage.setUserId(userId);
            usage.setFeatureCode(featureCode);
            usage.setDailyUsed(1);
            usage.setMonthlyUsed(1);
            usage.setTotalUsed(1);
            usage.setCreatedAt(now);
            usage.setUpdatedAt(now);
        } else {
            // 检查是否需要重置每日计数
            LocalDate lastUpdateDate = usage.getUpdatedAt().toLocalDate();
            if (!lastUpdateDate.equals(today)) {
                usage.setDailyUsed(1);
            } else {
                usage.setDailyUsed(usage.getDailyUsed() + 1);
            }
            
            // 检查是否需要重置每月计数
            int lastUpdateMonth = usage.getUpdatedAt().getMonthValue();
            int lastUpdateYear = usage.getUpdatedAt().getYear();
            if (lastUpdateYear != currentYear || lastUpdateMonth != currentMonth) {
                usage.setMonthlyUsed(1);
            } else {
                usage.setMonthlyUsed(usage.getMonthlyUsed() + 1);
            }
            
            // 总使用次数一直累加
            usage.setTotalUsed(usage.getTotalUsed() + 1);
            usage.setUpdatedAt(now);
        }
        
        userFeatureUsageRepository.save(usage);
        logger.debug("记录用户 {} 功能 {} 使用次数", userId, featureCode);
    }
}
