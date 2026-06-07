package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import com.suiyou.dto.account.InstitutionInitDTO;
import com.suiyou.service.SysAssetConfigService;

import java.util.List;

@Component
@Order(2)
@Slf4j
public class InstitutionDataLoader extends AbstractConfigLoader {

    @Autowired
    private SysAssetConfigService assetConfigService;
    
    @Value("classpath:sys_institution_init.json")
    private Resource jsonResource;
    
    @Override
    @Transactional
    protected void loadConfig() throws Exception {
        log.info("开始同步金融机构数据...");

        // 解析JSON数据
        if (!jsonResource.exists()) {
            log.warn("金融机构配置文件不存在，跳过加载");
            return;
        }

        // 1. 读取配置
        List<InstitutionInitDTO> dtos = objectMapper.readValue(
            jsonResource.getInputStream().readAllBytes(), 
            new TypeReference<List<InstitutionInitDTO>>() {}
        );
        
        // 2. 初始化金融机构
        assetConfigService.initInstitutions(dtos);
        
        // 3. 更新配置版本
        updateConfigVersion("institution_data", DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(dtos)));
        
        log.info("金融机构数据同步完成。");
    }
}