package com.minute.user.dto.response.auth;

import com.minute.user.common.ResponseCode;
import com.minute.user.common.ResponseMessage;
import com.minute.user.dto.response.ResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class SignupValidateResponseDto extends ResponseDto{

    private SignupValidateResponseDto() {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    }

    public static ResponseEntity<SignupValidateResponseDto> success(){
        SignupValidateResponseDto result = new SignupValidateResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> duplicateId(){
        return SignupResponseDto.duplicateId();
    }

    public static ResponseEntity<ResponseDto> invalidPassword() {
        return SignupResponseDto.invalidPassword();
    }
}
