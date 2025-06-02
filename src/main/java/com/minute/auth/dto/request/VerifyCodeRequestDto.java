package com.minute.auth.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class VerifyCodeRequestDto {

    private String userEmail;
    private String certificationNumber;
}
