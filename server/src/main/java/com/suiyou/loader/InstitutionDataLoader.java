package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suiyou.dto.account.InstitutionInitDTO;
import com.suiyou.model.SysInstitution;
import com.suiyou.repository.SysInstitutionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@Order(2)
@Slf4j
public class InstitutionDataLoader implements CommandLineRunner {

    @Autowired
    private SysInstitutionRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("classpath:sys_institution_init.json") // 你的机构JSON文件
    private Resource jsonResource;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("开始同步金融机构数据...");

        if (!jsonResource.exists()) {
            log.warn("机构初始化文件不存在，跳过加载");
            return;
        }

        List<InstitutionInitDTO> dtos = objectMapper.readValue(
            jsonResource.getInputStream(),
            new TypeReference<List<InstitutionInitDTO>>() {}
        );

        Set<String> instCodeSet = new java.util.HashSet<>();
        for (InstitutionInitDTO dto : dtos) {
            if (!instCodeSet.add(dto.getInstCode())) {
                log.warn("发现重复的instCode: {}", dto.getInstCode());
            }
        }

        int count = 0;
        for (InstitutionInitDTO dto : dtos) {
            SysInstitution existing = repository.findByInstCode(dto.getInstCode());
            if (existing != null) {
                // 更新逻辑
                updateEntity(existing, dto);
                repository.save(existing);
            } else {
                // 新增逻辑
                repository.save(convert(dto));
            }
            count++;
        }

        log.info("金融机构同步完成，共处理 {} 条记录。", count);
    }

    private void updateEntity(SysInstitution entity, InstitutionInitDTO dto) {
        entity.setInstName(dto.getInstName());
        entity.setShortName(dto.getShortName());
        entity.setInstType(dto.getInstType());
        entity.setLogoUrl(dto.getLogoUrl());
        entity.setThemeColor(dto.getThemeColor());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
    }

    private SysInstitution convert(InstitutionInitDTO dto) {
        SysInstitution entity = new SysInstitution();
        entity.setInstCode(dto.getInstCode());
        updateEntity(entity, dto);
        return entity;
    }
}