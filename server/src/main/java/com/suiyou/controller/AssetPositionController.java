package com.suiyou.controller;

import com.suiyou.model.AssetPosition;
import com.suiyou.service.AssetPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/asset-positions")
public class AssetPositionController {

    @Autowired
    private AssetPositionService assetPositionService;

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<AssetPosition>> getPositions(@PathVariable Long assetId) {
        return ResponseEntity.ok(assetPositionService.getPositions(assetId));
    }

    @PostMapping
    public ResponseEntity<AssetPosition> addPosition(@RequestBody Map<String, Object> request) {
        Long assetId = Long.parseLong(request.get("assetId").toString());
        String symbol = (String) request.get("symbol");
        String name = (String) request.get("name");
        BigDecimal quantity = new BigDecimal(request.get("quantity").toString());
        BigDecimal cost = new BigDecimal(request.get("cost").toString());

        return ResponseEntity.ok(assetPositionService.addPosition(assetId, symbol, name, quantity, cost));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetPosition> updatePosition(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        BigDecimal quantity = request.containsKey("quantity") ? new BigDecimal(request.get("quantity").toString()) : null;
        BigDecimal cost = request.containsKey("cost") ? new BigDecimal(request.get("cost").toString()) : null;

        return ResponseEntity.ok(assetPositionService.updatePosition(id, quantity, cost));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        assetPositionService.deletePosition(id);
        return ResponseEntity.ok().build();
    }
}
