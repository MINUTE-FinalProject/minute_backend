package com.minute.user.dto.request;

import com.minute.user.enumpackage.UserGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserPatchInfoRequestDto {


    private String userNickName;

    private String userProfileImage;

    private String userPhone;

    private UserGender userGender;

    private String userEmail;
}
