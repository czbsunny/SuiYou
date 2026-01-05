package com.suiyou.service;

import com.suiyou.dto.asset.CreateAssetDTO;
import com.suiyou.dto.asset.UpdateAssetDTO;
import com.suiyou.model.Asset;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface AssetService {
    /**
     * 创建资产
     * @param assetDTO 资产实体
     * @return 创建的资产
     */
    Asset createAsset(CreateAssetDTO assetDTO, Long userId);
    
    /**
     * 更新资产
     * @param assetDTO 资产实体
     * @return 更新的资产
     */
    Asset updateAsset(UpdateAssetDTO assetDTO, Long userId);
    
    /**
     * 根据ID获取资产
     * @param id 资产ID
     * @return 资产
     */
    Asset getAssetById(Long id);
    
    /**
     * 根据账户ID获取所有资产
     * @param accountId 账户ID
     * @return 资产列表
     */
    List<Asset> getAssetsByAccountId(Long accountId);
    
    /**
     * 根据用户ID获取所有资产
     * @param userId 用户ID
     * @return 资产列表
     */
    List<Asset> getAssetsByUserId(Long userId);

    /**
     * 根据账户ID和状态获取资产
     * @param accountId 账户ID
     * @param status 状态
     * @return 资产列表
     */
    List<Asset> getAssetsByAccountIdAndStatus(Long accountId, Integer status);
    
    /**
     * 根据账户ID和分类获取资产
     * @param accountId 账户ID
     * @param category 分类
     * @return 资产列表
     */
    List<Asset> getAssetsByAccountIdAndCategory(Long accountId, String category);
    
    /**
     * 根据账户ID和组类型获取资产
     * @param accountId 账户ID
     * @param groupType 组类型
     * @return 资产列表
     */
    List<Asset> getAssetsByAccountIdAndGroupType(Long accountId, String groupType);
    
    /**
     * 删除资产（软删除）
     * @param id 资产ID
     * @return 是否删除成功
     */
    boolean deleteAsset(Long id);
}
