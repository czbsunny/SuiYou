package com.suiyou.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suiyou.dto.CreateAccountDTO;
import com.suiyou.model.Account;
import com.suiyou.repository.AccountRepository;
import com.suiyou.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }
    
    @Override
    @Transactional
    public Account createAccount(CreateAccountDTO createAccountDTO, Long userId) {
        // 创建Account实体
        Account account = new Account();
        account.setName(createAccountDTO.getName());
        account.setGroupType(createAccountDTO.getGroupType());
        account.setCategory(createAccountDTO.getCategory());
        account.setSubCategory(createAccountDTO.getSubCategory());
        account.setInstitution(createAccountDTO.getInstitution());
        account.setCurrency(createAccountDTO.getCurrency());
        account.setVisibleScope(createAccountDTO.getVisibleScope());
        
        // 处理余额：如果是负债类型，余额转为负数
        BigDecimal balance = createAccountDTO.getBalance();
        if ("LIABILITY".equals(createAccountDTO.getGroupType()) && balance.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.negate();
        }
        account.setBalance(balance);
        
        // 设置用户ID和家庭ID
        account.setOwnerId(userId);
        // 这里需要从用户服务获取当前家庭ID
        Long familyId = getCurrentFamilyId(userId);
        account.setFamilyId(familyId);
        
        // 处理扩展属性
        if (createAccountDTO.getAttributes() != null && !createAccountDTO.getAttributes().isEmpty()) {
            try {
                String attributesJson = objectMapper.writeValueAsString(createAccountDTO.getAttributes());
                account.setAttributes(attributesJson);
            } catch (Exception e) {
                // 如果序列化失败，记录错误但继续创建账户
                System.err.println("Failed to serialize attributes: " + e.getMessage());
            }
        }
        
        // 保存账户
        return accountRepository.save(account);
    }
    
    /**
     * 获取用户的当前家庭ID
     * 这里需要调用用户服务或家庭服务来获取
     */
    private Long getCurrentFamilyId(Long userId) {
        // 这里应该调用用户服务或家庭服务来获取当前家庭ID
        // 临时实现：返回固定值（仅作示例）
        // 实际项目中应该从用户实体中获取currentFamilyId
        return 1L; // 临时返回固定值
    }
}