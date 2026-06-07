package com.suiyou.repository;

import com.suiyou.model.Account;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByOwnerIdAndStatusAndDeletedFalse(Long ownerId, Integer status);

    Account findByInstCodeAndAccountNoAndDeletedFalse(String instCode, String accountNo);

    Account findByOwnerIdAndInstCodeAndAccountTypeAndAccountNoAndDeletedFalse(
            Long ownerId, String instCode, String accountType, String accountNo);

    Account findByOwnerIdAndInstCodeAndAccountNoAndDeletedFalse(Long ownerId, String instCode, String accountNo);

    List<Account> findByOwnerIdAndStatus(Long ownerId, Integer status);

    List<Account> findByOwnerIdAndDeletedFalse(Long ownerId);

    Account findByInstCodeAndAccountNo(String instCode, String accountNo);

    Account findByOwnerIdAndInstCodeAndAccountNo(Long ownerId, String instCode, String accountNo);

    List<Account> findByOwnerIdAndInstCodeAndDeletedFalse(Long ownerId, String instCode);

    @Query("SELECT MAX(a.sortOrder) FROM Account a WHERE a.ownerId = :ownerId AND a.status = :status AND a.deleted = false")
    Integer findMaxSortOrderByOwnerIdAndStatusAndDeletedFalse(@Param("ownerId") Long ownerId, @Param("status") Integer status);
}
