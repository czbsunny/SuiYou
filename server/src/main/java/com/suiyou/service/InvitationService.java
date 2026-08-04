package com.suiyou.service;

import com.suiyou.model.Invitation;

public interface InvitationService {
    Invitation createCodeInvitation(Long inviterId);

    Invitation getValidInvitation(String invitationType, String credential);

    Invitation acceptInvitation(String invitationType, String credential, Long userId);
}
