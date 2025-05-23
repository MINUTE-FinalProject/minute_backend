package com.minute.user.dto.response.auth;

import com.minute.user.common.ResponseCode;
import com.minute.user.common.ResponseMessage;
import com.minute.user.dto.response.ResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class SignupResponseDto extends ResponseDto {

    private SignupResponseDto() {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    }

    public static ResponseEntity<SignupResponseDto> success(){
        SignupResponseDto result = new SignupResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> duplicateId() {
        ResponseDto result = new ResponseDto(ResponseCode.DUPLICATE_ID, ResponseMessage.DUPLICATE_ID);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    public static ResponseEntity<ResponseDto> duplicateEmail(){
        ResponseDto result = new ResponseDto(ResponseCode.DUPLICATE_EMAIL, ResponseMessage.DUPLICATE_EMAIL);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    public static ResponseEntity<ResponseDto> duplicateNickName(){
        ResponseDto result = new ResponseDto(ResponseCode.DUPLICATE_NICKNAME, ResponseMessage.DUPLICATE_NICKNAME);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    public static ResponseEntity<ResponseDto> duplicatePhone(){
        ResponseDto result = new ResponseDto(ResponseCode.DUPLICATE_PHONE, ResponseMessage.DUPLICATE_PHONE);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    public static ResponseEntity<ResponseDto> invalidPassword() {
        ResponseDto result = new ResponseDto("INVALID_PASSWORD", "비밀번호는 8~20자이며, 영문/숫자/특수문자를 포함해야 합니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}
