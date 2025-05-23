package com.minute.auth.dto.response.auth;

import com.minute.auth.common.ResponseCode;
import com.minute.auth.common.ResponseMessage;
import com.minute.auth.dto.response.ResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class SignInResponseDto extends ResponseDto {
    private String token;
    private int expirationTime;

    private SignInResponseDto(String token){
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        this.token=token;
        this.expirationTime= 3600;
    }

    //로그인 성공
    public static ResponseEntity<SignInResponseDto> success(String token) {
        SignInResponseDto result = new SignInResponseDto(token);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    //로그인 실패
    public static ResponseEntity<ResponseDto> signInFailed() {
        ResponseDto result = new ResponseDto(ResponseCode.SIGN_IN_FAIL, ResponseMessage.SIGN_IN_FAIL);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    //비밀번호 오류
    public static ResponseEntity<ResponseDto> wrongPw() {
        ResponseDto result = new ResponseDto(ResponseCode.INVALID_PASSWORD, ResponseMessage.INVALID_PASSWORD);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
}
