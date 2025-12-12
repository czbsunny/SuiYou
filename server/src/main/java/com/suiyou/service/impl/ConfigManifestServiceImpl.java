package com.suiyou.service.impl;

import com.suiyou.service.ConfigManifestService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class ConfigManifestServiceImpl implements ConfigManifestService {

    // 模拟的配置数据
    private final Map<String, Object> configData = new HashMap<>();
    // 模块版本信息
    private final Map<String, Map<String, String>> modules = new HashMap<>();

    public ConfigManifestServiceImpl() {
        // 初始化模块版本信息
        initModules();
        // 初始化配置数据
        initConfigData();
    }

    private void initModules() {
        // 资产分类模块
        Map<String, String> assetCategoriesModule = new HashMap<>();
        assetCategoriesModule.put("version", "v1.2");
        assetCategoriesModule.put("time", "202312121000");
        assetCategoriesModule.put("hash", "md5_value_1");
        modules.put("asset_categories", assetCategoriesModule);

        // 目标分类模块
        Map<String, String> goalCategoriesModule = new HashMap<>();
        goalCategoriesModule.put("version", "v1.0");
        goalCategoriesModule.put("time", "202312131000");
        goalCategoriesModule.put("hash", "md5_value_2");
        modules.put("goal_categories", goalCategoriesModule);

        // 目标向导配置模块
        Map<String, String> goalWizardModule = new HashMap<>();
        goalWizardModule.put("version", "v3.1");
        goalWizardModule.put("time", "202312221000");
        goalWizardModule.put("hash", "md5_value_3");
        modules.put("goal_wizard_config", goalWizardModule);
    }

    private void initConfigData() {
        // 模拟资产分类树形结构
        List<Map<String, Object>> assetCategories = new ArrayList<>();
        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 1);
        category1.put("name", "现金");
        category1.put("code", "cash");
        category1.put("children", Arrays.asList(
                createChildCategory(11, "活期存款", "current_deposit"),
                createChildCategory(12, "定期存款", "fixed_deposit")
        ));

        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", 2);
        category2.put("name", "投资");
        category2.put("code", "investment");
        category2.put("children", Arrays.asList(
                createChildCategory(21, "基金", "fund"),
                createChildCategory(22, "股票", "stock")
        ));

        assetCategories.add(category1);
        assetCategories.add(category2);
        configData.put("asset_categories", assetCategories);

        // 模拟目标向导配置
        Map<String, Object> goalWizardConfig = new HashMap<>();
        goalWizardConfig.put("steps", Arrays.asList(
                createWizardStep(1, "目标类型", "goal_type", true),
                createWizardStep(2, "目标金额", "goal_amount", true),
                createWizardStep(3, "目标期限", "goal_period", true),
                createWizardStep(4, "投资偏好", "investment_preference", false)
        ));
        goalWizardConfig.put("defaultValues", Map.of(
                "riskTolerance", "medium",
                "investmentHorizon", "medium"
        ));
        configData.put("goal_wizard_config", goalWizardConfig);

        // 模拟目标分类数据
        List<Map<String, Object>> goalCategories = Arrays.asList(
                createGoalCategory(1, "短期目标", "short_term", "1年内达成的目标", "icon_short_term", "#FF6B6B"),
                createGoalCategory(2, "中期目标", "medium_term", "1-3年内达成的目标", "icon_medium_term", "#4ECDC4"),
                createGoalCategory(3, "长期目标", "long_term", "3年以上达成的目标", "icon_long_term", "#45B7D1")
        );
        configData.put("goal_categories", goalCategories);
    }

    private Map<String, Object> createChildCategory(int id, String name, String code) {
        Map<String, Object> category = new HashMap<>();
        category.put("id", id);
        category.put("name", name);
        category.put("code", code);
        return category;
    }

    private Map<String, Object> createWizardStep(int id, String name, String key, boolean required) {
        Map<String, Object> step = new HashMap<>();
        step.put("id", id);
        step.put("name", name);
        step.put("key", key);
        step.put("required", required);
        return step;
    }

    private Map<String, Object> createGoalCategory(int id, String name, String code, String description, String icon, String color) {
        Map<String, Object> category = new HashMap<>();
        category.put("id", id);
        category.put("name", name);
        category.put("code", code);
        category.put("description", description);
        category.put("icon", icon);
        category.put("color", color);
        return category;
    }

    @Override
    public Map<String, Object> getConfigManifest() {
        Map<String, Object> result = new HashMap<>();
        
        // 计算全局哈希
        String globalHash = calculateGlobalHash();
        result.put("globalHash", globalHash);
        
        // 添加模块版本信息
        result.put("modules", modules);
        
        return result;
    }

    @Override
    public Map<String, Object> fetchConfigs(List<String> keys) {
        Map<String, Object> result = new HashMap<>();
        
        for (String key : keys) {
            if (configData.containsKey(key)) {
                result.put(key, configData.get(key));
            }
        }
        
        return result;
    }

    private String calculateGlobalHash() {
        // 简单实现：将所有模块的哈希拼接后再计算MD5
        StringBuilder combinedHashes = new StringBuilder();
        modules.values().forEach(module -> combinedHashes.append(module.get("hash")));
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(combinedHashes.toString().getBytes());
            
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // 如果MD5算法不可用，返回一个默认值
            return "default_global_hash";
        }
    }
}