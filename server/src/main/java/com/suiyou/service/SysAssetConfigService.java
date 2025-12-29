package com.suiyou.service;

import com.suiyou.dto.account.AssetCategoryRespDTO;
import com.suiyou.dto.account.CategoryInitDTO;
import com.suiyou.dto.account.InstitutionInitDTO;
import com.suiyou.dto.account.InstitutionRespDTO;
import com.suiyou.dto.account.RelationRuleConfigDTO;

import java.util.List;

public interface SysAssetConfigService {
    /**
     * 获取资产分类树
     */
    public List<AssetCategoryRespDTO> getCategoryTree();

    /**
     * 获取所有资产机构
     */
    public List<InstitutionRespDTO> getAllInstitutions();
    
    /**
     * 供 DataLoader 使用的方法 (之前提到的)
     */
    public void initCategories(List<CategoryInitDTO> dtos);

    /**
     * 供 DataLoader 使用的方法 (之前提到的)
     */
    public void initInstitutions(List<InstitutionInitDTO> dtos);

    /**
     * 初始化资产分类与机构的关联关系
     */
    public void initCategoryInstitutionRelations(RelationRuleConfigDTO config);
}
