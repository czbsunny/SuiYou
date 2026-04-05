package com.suiyou.service.impl;

import com.suiyou.dto.portfolio.CreatePortfolioHoldingDTO;
import com.suiyou.dto.portfolio.PortfolioHoldingRespDTO;
import com.suiyou.dto.portfolio.CreatePortfolioHoldingsDTO;
import com.suiyou.model.Portfolio;
import com.suiyou.model.PortfolioHolding;
import com.suiyou.model.enums.PortfolioType;
import com.suiyou.repository.PortfolioHoldingRepository;
import com.suiyou.repository.PortfolioRepository;
import com.suiyou.service.DataServerClient;
import com.suiyou.service.PortfolioHoldingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PortfolioHoldingServiceImpl implements PortfolioHoldingService {

    @Autowired
    private PortfolioHoldingRepository portfolioHoldingRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private DataServerClient dataServerClient;

    @Override
    @Transactional
    public PortfolioHolding createPortfolioHolding(CreatePortfolioHoldingDTO createPortfolioHoldingDTO, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("组合不存在"));

        PortfolioHolding holding = new PortfolioHolding();
        if (portfolio.getType().equals(PortfolioType.FUND)) {
            holding.setPortfolio(portfolio);
            holding.setSymbol(createPortfolioHoldingDTO.getSymbol());
            holding.setName(createPortfolioHoldingDTO.getName());
            holding.setQuantity(createPortfolioHoldingDTO.getQuantity());
            holding.setCost(createPortfolioHoldingDTO.getCost());
            
            // 计算持有金额
            holding.setAmount(createPortfolioHoldingDTO.getQuantity() * createPortfolioHoldingDTO.getCost());
        }

        return portfolioHoldingRepository.save(holding);
    }

    @Override
    public List<PortfolioHoldingRespDTO> getPortfolioHoldingsByPortfolioId(Long portfolioId) {
        return portfolioHoldingRepository.findByPortfolioId(portfolioId).stream()
                .map(PortfolioHoldingRespDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PortfolioHoldingRespDTO getPortfolioHoldingById(Long id) {
        PortfolioHolding holding = portfolioHoldingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("持仓不存在"));
        return PortfolioHoldingRespDTO.fromEntity(holding);
    }

    @Override
    @Transactional
    public PortfolioHolding updatePortfolioHolding(Long id, CreatePortfolioHoldingDTO createPortfolioHoldingDTO) {
        PortfolioHolding holding = portfolioHoldingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("持仓不存在"));

        holding.setSymbol(createPortfolioHoldingDTO.getSymbol());
        holding.setName(createPortfolioHoldingDTO.getName());
        holding.setQuantity(createPortfolioHoldingDTO.getQuantity());
        holding.setCost(createPortfolioHoldingDTO.getCost());
        
        // 重新计算持有金额
        holding.setAmount(createPortfolioHoldingDTO.getQuantity() * createPortfolioHoldingDTO.getCost());

        return portfolioHoldingRepository.save(holding);
    }

    @Override
    @Transactional
    public void createPortfolioHoldings(Long portfolioId, CreatePortfolioHoldingsDTO createPortfolioHoldingsDTO) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("组合不存在"));

        List<String> symbols = createPortfolioHoldingsDTO.getHoldings().stream()
            .map(CreatePortfolioHoldingDTO::getSymbol)
            .collect(Collectors.toList());

        final Map<String, Double> navs = new HashMap<>();
        if (PortfolioType.FUND.equals(portfolio.getType())) {
            navs.putAll(dataServerClient.getFundLatestNavs(symbols));
        } else {
            navs.putAll(dataServerClient.getStockLatestNavs(symbols));
        }
        
        List<PortfolioHolding> entities = createPortfolioHoldingsDTO.getHoldings().stream()
            .map(dto -> convertToEntity(dto, portfolio, navs))
            .collect(Collectors.toList());

        portfolioHoldingRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public boolean deletePortfolioHolding(Long id) {
        PortfolioHolding holding = portfolioHoldingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("持仓不存在"));

        portfolioHoldingRepository.delete(holding);
        return true;
    }

    @Override
    @Transactional
    public boolean clearPortfolioHoldings(Long portfolioId) {
        List<PortfolioHolding> holdings = portfolioHoldingRepository.findByPortfolioId(portfolioId);
        portfolioHoldingRepository.deleteAll(holdings);
        return true;
    }

    private PortfolioHolding convertToEntity(CreatePortfolioHoldingDTO dto, Portfolio portfolio, Map<String, Double> navs) {
        PortfolioHolding holding = new PortfolioHolding();
        holding.setPortfolio(portfolio);
        holding.setSymbol(dto.getSymbol());
        holding.setName(dto.getName());
        double currentNav = navs.get(dto.getSymbol());

        if (PortfolioType.FUND.equals(portfolio.getType())) {
            holding.setQuantity(dto.getAmount() / currentNav);
            holding.setCost((dto.getAmount() - dto.getReturnValue()) / dto.getAmount());
            holding.setAmount(dto.getAmount());  
            holding.setReturnValue(dto.getReturnValue());
            holding.setReturnRate(dto.getReturnValue() / dto.getAmount());
            holding.setDailyReturn(0.0);
            holding.setDailyReturnRate(0.0);
        } else {
            holding.setQuantity(dto.getQuantity());
            holding.setCost(dto.getCost());
            holding.setAmount(dto.getQuantity() * currentNav);
            holding.setReturnValue((currentNav - dto.getCost()) * dto.getQuantity());
            holding.setReturnRate((currentNav - dto.getCost()) / dto.getCost());
            holding.setDailyReturn(0.0);
            holding.setDailyReturnRate(0.0);
        }

        return holding;
    }
}