package com.suiyou.service;

import com.suiyou.model.TimeDepositItem;

import java.util.List;

public interface TimeDepositItemService {

    TimeDepositItem createTimeDepositItem(TimeDepositItem item);

    TimeDepositItem updateTimeDepositItem(Long id, TimeDepositItem item);

    TimeDepositItem getTimeDepositItemById(Long id);

    List<TimeDepositItem> getTimeDepositItemsByAssetId(Long assetId);

    boolean deleteTimeDepositItem(Long id);

    void deleteByAssetId(Long assetId);

    void updateAssetBalance(Long assetId);
}