package com.suiyou.controller;

import com.suiyou.model.AssetSnapshot;
import com.suiyou.service.AssetSnapshotService;
import com.suiyou.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assetsnapshots")
public class AssetSnapshotController {

    @Autowired
    private AssetSnapshotService assetSnapshotService;

    @PostMapping
    public ResponseEntity<?> createAllAssetSnapshots() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
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

    @GetMapping("/date")
    public ResponseEntity<?> getAssetSnapshotsByDate(@RequestParam LocalDate date) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
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

    @GetMapping("/range")
    public ResponseEntity<?> getAssetSnapshotsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
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

    @GetMapping("/dates")
    public ResponseEntity<?> getDistinctSnapshotDates() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
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
}
