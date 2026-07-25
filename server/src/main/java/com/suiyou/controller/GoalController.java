package com.suiyou.controller;

import com.suiyou.dto.goal.CreateGoalDTO;
import com.suiyou.dto.goal.GoalCategoryRespDTO;
import com.suiyou.dto.goal.GoalRespDTO;
import com.suiyou.dto.goal.GoalTemplateRespDTO;
import com.suiyou.security.SecurityUtils;
import com.suiyou.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goals")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @GetMapping("/categories")
    public ResponseEntity<List<GoalCategoryRespDTO>> getGoalCategories() {
        List<GoalCategoryRespDTO> categories = goalService.getGoalCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/templates")
    public ResponseEntity<List<GoalTemplateRespDTO>> getGoalTemplates(@RequestParam(required = false) String categoryCode) {
        List<GoalTemplateRespDTO> templates = goalService.getGoalTemplates(categoryCode);
        return ResponseEntity.ok(templates);
    }

    @GetMapping
    public ResponseEntity<List<GoalRespDTO>> getUserGoals() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<GoalRespDTO> goals = goalService.getUserGoals(userId);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalRespDTO> getGoalById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        GoalRespDTO goal = goalService.getGoalById(id, userId);
        return ResponseEntity.ok(goal);
    }

    @PostMapping
    public ResponseEntity<GoalRespDTO> createGoal(@Valid @RequestBody CreateGoalDTO goalDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        GoalRespDTO goalRespDTO = goalService.createGoal(goalDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(goalRespDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        goalService.deleteGoal(id, userId);
        return ResponseEntity.noContent().build();
    }
}