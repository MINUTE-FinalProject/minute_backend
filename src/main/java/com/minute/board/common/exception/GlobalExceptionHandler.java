package com.minute.board.common.exception; // 실제 패키지 경로에 맞게 수정해주세요.

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.http.HttpStatus; // 추가
import org.springframework.http.ResponseEntity; // 추가
import java.util.Map; // 추가
import java.util.HashMap; // 추가

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

    @ExceptionHandler(IllegalStateException.class)
// @ResponseStatus(HttpStatus.CONFLICT) // 이 어노테이션 대신 ResponseEntity를 직접 사용
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error"); // 또는 "fail" 등 프론트와 약속된 상태 코드
        errorResponse.put("message", ex.getMessage()); // 예: "이미 신고한 게시글입니다."
        // 실제 운영에서는 클라이언트에게 너무 상세한 예외 메시지 대신 일반적인 메시지를 보여주는 것이 좋을 수 있습니다.
        // ex.printStackTrace(); // 개발 중에는 서버 로그에 스택 트레이스 기록
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT); // 409 Conflict 상태 코드 반환
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