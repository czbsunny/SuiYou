package com.suiyou.repository;

import com.suiyou.model.Family;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {
    /**
     * 根据创建者ID查询家庭
     * @param creatorId 创建者ID
     * @return 家庭
     */
    Family findByCreatorId(Long creatorId);
    
    /**
     * 根据创建者ID和状态查询家庭
     * @param creatorId 创建者ID
     * @param status 状态
     * @return 家庭列表
     */
    List<Family> findByCreatorIdAndStatus(Long creatorId, Integer status);
    
    /**
     * 根据家庭ID和状态查询家庭
     * @param id 家庭ID
     * @param status 状态
     * @return 家庭
     */
    Family findByIdAndStatus(Long id, Integer status);
}