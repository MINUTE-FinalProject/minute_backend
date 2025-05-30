package com.minute.board.free.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime; // LocalDateTime 사용 시

@Getter
@Setter
@Schema(description = "관리자용 신고된 게시글 목록 검색/필터 조건 DTO")
public class AdminReportedPostFilterDTO {

    // ... (다른 필드들은 동일)
    @Schema(description = "게시글 ID로 검색", example = "101")
    private Integer postId;

    @Schema(description = "게시글 작성자 User ID로 검색", example = "wansu00")
    private String authorUserId;

    @Schema(description = "게시글 작성자 닉네임으로 검색", example = "완수최고")
    private String authorNickname;

    @Schema(description = "게시글 제목으로 검색", example = "이벤트")
    private String postTitle;

    @Schema(description = "검색 키워드 (내용 등에 사용)", example = "부적절")
    private String searchKeyword;

    @Schema(description = "게시글 숨김 상태 필터 (true: 숨김, false: 공개, null: 전체)", example = "false")
    private Boolean isHidden;

    @Schema(description = "게시글 작성일 검색 시작일 (YYYY-MM-DD)", example = "2025-01-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate postStartDate;

    @Schema(description = "게시글 작성일 검색 종료일 (YYYY-MM-DD)", example = "2025-01-31")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate postEndDate;

    // --- JPQL 쿼리용 조정된 날짜 필드 ---
    @Schema(hidden = true)
    private LocalDateTime queryPostStartDate;

    @Schema(hidden = true)
    private LocalDateTime queryPostEndDate;
}