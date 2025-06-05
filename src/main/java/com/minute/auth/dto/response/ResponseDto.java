package com.minute.auth.dto.response;

import com.minute.auth.common.ResponseCode;
import com.minute.auth.common.ResponseMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class ResponseDto {

    private String code;
    private String message;
    private String fileName;

    // 3개짜리 생성자
    public ResponseDto(String code, String message, String fileName) {
        this.code = code;
        this.message = message;
        this.fileName = fileName;
    }

    // 2개짜리 생성자 (fileName 없이 사용 가능하게)
    public ResponseDto(String code, String message) {
        this.code = code;
        this.message = message;
        this.fileName = null;  // 명시적으로 null 처리
    }

    public ResponseDto() {

    }

    public static ResponseEntity<ResponseDto> databaseError() {
        ResponseDto responsebody = new ResponseDto(ResponseCode.DATABASE_ERROR, ResponseMessage.DATABASE_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responsebody);
    }

    public static ResponseEntity<ResponseDto> validationFailed(){
        ResponseDto responsebody = new ResponseDto(ResponseCode.VALIDATION_FAILED, ResponseMessage.VALIDATION_FAILED);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responsebody);
    }

}