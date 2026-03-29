package com.suiyou.controller;

import com.suiyou.dto.portfolio.CreatePortfolioItemDTO;
import com.suiyou.dto.portfolio.PortfolioItemRespDTO;
import com.suiyou.service.PortfolioItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portfolio-items")
public class PortfolioItemController {

    @Autowired
    private PortfolioItemService portfolioItemService;

    @PostMapping
    public ResponseEntity<?> createPortfolioItem(@RequestBody CreatePortfolioItemDTO createPortfolioItemDTO, @RequestParam Long portfolioId) {
        try {
            portfolioItemService.createPortfolioItem(createPortfolioItemDTO, portfolioId);
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
    public ResponseEntity<?> getPortfolioItemsByPortfolioId(@PathVariable Long portfolioId) {
        try {
            List<PortfolioItemRespDTO> items = portfolioItemService.getPortfolioItemsByPortfolioId(portfolioId);
            return ResponseEntity.ok(Map.of(
                    "items", items,
                    "count", items.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "获取持仓列表失败",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPortfolioItemById(@PathVariable Long id) {
        try {
            PortfolioItemRespDTO item = portfolioItemService.getPortfolioItemById(id);
            return ResponseEntity.ok(Map.of(
                    "item", item
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "获取持仓失败",
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePortfolioItem(@PathVariable Long id, @RequestBody CreatePortfolioItemDTO createPortfolioItemDTO) {
        try {
            portfolioItemService.updatePortfolioItem(id, createPortfolioItemDTO);
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
    public ResponseEntity<?> deletePortfolioItem(@PathVariable Long id) {
        try {
            portfolioItemService.deletePortfolioItem(id);
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