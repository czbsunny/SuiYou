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
import com.suiyou.dto.account.InstitutionInitDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Order(2)
@Slf4j
public class InstitutionDataLoader extends AbstractConfigLoader {

    @Autowired
    private SysInstitutionRepository repository;
    
    @Value("classpath:sys_institution_init.json")
    private Resource jsonResource;
    
    @Override
    @Transactional
    protected void loadConfig() throws Exception {
        log.info("开始同步金融机构数据...");

        // 解析JSON数据
        List<InstitutionInitDTO> dtos = objectMapper.readValue(
            jsonResource.getInputStream().readAllBytes(), 
            new TypeReference<List<InstitutionInitDTO>>() {}
        );
        
        // 同步数据
        syncInstitutions(dtos);
        
        // 更新配置版本
        updateConfigVersion("institution_data", DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(dtos)));
        
        log.info("金融机构数据同步完成。");
    }

    /**
     * 同步金融机构数据
     */
    private void syncInstitutions(List<InstitutionInitDTO> dtos) {
        log.info("正在同步 [金融机构] 数据...");

        List<String> expectedCodes = new ArrayList<>();
        List<SysInstitution> entitiesToSave = new ArrayList<>();

        Set<String> instCodeSet = new java.util.HashSet<>();
        for (InstitutionInitDTO dto : dtos) {
            if (!instCodeSet.add(dto.getInstCode())) {
                log.warn("发现重复的instCode: {}", dto.getInstCode());
                continue;
            }
            expectedCodes.add(dto.getInstCode());
            entitiesToSave.add(processInstitution(dto));
        }

        // 保存所有更新或新增的机构
        repository.saveAll(entitiesToSave);
    }

    /**
     * 处理单个金融机构：如果存在则更新，不存在则创建
     */
    private SysInstitution processInstitution(InstitutionInitDTO dto) {
        // 根据机构code查询现有机构
        SysInstitution existingEntity = repository.findByInstCode(dto.getInstCode());
        
        if (existingEntity != null) {
            // 更新现有机构的属性
            existingEntity.setInstName(dto.getInstName());
            existingEntity.setLogoUrl(dto.getLogoUrl());
            existingEntity.setSortOrder(dto.getSortOrder());
            return existingEntity;
        } else {
            // 创建新机构
            return convert(dto);
        }
    }

    /**
     * 将DTO转换为实体
     */
    private SysInstitution convert(InstitutionInitDTO dto) {
        SysInstitution entity = new SysInstitution();
        entity.setInstCode(dto.getInstCode());
        entity.setInstName(dto.getInstName());
        entity.setLogoUrl(dto.getLogoUrl());
        entity.setSortOrder(dto.getSortOrder());
        return entity;
    }
}