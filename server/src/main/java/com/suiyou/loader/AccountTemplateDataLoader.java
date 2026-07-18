package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suiyou.enums.ModuleType;
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
        List<Map<String, Object>> instModuleRules = (List<Map<String, Object>>) config.get("instModuleRules");

        List<String> allInstCodes = institutionRepository.findAll().stream()
            .map(inst -> inst.getInstCode())
            .toList();
        
        log.info("发现 {} 个机构，开始生成账户模板...", allInstCodes.size());

        for (Map.Entry<String, Object> typeEntry : typeRules.entrySet()) {
            String accountType = typeEntry.getKey();
            Map<String, Object> moduleRules = (Map<String, Object>) typeEntry.getValue();
            
            for (String instCode : allInstCodes) {
                List<SysAccountTemplate> templates = buildTemplates(instCode, accountType, moduleRules);
                for (SysAccountTemplate template : templates) {
                    String key = instCode + ":" + accountType + ":" + template.getModuleType();
                    templateMap.put(key, template);
                }
            }
        }

        log.info("基础规则生成完成，已生成 {} 条模板", templateMap.size());

        for (Map<String, Object> instModuleRule : instModuleRules) {
            String instCode = (String) instModuleRule.get("instCode");
            String accountType = (String) instModuleRule.get("accountType");
            String moduleType = (String) instModuleRule.get("moduleType");
            String moduleName = (String) instModuleRule.get("moduleName");
            Boolean required = (Boolean) instModuleRule.get("required");
            Boolean enabled = (Boolean) instModuleRule.get("enabled");
            String iconUrl = (String) instModuleRule.get("iconUrl");

            if (!allInstCodes.contains(instCode)) {
                log.warn("机构 {} 不存在于系统中，跳过", instCode);
                continue;
            }
            
            ModuleType moduleTypeEnum = ModuleType.ofCode(moduleType);
            if (moduleTypeEnum == null) {
                log.warn("模块类型 {} 不存在于枚举中，跳过", moduleType);
                continue;
            }
            
            String key = instCode + ":" + accountType + ":" + moduleType;
            
            SysAccountTemplate template = new SysAccountTemplate();
            template.setInstCode(instCode);
            template.setAccountType(accountType);
            template.setModuleType(moduleType);
            template.setModuleName(moduleName);
            template.setRequired(required != null ? required : false);
            template.setEnabled(enabled != null ? enabled : true);
            template.setIconUrl(iconUrl != null ? iconUrl : moduleTypeEnum.getIconUrl());
            template.setSortOrder(moduleTypeEnum.getOrder());
            template.setCanPay(moduleTypeEnum.isCanPay());

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
    
    private List<SysAccountTemplate> buildTemplates(String instCode, String accountType, Map<String, Object> moduleRules) {
        List<SysAccountTemplate> templates = new ArrayList<>();
        List<Object> requiredModules = (List<Object>) moduleRules.get("required");
        if (requiredModules != null) {
            for (Object module : requiredModules) {
                ModuleType moduleType = ModuleType.ofCode((String) module);
                if (moduleType == null) {
                    log.warn("模块类型 {} 不存在于枚举中，跳过", module);
                    continue;
                }
                if (!containsModule(templates, moduleType.getCode())) {
                    templates.add(createTemplate(instCode, accountType, moduleType, true, true));
                }
            }
        }
        
        List<Object> defaultModules = (List<Object>) moduleRules.get("default");
        if (defaultModules != null) {
            for (Object module : defaultModules) {
                ModuleType moduleType = ModuleType.ofCode((String) module);
                if (moduleType == null) {
                    log.warn("模块类型 {} 不存在于枚举中，跳过", module);
                    continue;
                }
                if (!containsModule(templates, moduleType.getCode())) {
                    templates.add(createTemplate(instCode, accountType, moduleType, false, true));
                }
            }
        }
        
        List<Object> optionalModules = (List<Object>) moduleRules.get("optional");
        if (optionalModules != null) {
            for (Object module : optionalModules) {
                ModuleType moduleType = ModuleType.ofCode((String) module);
                if (moduleType == null) {
                    log.warn("模块类型 {} 不存在于枚举中，跳过", module);
                    continue;
                }
                if (!containsModule(templates, moduleType.getCode())) {
                    templates.add(createTemplate(instCode, accountType, moduleType, false, false));
                }
            }
        }
        
        return templates;
    }
    
    private boolean containsModule(List<SysAccountTemplate> templates, String moduleType) {
        return templates.stream().anyMatch(t -> t.getModuleType().equals(moduleType));
    }
    
    private SysAccountTemplate createTemplate(String instCode, String accountType, ModuleType moduleTypeEnum, boolean required, boolean enabled) {
        SysAccountTemplate template = new SysAccountTemplate();
        template.setInstCode(instCode);
        template.setAccountType(accountType);
        template.setModuleType(moduleTypeEnum.getCode());
        template.setModuleName(moduleTypeEnum.getName());
        template.setIconUrl(moduleTypeEnum.getIconUrl());
        template.setCanPay(moduleTypeEnum.isCanPay());
        template.setRequired(required);
        template.setEnabled(enabled);
        template.setSortOrder(moduleTypeEnum.getOrder());
        return template;
    }
}