package com.suiyou.repository;

import com.suiyou.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByOwnerId(Long ownerId);
    List<Account> findByFamilyId(Long familyId);
    List<Account> findByInstCode(String instCode);
    Account findByAccountNo(String accountNo);
}