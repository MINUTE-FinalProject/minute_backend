package com.minute.board.free.dto.request; // 실제 패키지 경로에 맞게 조정해주세요.

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "관리자용 '내 댓글' 목록 검색/필터 조건 DTO")
public class AdminMyCommentFilterDTO {

    @Schema(description = "댓글 내용 검색 키워드", example = "중요 공지")
    private String searchKeyword;

    @Schema(description = "댓글 작성일 검색 시작일 (YYYY-MM-DD)", example = "2025-05-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Schema(description = "댓글 작성일 검색 종료일 (YYYY-MM-DD)", example = "2025-05-31")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    // --- JPQL 또는 Specification 내부에서 사용할 조정된 날짜 필드 ---
    @Schema(hidden = true)
    private LocalDateTime queryStartDate;

    @Schema(hidden = true)
    private LocalDateTime queryEndDate;
}