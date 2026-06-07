package com.suiyou.scheduler;

import com.suiyou.model.AssetSnapshot;
import com.suiyou.repository.UserRepository;
import com.suiyou.service.AssetSnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class AssetSnapshotScheduler {

    @Autowired
    private AssetSnapshotService assetSnapshotService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 每月1日0点5分创建上月的资产快照
     * cron表达式：0 5 0 1 * ?
     */
    @Scheduled(cron = "0 5 0 1 * ?")
    public void createMonthlyAssetSnapshots() {
        log.info("开始执行月度资产快照创建任务");
        
        // 获取上个月的最后一天
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate lastDayOfLastMonth = lastMonth.atEndOfMonth();
        
        // 获取所有有效用户
        List<Long> userIds = userRepository.findAllValidUserIds();
        
        // 并行为每个用户创建快照
        userIds.parallelStream().forEach(userId -> {
            try {
                AssetSnapshot snapshot = assetSnapshotService.createMonthlyAssetSnapshot(userId, lastDayOfLastMonth);
                log.info("为用户 {} 创建了 {} 的资产快照，快照ID: {}", 
                        userId, lastDayOfLastMonth, snapshot.getId());
            } catch (Exception e) {
                log.error("为用户 {} 创建 {} 的资产快照失败: {}", 
                        userId, lastDayOfLastMonth, e.getMessage());
            }
        });
        
        log.info("月度资产快照创建任务执行完成，共处理 {} 个用户", userIds.size());
    }
    
    /**
     * 每月3日和7日0点5分更新上个月的资产快照
     * cron表达式：0 5 0 3,7 * ?
     */
    @Scheduled(cron = "0 5 0 3,7 * ?")
    public void updateMonthlyAssetSnapshots() {
        log.info("开始执行月度资产快照更新任务");
        
        // 获取上个月的最后一天
        LocalDate lastDayOfLastMonth = YearMonth.now().minusMonths(1).atEndOfMonth();
        
        // 获取所有有效用户
        List<Long> userIds = userRepository.findAllValidUserIds();
        
        // 并行为每个用户更新快照
        userIds.parallelStream().forEach(userId -> {
            try {
                AssetSnapshot snapshot = assetSnapshotService.createMonthlyAssetSnapshot(userId, lastDayOfLastMonth);
                log.info("为用户 {} 更新了 {} 的资产快照，快照ID: {}", 
                        userId, lastDayOfLastMonth, snapshot.getId());
            } catch (Exception e) {
                log.error("为用户 {} 更新 {} 的资产快照失败: {}", 
                        userId, lastDayOfLastMonth, e.getMessage());
            }
        });
        
        log.info("月度资产快照更新任务执行完成，共处理 {} 个用户", userIds.size());
    }
}