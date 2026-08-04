package com.suiyou.dto.invitation;

import com.suiyou.model.Invitation;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvitationResponseDTO {
    private Long id;
    private Long familyId;
    private Long inviterId;
    private String invitationType;
    private String credential;
    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;
    private String status;

    public static InvitationResponseDTO from(Invitation invitation) {
        InvitationResponseDTO response = new InvitationResponseDTO();
        response.id = invitation.getId();
        response.familyId = invitation.getFamilyId();
        response.inviterId = invitation.getInviterId();
        response.invitationType = invitation.getInvitationType();
        response.credential = invitation.getCredential();
        response.expiresAt = invitation.getExpiresAt();
        response.acceptedAt = invitation.getAcceptedAt();
        response.createdAt = invitation.getCreatedAt();
        response.status = invitation.getAcceptedAt() != null ? "ACCEPTED" :
                (invitation.getExpiresAt().isAfter(LocalDateTime.now()) ? "PENDING" : "EXPIRED");
        return response;
    }
}
