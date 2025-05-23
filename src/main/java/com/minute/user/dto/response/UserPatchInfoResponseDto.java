package com.minute.user.dto.response;

import com.minute.auth.common.ResponseCode;
import com.minute.auth.common.ResponseMessage;
import com.minute.auth.dto.response.ResponseDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class UserPatchInfoResponseDto extends ResponseDto {

    private UserPatchInfoResponseDto() {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    }

    public static ResponseEntity<UserPatchInfoResponseDto> success() {
        UserPatchInfoResponseDto result = new UserPatchInfoResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> noExistUser() {
        ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_USER, ResponseMessage.NOT_EXISTED_USER);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    public static ResponseEntity<ResponseDto> duplicateNickName() {
        return CommonResponseDto.duplicateNickName();
    }

    public static ResponseEntity<ResponseDto> duplicatePhone() {
        return CommonResponseDto.duplicatePhone();
    }

    public static ResponseEntity<ResponseDto> duplicateEmail() {
        return CommonResponseDto.duplicateEmail();
    }
}
