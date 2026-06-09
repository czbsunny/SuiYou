package com.suiyou.service.impl;

import com.suiyou.dto.account.InstitutionModuleRespDTO;
import com.suiyou.dto.account.InstitutionRespDTO;
import com.suiyou.dto.account.InstitutionTypeRespDTO;
import com.suiyou.enums.InstType;
import com.suiyou.model.SysInstitution;
import com.suiyou.repository.SysInstitutionRepository;
import com.suiyou.service.SysInstitutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysInstitutionServiceImpl implements SysInstitutionService {

    @Autowired
    private SysInstitutionRepository institutionRepository;

    @Override
    public List<InstitutionRespDTO> getAllInstitutions() {
        List<SysInstitution> institutionEntities = institutionRepository.findAllByOrderBySortOrderAsc();
        return institutionEntities.stream()
            .map(this::toInstitutionRespDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<InstitutionRespDTO> getInstitutionsByType(String typeCode) {
        List<SysInstitution> institutionEntities = institutionRepository.findByInstTypeOrderBySortOrderAsc(typeCode);
        return institutionEntities.stream()
                .map(this::toInstitutionRespDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstitutionRespDTO> getHotInstitutions() {
        List<SysInstitution> institutionEntities = institutionRepository.findByIsHotTrueOrderBySortOrderAsc();
        return institutionEntities.stream()
                .map(this::toInstitutionRespDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InstitutionRespDTO getInstitutionByCode(String instCode) {
        SysInstitution entity = institutionRepository.findByInstCode(instCode);
        if (entity == null) {
            return null;
        }
        return toInstitutionRespDTO(entity);
    }

    @Override
    public InstitutionModuleRespDTO getInstitutionModules(String instCode) {
        SysInstitution institution = institutionRepository.findByInstCode(instCode);
        if (institution == null) {
            return InstitutionModuleRespDTO.builder().build();
        }
        
        return InstitutionModuleRespDTO.builder()
                .instCode(institution.getInstCode())
                .instName(institution.getInstName())
                .modules(new ArrayList<>())
                .build();
    }

    @Override
    public List<String> getAccountTypesByInstCode(String instCode) {
        SysInstitution institution = institutionRepository.findByInstCode(instCode);
        if (institution == null) {
            return null;
        }
        return new ArrayList<>();
    }

    @Override
    public List<InstitutionTypeRespDTO> getAllInstitutionTypes() {
        return InstType.all().stream()
                .map(this::toInstitutionTypeRespDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InstitutionTypeRespDTO getInstitutionTypeByCode(String typeCode) {
        InstType instType = InstType.ofCode(typeCode);
        if (instType == null) {
            return null;
        }
        return toInstitutionTypeRespDTO(instType);
    }

    private InstitutionTypeRespDTO toInstitutionTypeRespDTO(InstType instType) {
        return InstitutionTypeRespDTO.builder()
                .typeCode(instType.getCode())
                .typeName(instType.getName())
                .description(instType.getDesc())
                .sortOrder(instType.getOrder())
                .accountTypes(new ArrayList<>())
                .build();
    }

    private InstitutionRespDTO toInstitutionRespDTO(SysInstitution entity) {
        InstitutionTypeRespDTO typeDto = null;
        if (entity.getInstType() != null) {
            InstType instType = InstType.ofCode(entity.getInstType());
            if (instType != null) {
                typeDto = toInstitutionTypeRespDTO(instType);
            }
        }

        return InstitutionRespDTO.builder()
                .id(entity.getId())
                .instCode(entity.getInstCode())
                .instName(entity.getInstName())
                .shortName(entity.getShortName())
                .instType(entity.getInstType())
                .logoUrl(entity.getLogoUrl())
                .indexLetter(entity.getIndexLetter())
                .isHot(entity.getIsHot())
                .institutionType(typeDto)
                .build();
    }
}