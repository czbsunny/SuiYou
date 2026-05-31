
package com.suiyou.service;

import com.suiyou.dto.CategoryRespDTO;
import com.suiyou.dto.account.AssetCategoryRespDTO;
import com.suiyou.dto.account.CategoryInitDTO;
import com.suiyou.dto.account.InstitutionInitDTO;
import com.suiyou.dto.account.InstitutionRespDTO;
import com.suiyou.dto.account.RelationRuleConfigDTO;
import com.suiyou.dto.account.CategoryInstitutionRelationRespDTO;

import java.util.List;

public interface SysAssetConfigService {
    List<AssetCategoryRespDTO> getAssetCategoryTree();
    List<InstitutionRespDTO> getAllInstitutions();
    List<InstitutionRespDTO> getInstitutionsByType(String typeCode);
    List<InstitutionRespDTO> getHotInstitutions();
    InstitutionRespDTO getInstitutionByCode(String instCode);
    List<CategoryInstitutionRelationRespDTO> getCategoryInstitutionRelations();
    List<CategoryRespDTO> getTransferCategoryTree();
    void initCategories(List<CategoryInitDTO> dtos);
    void initInstitutions(List<InstitutionInitDTO> dtos);
    void initCategoryInstitutionRelations(RelationRuleConfigDTO config);
    void initAssetCategories(List<CategoryInitDTO> dtos);
}
