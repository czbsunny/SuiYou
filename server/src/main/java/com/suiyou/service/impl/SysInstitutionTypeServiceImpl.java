
package com.suiyou.service.impl;

import com.suiyou.dto.account.InstitutionTypeInitDTO;
import com.suiyou.dto.account.InstitutionTypeRespDTO;
import com.suiyou.model.SysInstitutionType;
import com.suiyou.repository.SysInstitutionTypeRepository;
import com.suiyou.service.SysInstitutionTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suiyou.model.enums.AccountType;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysInstitutionTypeServiceImpl implements SysInstitutionTypeService {
    @Autowired
    private SysInstitutionTypeRepository institutionTypeRepository;

    @Override
    public List<InstitutionTypeRespDTO> getAllInstitutionTypes() {
        return institutionTypeRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toInstitutionTypeRespDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InstitutionTypeRespDTO getInstitutionTypeByCode(String typeCode) {
        return institutionTypeRepository.findByTypeCode(typeCode)
                .map(this::toInstitutionTypeRespDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public void initInstitutionTypes(List<InstitutionTypeInitDTO> dtos) {
        log.info("开始初始化机构类型，共 {} 条", dtos.size());
        
        for (InstitutionTypeInitDTO dto : dtos) {
            SysInstitutionType type = institutionTypeRepository.findByTypeCode(dto.getTypeCode()).orElse(null);
            if (type == null) {
                type = new SysInstitutionType();
            }
            
            type.setTypeCode(dto.getTypeCode());
            type.setTypeName(dto.getTypeName());
            type.setDescription(dto.getDescription());
            type.setSortOrder(dto.getSortOrder());
            type.setIconUrl(dto.getIconUrl());
            type.setThemeColor(dto.getThemeColor());
            
            institutionTypeRepository.save(type);
        }
        
        log.info("机构类型初始化完成");
    }

    private InstitutionTypeRespDTO toInstitutionTypeRespDTO(SysInstitutionType entity) {
        List<String> accountTypes = AccountType.getByInstitutionType(entity.getTypeCode())
                .stream()
                .map(at -> at.name() + ":" + at.getDescription())
                .collect(Collectors.toList());
        return InstitutionTypeRespDTO.builder()
                .id(entity.getId())
                .typeCode(entity.getTypeCode())
                .typeName(entity.getTypeName())
                .description(entity.getDescription())
                .sortOrder(entity.getSortOrder())
                .iconUrl(entity.getIconUrl())
                .themeColor(entity.getThemeColor())
                .accountTypes(accountTypes)
                .build();
    }
}
