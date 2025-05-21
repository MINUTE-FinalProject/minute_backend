package com.minute.board.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "공지사항 상세 조회 응답 DTO")
public class NoticeDetailResponseDTO {

    @Schema(description = "공지사항 ID", example = "1")
    private Integer noticeId;

    @Schema(description = "공지사항 제목", example = "서버 점검 안내")
    private String noticeTitle;

    @Schema(description = "공지사항 내용", example = "안녕하세요. 서버 점검이 있을 예정입니다...")
    private String noticeContent;

    @Schema(description = "작성자 ID (User의 userId)", example = "adminUser")
    private String authorId;

    @Schema(description = "작성자 닉네임", example = "관리자")
    private String authorNickname;

    @Schema(description = "작성일", example = "2025-05-21T10:00:00")
    private LocalDateTime noticeCreatedAt;

    @Schema(description = "조회수", example = "153")
    private int noticeViewCount;

    @Schema(description = "중요 공지 여부", example = "true")
    private boolean noticeIsImportant;

    // 필요하다면 이전 글/다음 글 정보도 추가할 수 있습니다. (선택 사항)
    // private SimpleNoticeInfo prevNotice;
    // private SimpleNoticeInfo nextNotice;
}