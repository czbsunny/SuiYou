package com.suiyou.service.impl;

import com.suiyou.dto.account.AccountTemplateRespDTO;
import com.suiyou.dto.account.AccountTypeDTO;
import com.suiyou.dto.account.InstRespDTO;
import com.suiyou.dto.account.InstTypeRespDTO;
import com.suiyou.enums.AccountType;
import com.suiyou.enums.InstType;
import com.suiyou.model.SysAccountTemplate;
import com.suiyou.model.SysInstitution;
import com.suiyou.repository.SysAccountTemplateRepository;
import com.suiyou.repository.SysInstitutionRepository;
import com.suiyou.service.SysInstitutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysInstitutionServiceImpl implements SysInstitutionService {

    @Autowired
    private SysInstitutionRepository institutionRepository;
    
    @Autowired
    private SysAccountTemplateRepository accountTemplateRepository;

    @Override
    public List<InstRespDTO> getAllInstitutions() {
        List<SysInstitution> institutionEntities = institutionRepository.findAllByOrderBySortOrderAsc();
        return institutionEntities.stream()
            .map(this::toInstRespDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<InstRespDTO> getInstitutionsByType(String typeCode) {
        List<SysInstitution> institutionEntities = institutionRepository.findByInstTypeOrderBySortOrderAsc(typeCode);
        return institutionEntities.stream()
                .map(this::toInstRespDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstRespDTO> getHotInstitutions() {
        List<SysInstitution> institutionEntities = institutionRepository.findByIsHotTrueOrderBySortOrderAsc();
        return institutionEntities.stream()
                .map(this::toInstRespDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InstRespDTO getInstitutionByCode(String instCode) {
        SysInstitution entity = institutionRepository.findByInstCode(instCode);
        if (entity == null) {
            return null;
        }
        return toInstRespDTO(entity);
    }

    @Override
    public List<InstTypeRespDTO> getAllInstitutionTypes() {
        return InstType.all().stream()
                .map(this::toInstitutionTypeRespDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InstTypeRespDTO getInstitutionTypeByCode(String typeCode) {
        InstType instType = InstType.ofCode(typeCode);
        if (instType == null) {
            return null;
        }
        return toInstitutionTypeRespDTO(instType);
    }

    private InstTypeRespDTO toInstitutionTypeRespDTO(InstType instType) {
        return InstTypeRespDTO.builder()
                .typeCode(instType.getCode())
                .typeName(instType.getName())
                .build();
    }

    private InstRespDTO toInstRespDTO(SysInstitution entity) {
        InstRespDTO resp = InstRespDTO.builder()
                .instCode(entity.getInstCode())
                .instName(entity.getInstName())
                .shortName(entity.getShortName())
                .instType(entity.getInstType())
                .logoUrl(entity.getLogoUrl())
                .indexLetter(entity.getIndexLetter())
                .isHot(entity.getIsHot())
                .build();

        if (entity.getInstType() != null) {
            InstType instType = InstType.ofCode(entity.getInstType());
            if (instType != null) {
                resp.setInstTypeName(instType.getName());
            }
        }

        resp.setAccountTypes(getAccountTypes(entity.getInstCode(), entity.getInstType()));
        return resp;
    }

    private List<AccountTypeDTO> getAccountTypes(String instCode, String instType) {
        return AccountType.all().stream()
                .filter(at -> at.supportsInstitution(instCode, instType))
                .map(at -> AccountTypeDTO.builder()
                        .accountType(at.getCode())
                        .accountTypeName(at.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountTemplateRespDTO> getAccountTemplates(String instCode, String accountType) {
        List<SysAccountTemplate> templates = accountTemplateRepository.findByInstCodeAndAccountType(instCode, accountType);
        return templates.stream()
                .map(t -> AccountTemplateRespDTO.builder()
                        .moduleType(t.getModuleType())
                        .moduleName(t.getModuleName())
                        .iconUrl(t.getIconUrl())
                        .required(t.getRequired())
                        .enabled(t.getEnabled())
                        .sortOrder(t.getSortOrder())
                        .build())
                .collect(Collectors.toList());
    }
}