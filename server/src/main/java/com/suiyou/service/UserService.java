package com.suiyou.service;

import com.suiyou.dto.auth.LoginResponseDTO;
import com.suiyou.model.User;
import com.suiyou.repository.UserRepository;
import com.suiyou.service.FamilyService;
import com.suiyou.security.JwtTokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private FamilyService familyService;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    // 用户注册
    @Transactional
    public User register(String phoneNumber, String username, String password) {
        // 检查手机号是否已存在
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("手机号已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        
        // 如果用户名为空，使用手机号后四位作为默认用户名
        if (username == null || username.trim().isEmpty()) {
            username = "用户" + phoneNumber.substring(phoneNumber.length() - 4);
        }
        user.setUsername(username);
        
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setIsDeleted(0); // 设置为未删除状态
        
        // 保存用户
        User savedUser = userRepository.save(user);

        // 自动创建家庭
        String familyName = user.getUsername() + "的家庭";
        familyService.createFamily(savedUser.getId(), familyName);

        return savedUser;
    }

    // 用户登录
    public LoginResponseDTO login(String phoneNumber, String password) {
        try {
            // 验证用户名密码
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(phoneNumber, password)
            );
        } catch (AuthenticationException e) {
            throw new RuntimeException("手机号或密码错误");
        }

        // 查找用户
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOptional.get();
        
        // 检查用户是否已删除
        if (user.getIsDeleted() != null && user.getIsDeleted() == 1) {
            throw new RuntimeException("用户已被删除");
        }
        
        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        // 生成token - 使用手机号作为subject，用户ID作为额外信息
        String token = jwtTokenProvider.generateToken(user.getPhoneNumber(), user.getId());

        // 构建响应
        return buildLoginResponse(user, token);
    }

    // 微信登录
    public LoginResponseDTO wechatLogin(String code, String nickname, String avatar, Integer gender) {
        String appId = "wx5caf34cc20778ea1";
        String appSecret = "c1a5e4cac731411e917cd04dfe39b71b";

        // 调用微信官方API换取openid
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code);

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        logger.info("微信登录响应: {}", response);
        
        ObjectMapper mapper = new ObjectMapper();
        String openId = null;
        String unionId = null;
        
        try {
            JsonNode json = mapper.readTree(response);
            if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                throw new RuntimeException("微信登录失败：" + json.get("errmsg").asText());
            }
            openId = json.get("openid").asText();
            if (json.has("unionid") && !json.get("unionid").isNull()) {
                unionId = json.get("unionid").asText();
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (openId == null) {
            throw new RuntimeException("微信登录失败：" + response);
        }

        // 查找微信用户
        Optional<User> userOptional = userRepository.findByWechatOpenId(openId);
        User user;

        if (userOptional.isPresent()) {
            // 已存在的用户，更新信息
            user = userOptional.get();
            
            // 检查用户是否已删除
            if (user.getIsDeleted() != null && user.getIsDeleted() == 1) {
                throw new RuntimeException("用户已被删除");
            }
            user.setWechatUnionId(unionId);
            user.setWechatNickname(nickname);
            user.setAvatar(avatar);
        } else {
            // 新用户，创建账号
            user = new User();
            user.setWechatOpenId(openId);
            user.setWechatUnionId(unionId);
            user.setWechatNickname(nickname);
            user.setAvatar(avatar);
            user.setPhoneNumber("wx_" + openId.substring(0, 10)); // 生成临时手机号
            user.setUsername(user.getWechatNickname() != null ? user.getWechatNickname() : "微信用户");
            user.setPasswordHash(passwordEncoder.encode("wx_" + openId)); // 设置临时密码
            user.setIsDeleted(0); // 设置为未删除状态
        }

        // 更新登录时间
        user.setLastLoginTime(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        // 如果是新用户，自动创建家庭
        if (!userOptional.isPresent()) {
            String familyName = savedUser.getUsername() + "的家庭";
            familyService.createFamily(savedUser.getId(), familyName);
        }

        // 生成token - 使用手机号作为subject，用户ID作为额外信息
        String token = jwtTokenProvider.generateToken(user.getPhoneNumber(), user.getId());

        // 构建响应
        return buildLoginResponse(user, token);
    }

    // 获取用户信息
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // 通过手机号查找用户
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    // 验证用户密码
    public boolean validatePassword(String phoneNumber, String password) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        if (!userOptional.isPresent()) {
            return false;
        }
        
        User user = userOptional.get();
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    // 更新用户信息
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // 构建登录响应
    private LoginResponseDTO buildLoginResponse(User user, String token) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);

        LoginResponseDTO.UserInfoDTO userInfo = new LoginResponseDTO.UserInfoDTO();
        userInfo.setId(user.getId());
        userInfo.setPhoneNumber(user.getPhoneNumber());
        userInfo.setUsername(user.getUsername());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setGender(user.getGender());
        userInfo.setWechatNickname(user.getWechatNickname());

        response.setUser(userInfo);
        return response;
    }
}