package com.suiyou.controller;

import com.suiyou.dto.auth.*;
import com.suiyou.model.User;
import com.suiyou.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            User user = userService.register(
                    registerDTO.getPhoneNumber(),
                    registerDTO.getUsername(),
                    registerDTO.getPassword()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body("注册成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            LoginResponseDTO response = userService.login(
                    loginDTO.getPhoneNumber(),
                    loginDTO.getPassword()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/wechat-login")
    public ResponseEntity<?> wechatLogin(@Valid @RequestBody WechatLoginDTO wechatLoginDTO) {
        try {
            LoginResponseDTO response = userService.wechatLogin(
                    wechatLoginDTO.getCode(),
                    wechatLoginDTO.getNickname(),
                    wechatLoginDTO.getAvatar(),
                    wechatLoginDTO.getGender()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestAttribute("userId") Long userId) {
        return userService.getUserById(userId)
                .map(user -> {
                    LoginResponseDTO.UserInfoDTO userInfo = new LoginResponseDTO.UserInfoDTO();
                    userInfo.setId(user.getId());
                    userInfo.setPhoneNumber(user.getPhoneNumber());
                    userInfo.setUsername(user.getUsername());
                    userInfo.setAvatar(user.getAvatar());
                    userInfo.setGender(user.getGender());
                    userInfo.setWechatNickname(user.getWechatNickname());
                    return ResponseEntity.ok(userInfo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserInfo(@RequestAttribute("userId") Long userId, @Valid @RequestBody UpdateUserInfoDTO updateUserInfoDTO) {
        return userService.getUserById(userId)
                .map(user -> {
                    user.setUsername(updateUserInfoDTO.getUsername());
                    User updatedUser = userService.updateUser(user);
                    LoginResponseDTO.UserInfoDTO userInfo = new LoginResponseDTO.UserInfoDTO();
                    userInfo.setId(updatedUser.getId());
                    userInfo.setPhoneNumber(updatedUser.getPhoneNumber());
                    userInfo.setUsername(updatedUser.getUsername());
                    userInfo.setAvatar(updatedUser.getAvatar());
                    userInfo.setGender(updatedUser.getGender());
                    userInfo.setWechatNickname(updatedUser.getWechatNickname());
                    return ResponseEntity.ok(userInfo);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
