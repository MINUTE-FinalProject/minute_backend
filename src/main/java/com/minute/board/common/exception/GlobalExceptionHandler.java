package com.minute.board.common.exception; // 실제 패키지 경로에 맞게 수정해주세요.

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // @ControllerAdvice + @ResponseBody, REST API의 예외 처리에 적합
public class GlobalExceptionHandler {

    // EntityNotFoundException 처리 (JPA 사용 시 표준 예외)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", ex.getMessage()); // 서비스에서 던진 메시지를 사용
        // ex.getMessage() 예: "해당 ID의 공지사항을 찾을 수 없습니다: 999"

        // HTTP 404 Not Found 상태 코드와 함께 에러 메시지를 JSON 형태로 반환
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 다른 종류의 커스텀 예외나 표준 예외에 대한 핸들러도 여기에 추가할 수 있습니다.
    // 예: IllegalArgumentException 등
    /*
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); // 400 Bad Request
    }
    */

    // 최상위 예외 처리 (예상치 못한 모든 서버 오류)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.");
        // 실제 운영 환경에서는 ex.printStackTrace() 등으로 로그를 남기는 것이 중요합니다.
        ex.printStackTrace(); // 개발 중에는 스택 트레이스 확인

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
    }
}