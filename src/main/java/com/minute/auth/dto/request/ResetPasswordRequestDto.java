package com.minute.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResetPasswordRequestDto {
    private String userEmail;
    private String newPassword;
}

