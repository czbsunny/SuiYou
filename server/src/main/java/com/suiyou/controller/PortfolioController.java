package com.suiyou.controller;

import com.suiyou.dto.portfolio.CreatePortfolioDTO;
import com.suiyou.dto.portfolio.PortfolioRespDTO;
import com.suiyou.dto.portfolio.PortfolioHoldingRespDTO;
import com.suiyou.dto.portfolio.UpdatePortfolioHoldingsDTO;
import com.suiyou.service.PortfolioService;
import com.suiyou.service.PortfolioHoldingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portfolios")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private PortfolioHoldingService portfolioHoldingService;

    @GetMapping
    public ResponseEntity<?> getPortfolios() {
        try {
            Long userId = getCurrentUserId();
            List<PortfolioRespDTO> portfolios = portfolioService.getPortfoliosByUserId(userId);
            return ResponseEntity.ok(Map.of(
                    "portfolios", portfolios,
                    "count", portfolios.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "获取组合列表失败",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createPortfolio(@RequestBody CreatePortfolioDTO createPortfolioDTO) {
        try {
            Long userId = getCurrentUserId();
            portfolioService.createPortfolio(createPortfolioDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "组合创建成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "组合创建失败",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPortfolioById(@PathVariable Long id) {
        try {
            PortfolioRespDTO portfolio = portfolioService.getPortfolioById(id);
            return ResponseEntity.ok(Map.of(
                    "portfolio", portfolio
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "获取组合失败",
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePortfolio(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            portfolioService.deletePortfolio(id, userId);
            return ResponseEntity.ok(Map.of(
                    "id", id,
                    "message", "组合删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "组合删除失败",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}/holdings")
    public ResponseEntity<?> getPortfolioHoldingsByPortfolioId(@PathVariable Long id) {
        try {
            List<PortfolioHoldingRespDTO> holdings = portfolioHoldingService.getPortfolioHoldingsByPortfolioId(id);
            return ResponseEntity.ok(Map.of(
                    "holdings", holdings,
                    "count", holdings.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "获取持仓列表失败",
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/holdings")
    public ResponseEntity<?> updatePortfolioHoldings(@PathVariable Long id, @RequestBody UpdatePortfolioHoldingsDTO updatePortfolioHoldingsDTO) {
        try {
            portfolioHoldingService.updatePortfolioHoldings(id, updatePortfolioHoldingsDTO);
            return ResponseEntity.ok(Map.of(
                    "id", id,
                    "message", "持仓更新成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "持仓更新失败",
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}/holdings")
    public ResponseEntity<?> clearPortfolioHoldings(@PathVariable Long id) {
        try {
            portfolioHoldingService.clearPortfolioHoldings(id);
            return ResponseEntity.ok(Map.of(
                    "id", id,
                    "message", "持仓清空成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "持仓清空失败",
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