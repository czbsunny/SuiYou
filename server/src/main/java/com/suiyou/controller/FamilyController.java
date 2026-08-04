package com.suiyou.controller;

import com.suiyou.dto.family.CreateFamilyDTO;
import com.suiyou.model.Family;
import com.suiyou.security.SecurityUtils;
import com.suiyou.service.FamilyService;
import com.suiyou.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/families")
public class FamilyController {
    @Autowired
    private FamilyService familyService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateFamilyDTO request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Family family = familyService.createFamily(
                    userId,
                    request.getName(),
                    request.getCurrency());
            userService.switchFamily(userId, family.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(family);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
