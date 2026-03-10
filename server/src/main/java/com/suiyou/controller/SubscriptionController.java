package com.suiyou.controller;

import com.suiyou.model.UserSubscription;
import com.suiyou.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        return Long.parseLong(authentication.getName());
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserSubscription() {
        Long userId = getCurrentUserId();
        UserSubscription subscription = subscriptionService.getUserSubscription(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("hasSubscription", subscription != null && "ACTIVE".equals(subscription.getStatus()));
        response.put("subscription", subscription);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribeToPlan(@RequestBody Map<String, String> request) {
        Long userId = getCurrentUserId();
        String planCode = request.get("planCode");
        
        UserSubscription subscription = subscriptionService.subscribeToPlan(userId, planCode);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "订阅成功",
            "subscription", subscription
        ));
    }

    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelSubscription() {
        Long userId = getCurrentUserId();
        UserSubscription subscription = subscriptionService.cancelSubscription(userId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "取消订阅成功",
            "subscription", subscription
        ));
    }
}
