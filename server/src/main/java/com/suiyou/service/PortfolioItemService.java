package com.suiyou.service;

import com.suiyou.dto.portfolio.CreatePortfolioItemDTO;
import com.suiyou.dto.portfolio.PortfolioItemRespDTO;
import com.suiyou.model.PortfolioItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PortfolioItemService {
    /**
     * 创建组合持仓
     * @param createPortfolioItemDTO 持仓创建DTO
     * @param portfolioId 组合ID
     * @return 创建的持仓
     */
    PortfolioItem createPortfolioItem(CreatePortfolioItemDTO createPortfolioItemDTO, Long portfolioId);

    /**
     * 获取组合的所有持仓
     * @param portfolioId 组合ID
     * @return 持仓列表
     */
    List<PortfolioItemRespDTO> getPortfolioItemsByPortfolioId(Long portfolioId);

    /**
     * 根据ID获取持仓
     * @param id 持仓ID
     * @return 持仓
     */
    PortfolioItemRespDTO getPortfolioItemById(Long id);

    /**
     * 更新持仓
     * @param id 持仓ID
     * @param createPortfolioItemDTO 持仓更新DTO
     * @return 更新后的持仓
     */
    PortfolioItem updatePortfolioItem(Long id, CreatePortfolioItemDTO createPortfolioItemDTO);

    /**
     * 删除持仓
     * @param id 持仓ID
     * @return 操作结果
     */
    boolean deletePortfolioItem(Long id);
}