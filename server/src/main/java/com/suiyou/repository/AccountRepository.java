package com.suiyou.repository;

import com.suiyou.model.Account;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // 根据用户ID和状态查询账户（未删除的）
    List<Account> findByOwnerIdAndStatusAndDeletedFalse(Long ownerId, Integer status);

    // 根据机构代码和机构唯一标识查询账户（未删除的）
    Account findByInstitutionAndInstitutionIdentifierAndDeletedFalse(String institution, String institutionIdentifier);

    // 根据用户ID和状态查询账户（包括已删除的）
    List<Account> findByOwnerIdAndStatus(Long ownerId, Integer status);

    // 根据用户ID查询所有未删除的账户（包括不同状态的）
    List<Account> findByOwnerIdAndDeletedFalse(Long ownerId);

    // 根据机构代码和机构唯一标识查询账户（包括已删除的）
    Account findByInstitutionAndInstitutionIdentifier(String institution, String institutionIdentifier);

    // 获取用户活跃账户的最大排序值
    Integer findMaxSortOrderByOwnerIdAndStatusAndDeletedFalse(Long ownerId, Integer status);
}
