package com.suiyou.repository;

import com.suiyou.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    /**
     * 根据用户ID查询所有账户
     */
    List<Account> findByOwnerId(Long ownerId);
    /**
     * 根据账户ID和用户ID查询账户
     */
    Optional<Account> findByIdAndOwnerId(Long id, Long ownerId);

    List<Account> findByFamilyId(Long familyId);
    List<Account> findByInstCode(String instCode);
    Account findByAccountNo(String accountNo);
}