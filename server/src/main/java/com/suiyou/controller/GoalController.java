package com.suiyou.controller;

import com.suiyou.dto.goal.CreateGoalDTO;
import com.suiyou.model.Goal;
import com.suiyou.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goals")
public class GoalController {

    @Autowired
    private GoalService goalService;

    // 获取用户的所有目标
    @GetMapping
    public ResponseEntity<?> getUserGoals() {
        try {
            // 从Security上下文中获取用户ID
            Long userId = getCurrentUserId();
            List<Goal> goals = goalService.getUserGoals(userId);
            return ResponseEntity.ok(Map.of(
                "goals", goals,
                "count", goals.size()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "获取目标失败",
                "message", e.getMessage()
            ));
        }
    }

    // 创建新目标
    @PostMapping
    public ResponseEntity<?> createGoal(@Valid @RequestBody CreateGoalDTO goalDTO) {
        try {
            Long userId = getCurrentUserId();
            
            Goal goal = goalService.createGoal(goalDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", goal.getId(),
                "message", "目标创建成功",
                "goal", goal
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "目标创建失败",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取当前登录用户的ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new RuntimeException("无法从认证信息中获取用户ID");
        }
    }
}