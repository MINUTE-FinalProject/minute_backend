package com.minute.auth.dto.response;

import org.springframework.http.ResponseEntity;

public class ResetPasswordResponseDto extends ResponseDto {

    public static ResponseEntity<ResponseDto> success() {
        return ResponseEntity.ok(new ResponseDto("SU", "비밀번호 재설정 성공"));
    }

    public static ResponseEntity<ResponseDto> userNotFound() {
        return ResponseEntity.status(404).body(new ResponseDto("NF", "사용자 없음"));
    }

    public static ResponseEntity<ResponseDto> invalidPassword() {
        return ResponseEntity.badRequest().body(new ResponseDto("IP", "비밀번호 형식이 잘못되었습니다."));
    }
}
