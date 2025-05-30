package com.minute.auth.dto.request;

import com.minute.user.enumpackage.UserGender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignUpRequestDTO {

    @NotBlank
    private String userId;

    @NotBlank
    @Size(min = 8, max = 20)
    private String userPw;

    @NotBlank
    private String userName;

    @NotBlank
    private String userNickName;

    @NotBlank
    @Pattern(regexp = "^[0-9]{11,13}$")
    private String userPhone;

    @NotBlank
    @Email
    private String userEmail;

    @NotNull
    private UserGender userGender;





}
