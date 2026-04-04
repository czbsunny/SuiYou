package com.suiyou.controller;

import com.suiyou.dto.portfolio.CreatePortfolioHoldingDTO;
import com.suiyou.dto.portfolio.PortfolioHoldingRespDTO;
import com.suiyou.service.PortfolioHoldingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portfolio-holdings")
public class PortfolioHoldingController {

    @Autowired
    private PortfolioHoldingService portfolioHoldingService;

    @PostMapping
    public ResponseEntity<?> createPortfolioHolding(@RequestBody CreatePortfolioHoldingDTO createPortfolioHoldingDTO, @RequestParam Long portfolioId) {
        try {
            portfolioHoldingService.createPortfolioHolding(createPortfolioHoldingDTO, portfolioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "持仓创建成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "持仓创建失败",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<?> getPortfolioHoldingsByPortfolioId(@PathVariable Long portfolioId) {
        try {
            List<PortfolioHoldingRespDTO> holdings = portfolioHoldingService.getPortfolioHoldingsByPortfolioId(portfolioId);
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getPortfolioHoldingById(@PathVariable Long id) {
        try {
            PortfolioHoldingRespDTO holding = portfolioHoldingService.getPortfolioHoldingById(id);
            return ResponseEntity.ok(Map.of(
                    "holding", holding
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "获取持仓失败",
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePortfolioHolding(@PathVariable Long id, @RequestBody CreatePortfolioHoldingDTO createPortfolioHoldingDTO) {
        try {
            portfolioHoldingService.updatePortfolioHolding(id, createPortfolioHoldingDTO);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePortfolioHolding(@PathVariable Long id) {
        try {
            portfolioHoldingService.deletePortfolioHolding(id);
            return ResponseEntity.ok(Map.of(
                    "id", id,
                    "message", "持仓删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "持仓删除失败",
                    "message", e.getMessage()
            ));
        }
    }
}