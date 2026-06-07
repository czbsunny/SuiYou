package com.suiyou.service;

import com.suiyou.dto.auth.LoginResponseDTO;
import com.suiyou.model.User;
import com.suiyou.repository.UserRepository;
import com.suiyou.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

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

    @Value("${wechat.appid:}")
    private String wechatAppId;

    @Value("${wechat.secret:}")
    private String wechatAppSecret;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public User register(String phoneNumber, String username, String password) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("手机号已被注册");
        }

        User user = new User();
        user.setPhoneNumber(phoneNumber);

        if (username == null || username.trim().isEmpty()) {
            username = "用户" + phoneNumber.substring(phoneNumber.length() - 4);
        }
        user.setUsername(username);

        user.setPasswordHash(passwordEncoder.encode(password));
        user.setIsDeleted(0);

        User savedUser = userRepository.save(user);

        String familyName = user.getUsername() + "的家庭";
        familyService.createFamily(savedUser.getId(), familyName, "CNY");

        return savedUser;
    }

    public LoginResponseDTO login(String phoneNumber, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(phoneNumber, password)
            );
        } catch (AuthenticationException e) {
            throw new RuntimeException("手机号或密码错误");
        }

        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOptional.get();

        if (user.getIsDeleted() != null && user.getIsDeleted() == 1) {
            throw new RuntimeException("用户已被删除");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
        familyService.ensureFamily(user.getId());

        String token = jwtTokenProvider.generateToken(user.getPhoneNumber(), user.getId());

        return buildLoginResponse(user, token);
    }

    public LoginResponseDTO wechatLogin(String code, String nickname, String avatar, Integer gender) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wechatAppId, wechatAppSecret, code);

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
        } catch (Exception e) {
            logger.warn("解析微信响应失败: {}", e.getMessage());
            if (openId == null && response != null && response.contains("openid")) {
                try {
                    JsonNode json = mapper.readTree(response);
                    if (json.has("openid")) {
                        openId = json.get("openid").asText();
                    }
                } catch (Exception ignored) {}
            }
        }

        if (openId == null) {
            if ("true".equalsIgnoreCase(System.getProperty("wechat.mock", "true"))) {
                openId = "mock_" + code.substring(Math.max(0, code.length() - 8));
                logger.info("使用 mock openId: {}", openId);
            } else {
                throw new RuntimeException("微信登录失败：" + response);
            }
        }

        Optional<User> userOptional = userRepository.findByWechatOpenId(openId);
        User user;
        String nickName = (nickname != null && !nickname.trim().isEmpty()) ? nickname : ("用户" + openId.substring(0, Math.min(6, openId.length())));
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getIsDeleted() != null && user.getIsDeleted() == 1) {
                throw new RuntimeException("用户已被删除");
            }
            user.setWechatUnionId(unionId);
            user.setWechatNickname(nickName);
            user.setAvatar(avatar);
        } else {
            user = new User();
            user.setWechatOpenId(openId);
            user.setWechatUnionId(unionId);
            user.setWechatNickname(nickName);
            user.setAvatar(avatar);
            user.setPhoneNumber("wx_" + openId.substring(0, Math.min(10, openId.length())));
            user.setUsername(nickName);
            user.setPasswordHash(passwordEncoder.encode("wx_" + openId));
            user.setIsDeleted(0);
        }

        user.setLastLoginTime(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        familyService.ensureFamily(savedUser.getId());

        String token = jwtTokenProvider.generateToken(user.getPhoneNumber(), user.getId());

        return buildLoginResponse(user, token);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public boolean validatePassword(String phoneNumber, String password) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        if (!userOptional.isPresent()) {
            return false;
        }

        User user = userOptional.get();
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

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
