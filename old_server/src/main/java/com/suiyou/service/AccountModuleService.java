package com.suiyou.service;

import com.suiyou.dto.account.AccountModuleRespDTO;
import com.suiyou.model.AccountModule;

import java.util.List;

public interface AccountModuleService {
    
    AccountModule addModule(Long accountId, String assetType, String moduleName, Long userId);
    
    boolean removeModule(Long accountId, Long moduleId, Long userId);
    
    List<AccountModuleRespDTO> getModulesByAccountId(Long accountId, Long userId);
    
    AccountModule getModuleById(Long id);
}