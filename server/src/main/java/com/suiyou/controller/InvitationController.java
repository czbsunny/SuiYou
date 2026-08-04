package com.suiyou.controller;

import com.suiyou.dto.invitation.InvitationQueryDTO;
import com.suiyou.dto.invitation.InvitationResponseDTO;
import com.suiyou.model.Invitation;
import com.suiyou.security.SecurityUtils;
import com.suiyou.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invitations")
public class InvitationController {
    @Autowired
    private InvitationService invitationService;

    @PostMapping
    public ResponseEntity<?> create() {
        try {
            Invitation invitation = invitationService.createCodeInvitation(
                    SecurityUtils.getCurrentUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(InvitationResponseDTO.from(invitation));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> query(@Valid InvitationQueryDTO request) {
        try {
            return ResponseEntity.ok(InvitationResponseDTO.from(invitationService.getValidInvitation(
                    request.getInvitationType(), request.getCredential())));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/accept")
    public ResponseEntity<?> accept(@Valid @RequestBody InvitationQueryDTO request) {
        try {
            Invitation invitation = invitationService.acceptInvitation(
                    request.getInvitationType(), request.getCredential(), SecurityUtils.getCurrentUserId());
            return ResponseEntity.ok(InvitationResponseDTO.from(invitation));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
