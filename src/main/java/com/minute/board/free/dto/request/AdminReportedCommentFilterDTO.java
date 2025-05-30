package com.minute.board.free.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "관리자용 신고된 댓글 목록 검색/필터 조건 DTO")
public class AdminReportedCommentFilterDTO {

    @Schema(description = "검색 키워드 (댓글 내용, 댓글 작성자 ID/닉네임 등 통합)", example = "욕설")
    private String searchKeyword;

    @Schema(description = "신고된 댓글의 원본 게시글 ID", example = "101")
    private Integer originalPostId;

    @Schema(description = "댓글 작성자 User ID", example = "commenter01")
    private String authorUserId;

    @Schema(description = "댓글 작성자 닉네임", example = "댓글러")
    private String authorNickname;

    @Schema(description = "댓글 숨김 상태 (true: 숨김, false: 공개, null: 전체)", example = "false")
    private Boolean isHidden;

    @Schema(description = "댓글 작성일 검색 시작일 (YYYY-MM-DD)", example = "2025-05-01") // 설명 변경: 신고일 -> 댓글 작성일
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate commentCreatedAtStartDate; // 필드명 변경: reportStartDate -> commentCreatedAtStartDate

    @Schema(description = "댓글 작성일 검색 종료일 (YYYY-MM-DD)", example = "2025-05-31") // 설명 변경: 신고일 -> 댓글 작성일
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate commentCreatedAtEndDate; // 필드명 변경: reportEndDate -> commentCreatedAtEndDate

    // --- JPQL 쿼리용 조정된 날짜 필드 ---
    @Schema(hidden = true)
    private LocalDateTime queryCommentCreatedAtStartDate; // 필드명 변경: queryReportStartDate -> queryCommentCreatedAtStartDate

    @Schema(hidden = true)
    private LocalDateTime queryCommentCreatedAtEndDate; // 필드명 변경: queryReportEndDate -> queryCommentCreatedAtEndDate
}