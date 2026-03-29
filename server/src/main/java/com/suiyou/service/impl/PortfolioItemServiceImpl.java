package com.suiyou.service.impl;

import com.suiyou.dto.portfolio.CreatePortfolioItemDTO;
import com.suiyou.dto.portfolio.PortfolioItemRespDTO;
import com.suiyou.model.Portfolio;
import com.suiyou.model.PortfolioItem;
import com.suiyou.repository.PortfolioItemRepository;
import com.suiyou.repository.PortfolioRepository;
import com.suiyou.service.PortfolioItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioItemServiceImpl implements PortfolioItemService {

    @Autowired
    private PortfolioItemRepository portfolioItemRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Override
    @Transactional
    public PortfolioItem createPortfolioItem(CreatePortfolioItemDTO createPortfolioItemDTO, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("组合不存在"));

        PortfolioItem item = new PortfolioItem();
        item.setPortfolio(portfolio);
        item.setSymbol(createPortfolioItemDTO.getSymbol());
        item.setName(createPortfolioItemDTO.getName());
        item.setQuantity(createPortfolioItemDTO.getQuantity());
        item.setCost(createPortfolioItemDTO.getCost());
        
        // 计算持有金额
        item.setAmount(createPortfolioItemDTO.getQuantity() * createPortfolioItemDTO.getCost());

        return portfolioItemRepository.save(item);
    }

    @Override
    public List<PortfolioItemRespDTO> getPortfolioItemsByPortfolioId(Long portfolioId) {
        return portfolioItemRepository.findByPortfolioId(portfolioId).stream()
                .map(PortfolioItemRespDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PortfolioItemRespDTO getPortfolioItemById(Long id) {
        PortfolioItem item = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("持仓不存在"));
        return PortfolioItemRespDTO.fromEntity(item);
    }

    @Override
    @Transactional
    public PortfolioItem updatePortfolioItem(Long id, CreatePortfolioItemDTO createPortfolioItemDTO) {
        PortfolioItem item = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("持仓不存在"));

        item.setSymbol(createPortfolioItemDTO.getSymbol());
        item.setName(createPortfolioItemDTO.getName());
        item.setQuantity(createPortfolioItemDTO.getQuantity());
        item.setCost(createPortfolioItemDTO.getCost());
        
        // 重新计算持有金额
        item.setAmount(createPortfolioItemDTO.getQuantity() * createPortfolioItemDTO.getCost());

        return portfolioItemRepository.save(item);
    }

    @Override
    @Transactional
    public boolean deletePortfolioItem(Long id) {
        PortfolioItem item = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("持仓不存在"));

        portfolioItemRepository.delete(item);
        return true;
    }
}