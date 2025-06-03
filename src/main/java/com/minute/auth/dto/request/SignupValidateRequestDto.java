package com.minute.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupValidateRequestDto {
    @NotBlank
    private String userId;

    @NotBlank
    @Size(min = 8, max = 20)
    private String userPw;
}
