package com.suiyou.controller;

import com.suiyou.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 获取资产报告
     * @return 资产报告数据
     */
    @GetMapping("/assets")
    public ResponseEntity<?> getAssetReport() {
        try {
            Object assetReport = reportService.generateAssetReport();
            return ResponseEntity.ok(assetReport);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取目标报告
     * @return 目标报告数据
     */
    @GetMapping("/goals")
    public ResponseEntity<?> getGoalReport() {
        try {
            Object goalReport = reportService.generateGoalReport();
            return ResponseEntity.ok(goalReport);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
