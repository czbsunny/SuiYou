package com.suiyou.service;

import com.suiyou.model.Family;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface FamilyService {
    /**
     * 创建家庭
     * @param creatorId 创建者ID
     * @param name 家庭名称
     * @param currency 货币类型
     * @return 创建的家庭
     */
    Family createFamily(Long creatorId, String name, String currency);
    
    /**
     * 根据用户ID获取用户的家庭列表
     * @param userId 用户ID
     * @return 家庭列表
     */
    List<Family> getFamiliesByUserId(Long userId);
    
    /**
     * 根据用户ID获取用户的第一个活跃家庭
     * @param userId 用户ID
     * @return 第一个活跃家庭
     */
    Family getFirstActiveFamilyByUserId(Long userId);
    
    /**
     * 根据家庭ID获取家庭
     * @param familyId 家庭ID
     * @return 家庭
     */
    Family getFamilyById(Long familyId);
}