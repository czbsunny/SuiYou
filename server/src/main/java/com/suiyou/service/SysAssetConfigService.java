package com.suiyou.service;

import com.suiyou.dto.account.AssetCategoryRespDTO;
import com.suiyou.dto.account.CategoryInitDTO;
import com.suiyou.dto.account.InstitutionInitDTO;

import java.util.List;

public interface SysAssetConfigService {
    /**
     * 获取资产分类树
     */
    public List<AssetCategoryRespDTO> getCategoryTree();

    /**
     * 供 DataLoader 使用的方法 (之前提到的)
     */
    public void initCategories(List<CategoryInitDTO> dtos);

    /**
     * 供 DataLoader 使用的方法 (之前提到的)
     */
    public void initInstitutions(List<InstitutionInitDTO> dtos);
}
