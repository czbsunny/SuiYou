package com.suiyou.controller;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suiyou.dto.asset.CreateAssetDTO;
import com.suiyou.dto.asset.AssetRespDTO;
import com.suiyou.service.AssetService;
import com.suiyou.security.SecurityUtils;

import com.suiyou.dto.portfolio.PortfolioRespDTO;
import com.suiyou.service.PortfolioService;
import com.suiyou.service.AssetSnapshotService;

@RestController
@RequestMapping("/assets")
public class AssetController {
    
    @Autowired
    private AssetService assetService;

    @Autowired
    private PortfolioService portfolioService;
    
    @Autowired
    private AssetSnapshotService assetSnapshotService;

    @PostMapping
    public ResponseEntity<?> createAsset(@RequestBody CreateAssetDTO asset) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            AssetRespDTO createdAsset = assetService.createAsset(asset, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "asset", createdAsset,
                "message", "资产添加成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "资产添加失败",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAssets() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            List<AssetRespDTO> assets = assetService.getAssetsByUserId(userId);
            return ResponseEntity.ok(Map.of(
                "assets", assets,
                "count", assets.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "获取资产账户失败",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfoliosByAssetId(@RequestParam Long assetId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            PortfolioRespDTO portfolio = portfolioService.getPortfolioByAssetId(assetId);
            return ResponseEntity.ok(Map.of(
                "portfolio", portfolio
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "获取组合失败",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/networth")
    public ResponseEntity<?> getNetWorth() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            BigDecimal currentNetWorth = assetService.getCurrentNetWorth(userId);
            BigDecimal lastMonthNetWorth = assetSnapshotService.getLastMonthNetWorth(userId);
            return ResponseEntity.ok(Map.of(
                "currentNetWorth", currentNetWorth,
                "lastMonthNetWorth", lastMonthNetWorth
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "获取净资产增长比值失败",
                "message", e.getMessage()
            ));
        }
    }
}
