package com.suiyou.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suiyou.model.Asset;
import com.suiyou.model.AssetSnapshot;
import com.suiyou.repository.AssetRepository;
import com.suiyou.repository.AssetSnapshotRepository;
import com.suiyou.service.AssetSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssetSnapshotServiceImpl implements AssetSnapshotService {

    @Autowired
    private AssetSnapshotRepository assetSnapshotRepository;
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public AssetSnapshot createMonthlyAssetSnapshot(Long userId, LocalDate snapshotDate) {
        // 获取用户所有有效资产
        List<Asset> assets = assetRepository.findByOwnerIdAndStatus(userId, 1);
        
        // 检查是否已存在该日期的快照
        AssetSnapshot existingSnapshot = assetSnapshotRepository.findByOwnerIdAndSnapshotDate(userId, snapshotDate);
        
        // 计算各类资产金额
        Map<String, BigDecimal> assetGroupBalances = calculateAssetGroupBalances(assets);
        
        // 生成详情快照JSON
        String detailsSnapshot = generateDetailsSnapshot(assets);
        
        // 计算汇总数值
        BigDecimal totalAssets = calculateTotalAssets(assetGroupBalances);
        BigDecimal totalLiabilities = assetGroupBalances.getOrDefault("LIABILITY", BigDecimal.ZERO);
        BigDecimal netWorth = totalAssets.subtract(totalLiabilities);
        
        if (existingSnapshot != null) {
            // 更新现有快照
            existingSnapshot.setLiquidAssets(assetGroupBalances.getOrDefault("LIQUID", BigDecimal.ZERO));
            existingSnapshot.setInvestmentAssets(assetGroupBalances.getOrDefault("INVEST", BigDecimal.ZERO));
            existingSnapshot.setFixedAssets(assetGroupBalances.getOrDefault("FIXED", BigDecimal.ZERO));
            existingSnapshot.setOtherAssets(assetGroupBalances.getOrDefault("OTHER_ASSET", BigDecimal.ZERO));
            existingSnapshot.setTotalLiabilities(totalLiabilities);
            existingSnapshot.setTotalAssets(totalAssets);
            existingSnapshot.setNetWorth(netWorth);
            existingSnapshot.setDetailsSnapshot(detailsSnapshot);
            return assetSnapshotRepository.save(existingSnapshot);
        } else {
            // 创建新快照
            AssetSnapshot snapshot = new AssetSnapshot();
            snapshot.setOwnerId(userId);
            snapshot.setSnapshotDate(snapshotDate);
            snapshot.setLiquidAssets(assetGroupBalances.getOrDefault("LIQUID", BigDecimal.ZERO));
            snapshot.setInvestmentAssets(assetGroupBalances.getOrDefault("INVEST", BigDecimal.ZERO));
            snapshot.setFixedAssets(assetGroupBalances.getOrDefault("FIXED", BigDecimal.ZERO));
            snapshot.setOtherAssets(assetGroupBalances.getOrDefault("OTHER_ASSET", BigDecimal.ZERO));
            snapshot.setTotalLiabilities(totalLiabilities);
            snapshot.setTotalAssets(totalAssets);
            snapshot.setNetWorth(netWorth);
            snapshot.setDetailsSnapshot(detailsSnapshot);
            return assetSnapshotRepository.save(snapshot);
        }
    }

    @Override
    @Transactional
    public AssetSnapshot createCurrentMonthAssetSnapshot(Long userId) {
        // 获取当前月份的最后一天
        LocalDate lastDayOfMonth = YearMonth.now().atEndOfMonth();
        return createMonthlyAssetSnapshot(userId, lastDayOfMonth);
    }

    @Override
    public AssetSnapshot getAssetSnapshotByDate(Long userId, LocalDate snapshotDate) {
        return assetSnapshotRepository.findByOwnerIdAndSnapshotDate(userId, snapshotDate);
    }

    @Override
    public List<AssetSnapshot> getAssetSnapshotsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return assetSnapshotRepository.findByOwnerIdAndSnapshotDateBetween(userId, startDate, endDate);
    }

    @Override
    public List<LocalDate> getDistinctSnapshotDates(Long userId) {
        return assetSnapshotRepository.findDistinctSnapshotDatesByOwnerId(userId);
    }

    @Override
    public List<AssetSnapshot> getAllAssetSnapshotsDesc(Long userId) {
        return assetSnapshotRepository.findAllByOwnerIdOrderBySnapshotDateDesc(userId);
    }
    
    /**
     * 按资产大类计算资产金额
     */
    private Map<String, BigDecimal> calculateAssetGroupBalances(List<Asset> assets) {
        return assets.stream()
                .collect(Collectors.groupingBy(
                    Asset::getGroupType,
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Asset::getTotalBalance,
                        BigDecimal::add
                    )
                ));
    }
    
    /**
     * 计算总资产（不包括负债）
     */
    private BigDecimal calculateTotalAssets(Map<String, BigDecimal> assetGroupBalances) {
        return assetGroupBalances.entrySet().stream()
                .filter(entry -> !"LIABILITY".equals(entry.getKey()))
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 生成资产详情快照JSON
     */
    private String generateDetailsSnapshot(List<Asset> assets) {
        // 按资产大类和分类分组
        Map<String, Map<String, List<Map<String, Object>>>> groupedDetails = assets.stream()
                .collect(Collectors.groupingBy(
                    Asset::getGroupType,
                    Collectors.groupingBy(
                        Asset::getCategory,
                        Collectors.mapping(asset -> {
                            Map<String, Object> assetDetail = new HashMap<>();
                            assetDetail.put("name", asset.getName());
                            assetDetail.put("amount", asset.getTotalBalance());
                            assetDetail.put("category", asset.getSubCategory());
                            assetDetail.put("currency", asset.getCurrency());
                            return assetDetail;
                        }, Collectors.toList())
                    )
                ));
        
        // 构建详情快照结构
        Map<String, Object> detailsSnapshot = new HashMap<>();
        detailsSnapshot.put("details", groupedDetails);
        detailsSnapshot.put("count", assets.size());
        
        try {
            return objectMapper.writeValueAsString(detailsSnapshot);
        } catch (JsonProcessingException e) {
            // 如果序列化失败，返回空JSON
            System.err.println("Failed to generate details snapshot: " + e.getMessage());
            return "{\"details\": {}, \"count\": 0}";
        }
    }
}