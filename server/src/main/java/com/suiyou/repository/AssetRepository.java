package com.suiyou.repository;

import com.suiyou.model.Asset;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    /**
     * 根据账户ID查询所有资产
     * @param accountId 账户ID
     * @return 资产列表
     */
    List<Asset> findByAccountId(Long accountId);
    
    /**
     * 根据账户ID和状态查询资产
     * @param accountId 账户ID
     * @param status 状态
     * @return 资产列表
     */
    List<Asset> findByAccountIdAndStatus(Long accountId, Integer status);
    
    /**
     * 根据账户ID和分类查询资产
     * @param accountId 账户ID
     * @param category 分类
     * @return 资产列表
     */
    List<Asset> findByAccountIdAndCategory(Long accountId, String category);
    
    /**
     * 根据账户ID和组类型查询资产
     * @param accountId 账户ID
     * @param groupType 组类型
     * @return 资产列表
     */
    List<Asset> findByAccountIdAndGroupType(Long accountId, String groupType);

    /**
     * 根据用户ID和状态查询资产
     * @param userId 用户ID
     * @param status 状态
     * @return 资产列表
     */
    List<Asset> findByOwnerIdAndStatus(Long userId, Integer status);
}
