package com.minute.auth.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyCodeRequestDto {
    private String userId;
    private String userEmail;
    private String certificationNumber;
}
