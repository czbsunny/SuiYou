package com.suiyou.service;

import com.suiyou.dto.portfolio.CreatePortfolioDTO;
import com.suiyou.dto.portfolio.PortfolioRespDTO;
import com.suiyou.model.Portfolio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PortfolioService {
    /**
     * 创建组合
     * @param createPortfolioDTO 组合创建DTO
     * @param userId 用户ID
     * @return 创建的组合
     */
    Portfolio createPortfolio(CreatePortfolioDTO createPortfolioDTO, Long userId);

    /**
     * 获取用户的所有组合
     * @param userId 用户ID
     * @return 组合列表
     */
    List<PortfolioRespDTO> getPortfoliosByUserId(Long userId);

    /**
     * 根据ID获取组合
     * @param id 组合ID
     * @return 组合
     */
    PortfolioRespDTO getPortfolioById(Long id);

    /**
     * 根据资产ID获取组合
     * @param assetId 资产ID
     * @return 组合
     */
    PortfolioRespDTO getPortfolioByAssetId(Long assetId);

    /**
     * 删除组合
     * @param id 组合ID
     * @param userId 用户ID
     * @return 操作结果
     */
    boolean deletePortfolio(Long id, Long userId);
}