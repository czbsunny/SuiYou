package com.suiyou.service;

import com.suiyou.dto.asset.CreateAssetDTO;
import com.suiyou.dto.asset.UpdateAssetDTO;
import com.suiyou.dto.asset.AssetRespDTO;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface AssetService {
    /**
     * 创建资产
     * @param assetDTO 资产实体
     * @return 创建的资产
     */
    AssetRespDTO createAsset(CreateAssetDTO assetDTO, Long userId);
    
    /**
     * 更新资产
     * @param assetDTO 资产实体
     * @return 更新的资产
     */
    AssetRespDTO updateAsset(UpdateAssetDTO assetDTO, Long userId);
    
    /**
     * 根据ID获取资产
     * @param id 资产ID
     * @return 资产
     */
    AssetRespDTO getAssetById(Long id);
    
    /**
     * 根据账户ID获取所有资产
     * @param accountId 账户ID
     * @return 资产列表
     */
    List<AssetRespDTO> getAssetsByAccountId(Long accountId);
    
    /**
     * 根据用户ID获取所有资产
     * @param userId 用户ID
     * @return 资产列表
     */
    List<AssetRespDTO> getAssetsByUserId(Long userId);

    /**
     * 删除资产（软删除）
     * @param id 资产ID
     * @return 是否删除成功
     */
    boolean deleteAsset(Long id);
}
