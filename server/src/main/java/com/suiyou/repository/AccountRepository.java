package com.suiyou.repository;

import com.suiyou.model.Account;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // 根据用户ID和状态查询账户
    List<Account> findByOwnerIdAndStatus(Long ownerId, Integer status);
}
