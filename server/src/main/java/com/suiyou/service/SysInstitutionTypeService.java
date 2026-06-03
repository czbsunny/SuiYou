
package com.suiyou.service;

import com.suiyou.dto.account.InstitutionTypeInitDTO;
import com.suiyou.dto.account.InstitutionTypeRespDTO;

import java.util.List;

public interface SysInstitutionTypeService {
    /**
     * 获取所有机构类型
     */
    List<InstitutionTypeRespDTO> getAllInstitutionTypes();

    /**
     * 根据类型代码获取机构类型
     */
    InstitutionTypeRespDTO getInstitutionTypeByCode(String typeCode);

    /**
     * 初始化机构类型
     */
    void initInstitutionTypes(List<InstitutionTypeInitDTO> dtos);
}
