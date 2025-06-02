package com.minute.board.free.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "관리자용 신고된 활동 목록 검색/필터 조건 DTO")
public class AdminReportFilterDTO {

    @Schema(description = "검색 키워드 (제목 또는 내용에 포함된 단어)", example = "문제 있는 내용")
    private String keyword; // 제목 또는 내용 검색용

    @Schema(description = "신고된 항목의 ID (게시글 ID 또는 댓글 ID)", example = "101")
    private Integer reportedItemId; // 'ID' 검색용 (게시글/댓글 ID)

    @Schema(description = "신고된 항목의 작성자 키워드 (User ID 또는 닉네임)", example = "originalAuthor")
    private String authorKeyword; // '닉네임' 또는 'ID' (작성자) 검색용

    @Schema(description = "신고된 항목의 숨김 상태 (true: 숨김, false: 공개, null: 전체)", example = "false")
    private Boolean isItemHidden;

    @Schema(description = "신고일 검색 시작일 (YYYY-MM-DD)", example = "2025-05-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate reportStartDate;

    @Schema(description = "신고일 검색 종료일 (YYYY-MM-DD)", example = "2025-05-31")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate reportEndDate;

    @Schema(description = "원본 항목 작성일 검색 시작일 (YYYY-MM-DD)", example = "2025-04-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate originalItemStartDate;

    @Schema(description = "원본 항목 작성일 검색 종료일 (YYYY-MM-DD)", example = "2025-04-30")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate originalItemEndDate;

    // --- JPQL 또는 Specification 내부에서 사용할 조정된 날짜 필드 ---
    @Schema(hidden = true)
    private LocalDateTime queryReportStartDate;
    @Schema(hidden = true)
    private LocalDateTime queryReportEndDate;
    @Schema(hidden = true)
    private LocalDateTime queryOriginalItemStartDate;
    @Schema(hidden = true)
    private LocalDateTime queryOriginalItemEndDate;
}