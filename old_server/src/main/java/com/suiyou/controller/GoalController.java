package com.suiyou.controller;

import com.suiyou.dto.goal.CreateGoalDTO;
import com.suiyou.dto.goal.GoalRespDTO;
import com.suiyou.service.GoalService;
import com.suiyou.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goals")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @GetMapping
    public ResponseEntity<?> getUserGoals() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            List<GoalRespDTO> goals = goalService.getUserGoals(userId);
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getGoalById(@PathVariable Long id) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            GoalRespDTO goal = goalService.getGoalById(id, userId);
            return ResponseEntity.ok(Map.of(
                "goal", goal
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "获取目标失败",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createGoal(@Valid @RequestBody CreateGoalDTO goalDTO) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            GoalRespDTO goalRespDTO = goalService.createGoal(goalDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "目标创建成功",
                "goal", goalRespDTO
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "目标创建失败",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long id) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            goalService.deleteGoal(id, userId);
            return ResponseEntity.ok(Map.of(
                "message", "目标删除成功"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "目标删除失败",
                "message", e.getMessage()
            ));
        }
    }
}
