package com.suiyou.controller;

import com.suiyou.model.TimeDepositItem;
import com.suiyou.service.TimeDepositItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/timedeposit")
public class TimeDepositItemController {

    @Autowired
    private TimeDepositItemService timeDepositItemService;

    @PostMapping
    public ResponseEntity<?> createTimeDepositItem(@RequestBody TimeDepositItem item) {
        try {
            TimeDepositItem createdItem = timeDepositItemService.createTimeDepositItem(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "item", createdItem,
                "message", "定期存款记录创建成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "创建失败",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTimeDepositItem(@PathVariable Long id, @RequestBody TimeDepositItem item) {
        try {
            TimeDepositItem updatedItem = timeDepositItemService.updateTimeDepositItem(id, item);
            return ResponseEntity.ok(Map.of(
                "item", updatedItem,
                "message", "定期存款记录更新成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "更新失败",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTimeDepositItemById(@PathVariable Long id) {
        try {
            TimeDepositItem item = timeDepositItemService.getTimeDepositItemById(id);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "记录不存在",
                    "message", "未找到指定的定期存款记录"
                ));
            }
            return ResponseEntity.ok(Map.of(
                "item", item,
                "totalExpectedInterest", item.calculateTotalExpectedInterest(),
                "accruedInterest", item.calculateAccruedInterest(),
                "daysRemaining", item.calculateDaysRemaining(),
                "progressPercentage", item.calculateProgressPercentage(),
                "maturityAmount", item.calculateMaturityAmount(),
                "currentValue", item.calculateCurrentValue()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "获取失败",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getTimeDepositItemsByAssetId(@RequestParam Long assetId) {
        try {
            List<TimeDepositItem> items = timeDepositItemService.getTimeDepositItemsByAssetId(assetId);
            return ResponseEntity.ok(Map.of(
                "items", items,
                "count", items.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "获取失败",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTimeDepositItem(@PathVariable Long id) {
        try {
            boolean deleted = timeDepositItemService.deleteTimeDepositItem(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "删除失败",
                    "message", "未找到指定的定期存款记录"
                ));
            }
            return ResponseEntity.ok(Map.of(
                "message", "定期存款记录删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "删除失败",
                "message", e.getMessage()
            ));
        }
    }
}