package com.suiyou.repository;

import com.suiyou.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 通过手机号查找用户
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    // 通过微信OpenID查找用户
    Optional<User> findByWechatOpenId(String wechatOpenId);
    
    // 检查手机号是否已存在
    boolean existsByPhoneNumber(String phoneNumber);
    
    // 检查微信OpenID是否已存在
    boolean existsByWechatOpenId(String wechatOpenId);
}