package com.suiyou.repository;

import com.suiyou.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByWechatOpenId(String wechatOpenId);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByWechatOpenId(String wechatOpenId);

    @Query("SELECT u.id FROM User u WHERE u.isDeleted IS NULL OR u.isDeleted = 0")
    List<Long> findAllValidUserIds();
}
