package com.suiyou.service.impl;

import com.suiyou.model.Asset;
import com.suiyou.model.TimeDepositItem;
import com.suiyou.repository.AssetRepository;
import com.suiyou.repository.TimeDepositItemRepository;
import com.suiyou.service.TimeDepositItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TimeDepositItemServiceImpl implements TimeDepositItemService {

    @Autowired
    private TimeDepositItemRepository timeDepositItemRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Override
    @Transactional
    public TimeDepositItem createTimeDepositItem(TimeDepositItem item) {
        TimeDepositItem savedItem = timeDepositItemRepository.save(item);
        updateAssetBalance(item.getAssetId());
        return savedItem;
    }

    @Override
    @Transactional
    public TimeDepositItem updateTimeDepositItem(Long id, TimeDepositItem item) {
        TimeDepositItem existingItem = timeDepositItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("定期存款记录不存在: " + id));

        Long oldAssetId = existingItem.getAssetId();

        existingItem.setPrincipal(item.getPrincipal());
        existingItem.setAnnualRate(item.getAnnualRate());
        existingItem.setStartDate(item.getStartDate());
        existingItem.setMaturityDate(item.getMaturityDate());
        existingItem.setInterestType(item.getInterestType());
        existingItem.setPayoutType(item.getPayoutType());
        existingItem.setAutoRenewal(item.getAutoRenewal());
        existingItem.setTag(item.getTag());

        TimeDepositItem savedItem = timeDepositItemRepository.save(existingItem);

        updateAssetBalance(oldAssetId);
        if (!oldAssetId.equals(savedItem.getAssetId())) {
            updateAssetBalance(savedItem.getAssetId());
        }

        return savedItem;
    }

    @Override
    public TimeDepositItem getTimeDepositItemById(Long id) {
        return timeDepositItemRepository.findByIdAndStatus(id, 1).orElse(null);
    }

    @Override
    public List<TimeDepositItem> getTimeDepositItemsByAssetId(Long assetId) {
        return timeDepositItemRepository.findByAssetIdAndStatus(assetId, 1);
    }

    @Override
    @Transactional
    public boolean deleteTimeDepositItem(Long id) {
        return timeDepositItemRepository.findById(id)
                .map(item -> {
                    item.setStatus(0);
                    timeDepositItemRepository.save(item);
                    updateAssetBalance(item.getAssetId());
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public void deleteByAssetId(Long assetId) {
        timeDepositItemRepository.deleteByAssetId(assetId);
    }

    @Override
    @Transactional
    public void updateAssetBalance(Long assetId) {
        Asset asset = assetRepository.findById(assetId).orElse(null);
        if (asset == null) {
            return;
        }

        List<TimeDepositItem> items = timeDepositItemRepository.findByAssetIdAndStatus(assetId, 1);
        BigDecimal totalBalance = items.stream()
                .map(TimeDepositItem::calculateCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        asset.setTotalBalance(totalBalance);
        asset.setFrozenBalance(totalBalance);
        assetRepository.save(asset);
    }
}