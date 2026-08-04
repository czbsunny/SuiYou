package com.suiyou.service.impl;

import com.suiyou.model.FamilyMember;
import com.suiyou.model.Invitation;
import com.suiyou.model.User;
import com.suiyou.repository.FamilyMemberRepository;
import com.suiyou.repository.InvitationRepository;
import com.suiyou.repository.UserRepository;
import com.suiyou.service.FamilyService;
import com.suiyou.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class InvitationServiceImpl implements InvitationService {
    private static final String CODE = "CODE";
    private static final String CODE_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private FamilyMemberRepository familyMemberRepository;

    @Autowired
    private FamilyService familyService;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Invitation createCodeInvitation(Long inviterId) {
        Long familyId = familyService.getRequiredFamilyByUserId(inviterId).getId();
        FamilyMember member = familyMemberRepository.findByFamilyIdAndUserId(familyId, inviterId)
                .orElseThrow(() -> new RuntimeException("无权为该家庭创建邀请"));
        if (!Integer.valueOf(1).equals(member.getStatus())) {
            throw new RuntimeException("无权为该家庭创建邀请");
        }
        if (invitationRepository.findByFamilyIdAndInvitationTypeAndAcceptedAtIsNullAndExpiresAtAfter(
                familyId, CODE, LocalDateTime.now()).isPresent()) {
            throw new RuntimeException("该家庭已有正在生效的邀请");
        }

        for (int attempt = 0; attempt < 10; attempt++) {
            Invitation invitation = new Invitation();
            invitation.setFamilyId(familyId);
            invitation.setInviterId(inviterId);
            invitation.setInvitationType(CODE);
            invitation.setCredential(generateCredential());
            invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
            try {
                return invitationRepository.saveAndFlush(invitation);
            } catch (DataIntegrityViolationException e) {
                if (attempt == 9) {
                    throw new RuntimeException("生成邀请口令失败");
                }
            }
        }
        throw new RuntimeException("生成邀请口令失败");
    }

    @Override
    @Transactional(readOnly = true)
    public Invitation getValidInvitation(String invitationType, String credential) {
        validateCode(invitationType, credential);
        Invitation invitation = invitationRepository.findByInvitationTypeAndCredential(CODE, credential)
                .orElseThrow(() -> new RuntimeException("邀请口令不存在"));
        validateAvailable(invitation);
        return invitation;
    }

    @Override
    @Transactional
    public Invitation acceptInvitation(String invitationType, String credential, Long userId) {
        validateCode(invitationType, credential);
        Invitation invitation = invitationRepository.findForUpdate(CODE, credential)
                .orElseThrow(() -> new RuntimeException("邀请口令不存在"));
        validateAvailable(invitation);
        if (familyMemberRepository.existsByFamilyIdAndUserId(invitation.getFamilyId(), userId)) {
            throw new RuntimeException("用户已经是该家庭成员");
        }

        FamilyMember member = new FamilyMember();
        member.setFamilyId(invitation.getFamilyId());
        member.setUserId(userId);
        member.setRole("MEMBER");
        member.setJoinedAt(LocalDateTime.now());
        familyMemberRepository.saveAndFlush(member);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setFamilyId(invitation.getFamilyId());
        userRepository.save(user);
        invitation.setAcceptedAt(LocalDateTime.now());
        return invitationRepository.save(invitation);
    }

    private String generateCredential() {
        StringBuilder credential = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            credential.append(CODE_CHARACTERS.charAt(RANDOM.nextInt(CODE_CHARACTERS.length())));
        }
        return credential.toString();
    }

    private void validateCode(String invitationType, String credential) {
        if (invitationType == null || !CODE.equals(invitationType)) {
            throw new RuntimeException("暂不支持该邀请方式");
        }
        if (credential == null || !credential.matches("[0-9A-Za-z]{6}")) {
            throw new RuntimeException("邀请口令必须是6位数字或字母");
        }
    }

    private void validateAvailable(Invitation invitation) {
        if (invitation.getAcceptedAt() != null) {
            throw new RuntimeException("邀请口令已接受");
        }
        if (!invitation.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("邀请口令已过期");
        }
    }
}
