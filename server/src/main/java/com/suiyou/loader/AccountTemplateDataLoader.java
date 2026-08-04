package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suiyou.enums.AssetType;
import com.suiyou.model.SysAccountTemplate;
import com.suiyou.repository.SysAccountTemplateRepository;
import com.suiyou.repository.SysInstitutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.*;

@Component
@Order(2)
@lombok.extern.slf4j.Slf4j
public class AccountTemplateDataLoader extends AbstractConfigLoader {

    @Autowired
    private SysAccountTemplateRepository accountTemplateRepository;
    
    @Autowired
    private SysInstitutionRepository institutionRepository;
    
    @Value("classpath:sys_account_template_init.json")
    private Resource jsonResource;
    
    @Override
    protected void loadConfig() throws Exception {
        if (!jsonResource.exists()) {
            log.warn("账户模板配置文件不存在，跳过加载");
            return;
        }

        Map<String, Object> config = objectMapper.readValue(
            jsonResource.getInputStream().readAllBytes(), 
            new TypeReference<Map<String, Object>>() {}
        );

        Map<String, SysAccountTemplate> templateMap = new HashMap<>();
        
        Map<String, Object> typeRules = (Map<String, Object>) config.get("typeRules");
        List<Map<String, Object>> instAssetRules = (List<Map<String, Object>>) config.get("instAssetRules");

        List<String> allInstCodes = institutionRepository.findAll().stream()
            .map(inst -> inst.getInstCode())
            .toList();
        
        log.info("发现 {} 个机构，开始生成账户模板...", allInstCodes.size());

        for (Map.Entry<String, Object> typeEntry : typeRules.entrySet()) {
            String accountType = typeEntry.getKey();
            Map<String, Object> assetRules = (Map<String, Object>) typeEntry.getValue();
            
            for (String instCode : allInstCodes) {
                List<SysAccountTemplate> templates = buildTemplates(instCode, accountType, assetRules);
                for (SysAccountTemplate template : templates) {
                    String key = instCode + ":" + accountType + ":" + template.getAssetType();
                    templateMap.put(key, template);
                }
            }
        }

        log.info("基础规则生成完成，已生成 {} 条模板", templateMap.size());

        for (Map<String, Object> instAssetRule : instAssetRules) {
            String instCode = (String) instAssetRule.get("instCode");
            String accountType = (String) instAssetRule.get("accountType");
            String assetType = (String) instAssetRule.get("assetType");
            String assetName = (String) instAssetRule.get("assetName");
            Boolean required = (Boolean) instAssetRule.get("required");
            Boolean enabled = (Boolean) instAssetRule.get("enabled");
            String iconUrl = (String) instAssetRule.get("iconUrl");

            if (!allInstCodes.contains(instCode)) {
                log.warn("机构 {} 不存在于系统中，跳过", instCode);
                continue;
            }
            
            AssetType assetTypeEnum = AssetType.ofCode(assetType);
            if (assetTypeEnum == null) {
                log.warn("模块类型 {} 不存在于枚举中，跳过", assetType);
                continue;
            }
            
            String key = instCode + ":" + accountType + ":" + assetType;
            
            SysAccountTemplate template = new SysAccountTemplate();
            template.setInstCode(instCode);
            template.setAccountType(accountType);
            template.setAssetType(assetType);
            template.setAssetName(assetName);
            template.setIsRequired(required != null ? required : false);
            template.setIsEnabled(enabled != null ? enabled : true);
            template.setIconUrl(iconUrl != null ? iconUrl : assetTypeEnum.getIconUrl());
            template.setSortOrder(assetTypeEnum.getOrder());
            template.setCanPay(assetTypeEnum.isCanPay());

            templateMap.put(key, template);
        }

        log.info("机构特殊规则合并完成，共 {} 条模板", templateMap.size());

        log.info("开始删除旧数据...");
        accountTemplateRepository.deleteAllInBatch();
        log.info("旧数据删除完成");
        
        List<SysAccountTemplate> allTemplates = new ArrayList<>(templateMap.values());
        int batchSize = 200;
        for (int i = 0; i < allTemplates.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allTemplates.size());
            accountTemplateRepository.saveAll(allTemplates.subList(i, end));
            log.info("已保存 {}-{} 条模板", i + 1, end);
        }
        
        updateConfigVersion("account_template_data", DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(config)));
        
        log.info("账户模板数据同步完成，共 {} 条记录", allTemplates.size());
    }
    
    @Override
    protected String getLoaderName() {
        return "账户模板数据加载器";
    }
    
    private List<SysAccountTemplate> buildTemplates(String instCode, String accountType, Map<String, Object> assetRules) {
        List<SysAccountTemplate> templates = new ArrayList<>();
        List<Object> requiredAssets = (List<Object>) assetRules.get("required");
        if (requiredAssets != null) {
            for (Object asset : requiredAssets) {
                AssetType assetType = AssetType.ofCode((String) asset);
                if (assetType == null) {
                    log.warn("模块类型 {} 不存在于枚举中，跳过", asset);
                    continue;
                }
                if (!containsAsset(templates, assetType.getCode())) {
                    templates.add(createTemplate(instCode, accountType, assetType, true, true));
                }
            }
        }
        
        List<Object> defaultAssets = (List<Object>) assetRules.get("default");
        if (defaultAssets != null) {
            for (Object asset : defaultAssets) {
                AssetType assetType = AssetType.ofCode((String) asset);
                if (assetType == null) {
                    log.warn("模块类型 {} 不存在于枚举中，跳过", asset);
                    continue;
                }
                if (!containsAsset(templates, assetType.getCode())) {
                    templates.add(createTemplate(instCode, accountType, assetType, false, true));
                }
            }
        }
        
        List<Object> optionalAssets = (List<Object>) assetRules.get("optional");
        if (optionalAssets != null) {
            for (Object asset : optionalAssets) {
                AssetType assetType = AssetType.ofCode((String) asset);
                if (assetType == null) {
                    log.warn("模块类型 {} 不存在于枚举中，跳过", asset);
                    continue;
                }
                if (!containsAsset(templates, assetType.getCode())) {
                    templates.add(createTemplate(instCode, accountType, assetType, false, false));
                }
            }
        }
        
        return templates;
    }
    
    private boolean containsAsset(List<SysAccountTemplate> templates, String assetType) {
        return templates.stream().anyMatch(t -> t.getAssetType().equals(assetType));
    }
    
    private SysAccountTemplate createTemplate(String instCode, String accountType, AssetType assetTypeEnum, boolean required, boolean enabled) {
        SysAccountTemplate template = new SysAccountTemplate();
        template.setInstCode(instCode);
        template.setAccountType(accountType);
        template.setAssetType(assetTypeEnum.getCode());
        template.setAssetName(assetTypeEnum.getName());
        template.setIconUrl(assetTypeEnum.getIconUrl());
        template.setCanPay(assetTypeEnum.isCanPay());
        template.setIsRequired(required);
        template.setIsEnabled(enabled);
        template.setSortOrder(assetTypeEnum.getOrder());
        return template;
    }
}