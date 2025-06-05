package com.minute.auth.dto.response;

import com.minute.auth.common.ResponseCode;
import com.minute.auth.common.ResponseMessage;
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
