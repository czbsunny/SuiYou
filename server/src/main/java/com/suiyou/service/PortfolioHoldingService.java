package com.suiyou.service;

import com.suiyou.dto.portfolio.CreatePortfolioHoldingDTO;
import com.suiyou.dto.portfolio.PortfolioHoldingRespDTO;
import com.suiyou.dto.portfolio.UpdatePortfolioHoldingsDTO;
import com.suiyou.model.PortfolioHolding;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PortfolioHoldingService {
    /**
     * 创建组合持仓
     * @param createPortfolioHoldingDTO 持仓创建DTO
     * @param portfolioId 组合ID
     * @return 创建的持仓
     */
    PortfolioHolding createPortfolioHolding(CreatePortfolioHoldingDTO createPortfolioHoldingDTO, Long portfolioId);

    /**
     * 获取组合的所有持仓
     * @param portfolioId 组合ID
     * @return 持仓列表
     */
    List<PortfolioHoldingRespDTO> getPortfolioHoldingsByPortfolioId(Long portfolioId);

    /**
     * 根据ID获取持仓
     * @param id 持仓ID
     * @return 持仓
     */
    PortfolioHoldingRespDTO getPortfolioHoldingById(Long id);

    /**
     * 更新持仓
     * @param id 持仓ID
     * @param createPortfolioHoldingDTO 持仓更新DTO
     * @return 更新后的持仓
     */
    PortfolioHolding updatePortfolioHolding(Long id, CreatePortfolioHoldingDTO createPortfolioHoldingDTO);

    /**
     * 更新组合持仓
     * @param id 组合ID
     * @param updatePortfolioHoldingsDTO 持仓更新DTO
     * @return 操作结果
     */
    void updatePortfolioHoldings(Long id, UpdatePortfolioHoldingsDTO updatePortfolioHoldingsDTO);

    /**
     * 删除持仓
     * @param id 持仓ID
     * @return 操作结果
     */
    boolean deletePortfolioHolding(Long id);

    /**
     * 清空组合持仓
     * @param portfolioId 组合ID
     * @return 操作结果
     */
    boolean clearPortfolioHoldings(Long portfolioId);
}