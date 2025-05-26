package com.minute.auth.dto.response.auth;

import com.minute.auth.common.ResponseCode;
import com.minute.auth.common.ResponseMessage;
import com.minute.auth.dto.response.ResponseDto;
import com.minute.user.dto.response.CommonResponseDto;
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

    public static ResponseEntity<ResponseDto> invalidPassword() {
        ResponseDto result = new ResponseDto("INVALID_PASSWORD", "비밀번호는 8~20자이며, 영문/숫자/특수문자를 포함해야 합니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    public static ResponseEntity<ResponseDto> duplicateEmail(){
        return CommonResponseDto.notExistUser();
    }

    public static ResponseEntity<ResponseDto> duplicateNickName(){
        return CommonResponseDto.duplicateNickName();
    }

    public static ResponseEntity<ResponseDto> duplicatePhone(){
        return CommonResponseDto.duplicatePhone();
    }
}
