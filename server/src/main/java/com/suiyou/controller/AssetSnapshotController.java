package com.suiyou.controller;

import com.suiyou.model.AssetSnapshot;
import com.suiyou.service.AssetSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assetsnapshots")
public class AssetSnapshotController {

    @Autowired
    private AssetSnapshotService assetSnapshotService;

    /**
     * 创建当前用户所有资产的快照
     */
    @PostMapping
    public ResponseEntity<?> createAllAssetSnapshots() {
        try {
            Long userId = getCurrentUserId();
            AssetSnapshot snapshot = assetSnapshotService.createCurrentMonthAssetSnapshot(userId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "snapshot", snapshot,
                "message", "资产快照创建成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "创建资产快照失败",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 根据日期获取资产快照
     */
    @GetMapping("/date")
    public ResponseEntity<?> getAssetSnapshotsByDate(@RequestParam LocalDate date) {
        try {
            Long userId = getCurrentUserId();
            AssetSnapshot snapshot = assetSnapshotService.getAssetSnapshotByDate(userId, date);
            
            return ResponseEntity.ok(Map.of(
                "snapshot", snapshot,
                "date", date
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "获取资产快照失败",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 根据日期范围获取用户的资产快照
     */
    @GetMapping("/range")
    public ResponseEntity<?> getAssetSnapshotsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        try {
            Long userId = getCurrentUserId();
            List<AssetSnapshot> snapshots = assetSnapshotService.getAssetSnapshotsByDateRange(userId, startDate, endDate);
            
            return ResponseEntity.ok(Map.of(
                "snapshots", snapshots,
                "count", snapshots.size(),
                "startDate", startDate,
                "endDate", endDate
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "获取资产快照失败",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取所有快照日期
     */
    @GetMapping("/dates")
    public ResponseEntity<?> getDistinctSnapshotDates() {
        try {
            Long userId = getCurrentUserId();
            List<LocalDate> dates = assetSnapshotService.getDistinctSnapshotDates(userId);
            
            return ResponseEntity.ok(Map.of(
                "dates", dates,
                "count", dates.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "获取快照日期失败",
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