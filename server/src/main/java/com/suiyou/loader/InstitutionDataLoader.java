package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suiyou.model.SysInstitution;
import com.suiyou.repository.SysInstitutionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.Optional;

@Component
@Order(1)
@Slf4j
public class InstitutionDataLoader extends AbstractConfigLoader {

    @Autowired
    private SysInstitutionRepository institutionRepository;
    
    @Value("classpath:sys_institution_init.json")
    private Resource jsonResource;
    
    @Override
    @Transactional
    protected void loadConfig() throws Exception {
        log.info("开始同步金融机构数据...");

        if (!jsonResource.exists()) {
            log.warn("金融机构配置文件不存在，跳过加载");
            return;
        }

        List<SysInstitution> institutions = objectMapper.readValue(
            jsonResource.getInputStream().readAllBytes(), 
            new TypeReference<List<SysInstitution>>() {}
        );
        
        for (SysInstitution dto : institutions) {
            SysInstitution existing = institutionRepository.findByInstCode(dto.getInstCode());
            if (existing != null) {
                existing.setInstName(dto.getInstName());
                existing.setShortName(dto.getShortName());
                existing.setInstType(dto.getInstType());
                existing.setLogoUrl(dto.getLogoUrl());
                existing.setSortOrder(dto.getSortOrder());
                existing.setIsHot(Optional.ofNullable(dto.getIsHot()).orElse(false));
                institutionRepository.save(existing);
            } else {
                institutionRepository.save(dto);
            }
        }
        
        updateConfigVersion("institution_data", DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(institutions)));
        
        log.info("金融机构数据同步完成。");
    }
}