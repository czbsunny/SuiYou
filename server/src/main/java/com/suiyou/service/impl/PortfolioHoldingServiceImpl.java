package com.suiyou.service.impl;

import com.suiyou.dto.portfolio.CreatePortfolioHoldingDTO;
import com.suiyou.dto.portfolio.PortfolioHoldingRespDTO;
import com.suiyou.dto.portfolio.UpdatePortfolioHoldingsDTO;
import com.suiyou.model.Portfolio;
import com.suiyou.model.PortfolioHolding;
import com.suiyou.repository.PortfolioHoldingRepository;
import com.suiyou.repository.PortfolioRepository;
import com.suiyou.service.PortfolioHoldingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioHoldingServiceImpl implements PortfolioHoldingService {

    @Autowired
    private PortfolioHoldingRepository portfolioItemRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Override
    @Transactional
    public PortfolioHolding createPortfolioHolding(CreatePortfolioHoldingDTO createPortfolioHoldingDTO, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("组合不存在"));

        PortfolioHolding holding = new PortfolioHolding();
        holding.setPortfolio(portfolio);
        holding.setSymbol(createPortfolioHoldingDTO.getSymbol());
        holding.setName(createPortfolioHoldingDTO.getName());
        holding.setQuantity(createPortfolioHoldingDTO.getQuantity());
        holding.setCost(createPortfolioHoldingDTO.getCost());
        
        // 计算持有金额
        holding.setAmount(createPortfolioHoldingDTO.getQuantity() * createPortfolioHoldingDTO.getCost());

        return portfolioItemRepository.save(holding);
    }

    @Override
    public List<PortfolioHoldingRespDTO> getPortfolioHoldingsByPortfolioId(Long portfolioId) {
        return portfolioItemRepository.findByPortfolioId(portfolioId).stream()
                .map(PortfolioHoldingRespDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PortfolioHoldingRespDTO getPortfolioHoldingById(Long id) {
        PortfolioHolding holding = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("持仓不存在"));
        return PortfolioHoldingRespDTO.fromEntity(holding);
    }

    @Override
    @Transactional
    public PortfolioHolding updatePortfolioHolding(Long id, CreatePortfolioHoldingDTO createPortfolioHoldingDTO) {
        PortfolioHolding holding = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("持仓不存在"));

        holding.setSymbol(createPortfolioHoldingDTO.getSymbol());
        holding.setName(createPortfolioHoldingDTO.getName());
        holding.setQuantity(createPortfolioHoldingDTO.getQuantity());
        holding.setCost(createPortfolioHoldingDTO.getCost());
        
        // 重新计算持有金额
        holding.setAmount(createPortfolioHoldingDTO.getQuantity() * createPortfolioHoldingDTO.getCost());

        return portfolioItemRepository.save(holding);
    }

    @Override
    @Transactional
    public void updatePortfolioHoldings(Long portfolioId, UpdatePortfolioHoldingsDTO updatePortfolioHoldingsDTO) {
        List<CreatePortfolioHoldingDTO> holdings = updatePortfolioHoldingsDTO.getHoldings();
        for (CreatePortfolioHoldingDTO holdingDTO : holdings) {
            createPortfolioHolding(holdingDTO, portfolioId);
        }
    }

    @Override
    @Transactional
    public boolean deletePortfolioHolding(Long id) {
        PortfolioHolding holding = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("持仓不存在"));

        portfolioItemRepository.delete(holding);
        return true;
    }

    @Override
    @Transactional
    public boolean clearPortfolioHoldings(Long portfolioId) {
        List<PortfolioHolding> holdings = portfolioItemRepository.findByPortfolioId(portfolioId);
        portfolioItemRepository.deleteAll(holdings);
        return true;
    }
}