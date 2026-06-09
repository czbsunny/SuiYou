package com.suiyou.service;

import com.suiyou.dto.account.InstitutionModuleRespDTO;
import com.suiyou.dto.account.InstitutionRespDTO;
import com.suiyou.dto.account.InstitutionTypeRespDTO;

import java.util.List;

public interface SysInstitutionService {
    List<InstitutionRespDTO> getAllInstitutions();
    List<InstitutionRespDTO> getInstitutionsByType(String typeCode);
    List<InstitutionRespDTO> getHotInstitutions();
    InstitutionRespDTO getInstitutionByCode(String instCode);
    InstitutionModuleRespDTO getInstitutionModules(String instCode);
    List<String> getAccountTypesByInstCode(String instCode);
    
    List<InstitutionTypeRespDTO> getAllInstitutionTypes();
    InstitutionTypeRespDTO getInstitutionTypeByCode(String typeCode);
}