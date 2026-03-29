package com.suiyou.service.impl;

import com.suiyou.dto.portfolio.CreatePortfolioDTO;
import com.suiyou.dto.portfolio.PortfolioRespDTO;
import com.suiyou.model.Asset;
import com.suiyou.model.Portfolio;
import com.suiyou.model.User;
import com.suiyou.repository.AssetRepository;
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
        return portfolioRepository.findAll().stream()
                .filter(portfolio -> portfolio.getUser() != null && portfolio.getUser().getId().equals(userId))
                .map(PortfolioRespDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PortfolioRespDTO getPortfolioById(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("组合不存在"));
        return PortfolioRespDTO.fromEntity(portfolio);
    }

    @Override
    public PortfolioRespDTO getPortfolioByAssetId(Long assetId) {
        return portfolioRepository.findAll().stream()
                .filter(portfolio -> portfolio.getAsset() != null && portfolio.getAsset().getId().equals(assetId))
                .map(PortfolioRespDTO::fromEntity)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("资产对应的组合不存在"));
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