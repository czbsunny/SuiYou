package com.suiyou.dto.invitation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InvitationQueryDTO {
    private String invitationType = "CODE";

    @NotBlank(message = "邀请口令不能为空")
    private String credential;
}
