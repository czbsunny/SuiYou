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
    /**
     * 获取资产分类树
     */
    public List<AssetCategoryRespDTO> getAssetCategoryTree();

    /**
     * 获取所有资产机构
     */
    public List<InstitutionRespDTO> getAllInstitutions();
    
    /**
     * 获取资产分类与机构的关联关系
     */
    public List<CategoryInstitutionRelationRespDTO> getCategoryInstitutionRelations();

    /**
     * 获取收支分类树
     */
    public List<CategoryRespDTO> getTransferCategoryTree();

    /**
     * 初始化资产分类
     */
    public void initCategories(List<CategoryInitDTO> dtos);

    /**
     * 初始化资产机构
     */
    public void initInstitutions(List<InstitutionInitDTO> dtos);

    /**
     * 初始化资产分类与机构的关联关系
     */
    public void initCategoryInstitutionRelations(RelationRuleConfigDTO config);

    /**
     * 初始化收支分类
     */
    public void initAssetCategories(List<CategoryInitDTO> dtos);
}
