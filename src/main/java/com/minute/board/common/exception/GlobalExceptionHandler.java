package com.minute.board.common.exception; // 실제 패키지 경로에 맞게 수정해주세요.

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j; // Slf4j import 추가
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap; // 중복 import 제거 가능
import java.util.Map;

@Slf4j // Lombok Slf4j 로거 사용
@RestControllerAdvice
public class GlobalExceptionHandler {

    // EntityNotFoundException 처리 (JPA 사용 시 표준 예외)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.warn("EntityNotFoundException caught: {}", ex.getMessage()); // 예외 발생 시 로그 (WARN 레벨)
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        log.warn("IllegalStateException caught: {}", ex.getMessage()); // 예외 발생 시 로그 (WARN 레벨)
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /*
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException caught: {}", ex.getMessage()); // 예외 발생 시 로그 (WARN 레벨)
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    */

    // 최상위 예외 처리 (예상치 못한 모든 서버 오류)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex) {
        // ⭐⭐⭐ 어떤 예외가 잡혔는지 상세 로그로 확인 ⭐⭐⭐
        log.error("Unhandled exception caught by GlobalExceptionHandler: ", ex); // 예외 스택 트레이스 전체를 ERROR 레벨로 기록
        // log.error("Exception Type: {}", ex.getClass().getName()); // 필요하다면 예외 타입만 별도로 로깅
        // log.error("Exception Message: {}", ex.getMessage());    // 필요하다면 메시지만 별도로 로깅

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.");
        // ex.printStackTrace(); // @Slf4j의 log.error("", ex)가 이미 스택 트레이스를 포함하므로 중복될 수 있음.

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}