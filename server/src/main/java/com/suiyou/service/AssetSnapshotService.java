package com.suiyou.service;

import com.suiyou.model.AssetSnapshot;

import java.time.LocalDate;
import java.util.List;

public interface AssetSnapshotService {
    /**
     * 为用户创建月度资产快照（月末最后一天）
     * @param userId 用户ID
     * @param snapshotDate 快照日期（月末最后一天）
     * @return 资产快照
     */
    AssetSnapshot createMonthlyAssetSnapshot(Long userId, LocalDate snapshotDate);
    
    /**
     * 为用户创建当前月份的资产快照（月末最后一天）
     * @param userId 用户ID
     * @return 资产快照
     */
    AssetSnapshot createCurrentMonthAssetSnapshot(Long userId);
    
    /**
     * 根据日期获取用户的资产快照
     * @param userId 用户ID
     * @param snapshotDate 快照日期
     * @return 资产快照
     */
    AssetSnapshot getAssetSnapshotByDate(Long userId, LocalDate snapshotDate);
    
    /**
     * 根据日期范围获取用户的资产快照
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 资产快照列表
     */
    List<AssetSnapshot> getAssetSnapshotsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户所有快照日期
     * @param userId 用户ID
     * @return 快照日期列表
     */
    List<LocalDate> getDistinctSnapshotDates(Long userId);
    
    /**
     * 获取用户所有资产快照（按日期降序）
     * @param userId 用户ID
     * @return 资产快照列表
     */
    List<AssetSnapshot> getAllAssetSnapshotsDesc(Long userId);
}