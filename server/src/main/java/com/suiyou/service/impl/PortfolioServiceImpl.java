package com.suiyou.service.impl;

import com.suiyou.dto.portfolio.CreatePortfolioDTO;
import com.suiyou.dto.portfolio.PortfolioHoldingRespDTO;
import com.suiyou.dto.portfolio.PortfolioRespDTO;
import com.suiyou.model.Asset;
import com.suiyou.model.Portfolio;
import com.suiyou.model.PortfolioHolding;
import com.suiyou.model.User;
import com.suiyou.repository.AssetRepository;
import com.suiyou.repository.PortfolioHoldingRepository;
import com.suiyou.repository.PortfolioRepository;
import com.suiyou.repository.UserRepository;
import com.suiyou.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private PortfolioHoldingRepository portfolioHoldingRepository;

    @Override
    @Transactional
    public Portfolio createPortfolio(CreatePortfolioDTO createPortfolioDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        Asset asset = assetRepository.findById(createPortfolioDTO.getAssetId())
                .orElseThrow(() -> new IllegalArgumentException("资产不存在"));

        Portfolio portfolio = new Portfolio();
        portfolio.setUser(user);
        portfolio.setAsset(asset);
        portfolio.setName(createPortfolioDTO.getName());
        portfolio.setDescription(createPortfolioDTO.getDescription());

        return portfolioRepository.save(portfolio);
    }

    @Override
    public List<PortfolioRespDTO> getPortfoliosByUserId(Long userId) {
        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);
        return portfolios.stream()
                .map(portfolio -> {
                    PortfolioRespDTO dto = PortfolioRespDTO.fromEntity(portfolio);
                    List<PortfolioHolding> holdings = portfolioHoldingRepository.findByPortfolioId(portfolio.getId());
                    dto.setItems(holdings.stream()
                            .map(PortfolioHoldingRespDTO::fromEntity)
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PortfolioRespDTO getPortfolioById(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("组合不存在"));

        PortfolioRespDTO dto = PortfolioRespDTO.fromEntity(portfolio);
        List<PortfolioHolding> holdings = portfolioHoldingRepository.findByPortfolioId(portfolio.getId());
        dto.setItems(holdings.stream()
                .map(PortfolioHoldingRespDTO::fromEntity)
                .collect(Collectors.toList()));
        return dto;
    }

    @Override
    public PortfolioRespDTO getPortfolioByAssetId(Long assetId) {
        Portfolio portfolio = portfolioRepository.findByAssetId(assetId)
                .orElseThrow(() -> new IllegalArgumentException("资产对应的组合不存在"));
 
        PortfolioRespDTO dto = PortfolioRespDTO.fromEntity(portfolio);
        List<PortfolioHolding> holdings = portfolioHoldingRepository.findByPortfolioId(portfolio.getId());
        dto.setItems(holdings.stream()
                .map(PortfolioHoldingRespDTO::fromEntity)
                .collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional
    public boolean deletePortfolio(Long id, Long userId) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("组合不存在"));

        if (!portfolio.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("没有权限删除该组合");
        }

        portfolioRepository.delete(portfolio);
        return true;
    }
}