package com.minute.user.dto.response;

import com.minute.auth.common.ResponseCode;
import com.minute.auth.common.ResponseMessage;
import com.minute.auth.dto.response.ResponseDto;
import com.minute.user.entity.User;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Getter
public class GetSignInUserResponseDto extends ResponseDto {
    private String userId;
    private String userName;
    private String userNickName;
    private String userEmail;
    private LocalDateTime createdAt;
    private String profileImage;

    private GetSignInUserResponseDto(User user) {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        this.userId=user.getUserId();
        this.userName=user.getUserName();
        this.userNickName=user.getUserNickName();
        this.userEmail=user.getUserEmail();
        this.createdAt=user.getCreatedAt();
        this.profileImage=user.getProfileImage();
    }

    public static ResponseEntity<GetSignInUserResponseDto> success(User user) {
        GetSignInUserResponseDto result = new GetSignInUserResponseDto(user);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> notExistUser(){
        return CommonResponseDto.notExistUser();
    }
}
