package com.suiyou.service;

import com.suiyou.dto.account.AccountTemplateRespDTO;
import com.suiyou.dto.account.InstRespDTO;
import com.suiyou.dto.account.InstTypeRespDTO;

import java.util.List;

public interface SysInstitutionService {
    List<InstRespDTO> getAllInstitutions();
    List<InstRespDTO> getInstitutionsByType(String typeCode);
    List<InstRespDTO> getHotInstitutions();
    InstRespDTO getInstitutionByCode(String instCode);
    
    List<InstTypeRespDTO> getAllInstitutionTypes();
    InstTypeRespDTO getInstitutionTypeByCode(String typeCode);
    
    List<AccountTemplateRespDTO> getAccountTemplates(String instCode, String accountType);
}