package com.minute.board.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema; // Schema 임포트
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Schema(description = "공지사항 목록 항목 응답 DTO") // 클래스에 대한 설명
@Getter
@Builder
public class NoticeListResponseDTO {

    @Schema(description = "공지사항 ID", example = "1")
    private Integer noticeId;

    @Schema(description = "공지사항 제목", example = "서버 점검 안내")
    private String noticeTitle;

    @Schema(description = "작성자 ID (User의 userId)", example = "adminUser")
    private String authorId;

    @Schema(description = "작성자 닉네임", example = "관리자")
    private String authorNickname;

    @Schema(description = "작성일", example = "2025-05-21T10:00:00")
    private LocalDateTime noticeCreatedAt;

    @Schema(description = "조회수", example = "150")
    private int noticeViewCount;

    @Schema(description = "중요 공지 여부", example = "true")
    private boolean noticeIsImportant;
}