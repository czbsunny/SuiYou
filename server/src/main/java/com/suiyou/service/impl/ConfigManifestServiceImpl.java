package com.suiyou.service.impl;

import com.suiyou.service.ConfigManifestService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.suiyou.model.SysConfigVersion;
import com.suiyou.service.SysAssetCategoryService;
import com.suiyou.service.SysGoalCategoryService;
import com.suiyou.repository.SysConfigVersionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import com.suiyou.dto.ConfigManifestRespDTO;

@Service
@Slf4j
public class ConfigManifestServiceImpl implements ConfigManifestService {

    @Autowired
    private SysConfigVersionRepository versionRepository;

    @Autowired
    private SysAssetCategoryService assetCategoryService;
    
    @Autowired
    private SysGoalCategoryService goalCategoryService;
    
    public ConfigManifestServiceImpl() {

    }

    @Override
    public Map<String, Object> fetchConfigs(List<String> keys) {
        Map<String, Object> resultMap = new HashMap<>();
        
        if (keys == null || keys.isEmpty()) return resultMap;

        for (String key : keys) {
            // 根据 Key 路由到不同的业务 Service
            switch (key) {
                case "asset_categories":
                    // 业务 Service 负责查库并组装成 Tree
                    resultMap.put(key, assetCategoryService.getCategoryTree()); 
                    break;
                    
                case "goal_categories":
                    // 如果是简单的列表，直接返回 List
                    resultMap.put(key, goalCategoryService.getGoalCategories());
                    break;
                    
                default:
                    // 忽略未知的 Key
                    break;
            }
        }
        return resultMap;
    }

    private

    @Override
    public 
 getConfigManifest() {

        // 1. 查出所有版本 (一定要按 Key 排序！否则 GlobalHash 会乱跳)
        List<SysConfigVersion> versions = versionRepository.findAllByOrderByModuleKeyAsc();
        
        // 2. 组装 Modules Map
        Map<String, String> modules = new LinkedHashMap<>();
        StringBuilder hashBuilder = new StringBuilder();
        
        for (SysConfigVersion v : versions) {
            modules.put(v.getModuleKey(), v.getVersionHash());
            hashBuilder.append(v.getVersionHash());
        }
        
        // 3. 计算全局 Hash
        String globalHash = DigestUtils.md5DigestAsHex(hashBuilder.toString().getBytes());
        
        // 4. 返回 DTO
        return new ConfigManifestRespDTO(globalHash, modules);
    }
}