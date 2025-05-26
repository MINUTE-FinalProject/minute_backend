package com.minute.board.free.dto.response; // 또는 admin용 DTO 패키지 (예: com.minute.board.free.dto.admin.response)

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor // JPA DTO 프로젝션을 위해 기본 생성자가 필요할 수 있음
@Schema(description = "신고된 댓글 목록의 각 항목 정보 DTO")
public class ReportedCommentEntryDTO {

    @Schema(description = "댓글 ID", example = "7")
    private Integer commentId;

    @Schema(description = "댓글 내용 (일부)", example = "이 댓글은 문제가 있어 보입니다...")
    private String commentContentPreview; // 전체 내용 대신 일부만 보여줄 수도 있습니다.

    @Schema(description = "댓글 작성자 User ID", example = "testUser")
    private String authorUserId;

    @Schema(description = "댓글 작성자 닉네임", example = "테스터")
    private String authorNickname;

    @Schema(description = "댓글 작성일시", example = "2025-05-25T14:30:00")
    private LocalDateTime commentCreatedAt;

    @Schema(description = "해당 댓글이 달린 원본 게시글 ID", example = "2")
    private Integer originalPostId;

    @Schema(description = "해당 댓글의 총 신고 횟수", example = "3")
    private Long reportCount;

    @Schema(description = "댓글 숨김 처리 여부", example = "false")
    private boolean isHidden;

    // JPA DTO 프로젝션을 위한 생성자
    public ReportedCommentEntryDTO(Integer commentId, String commentContent, String authorUserId, String authorNickname, LocalDateTime commentCreatedAt, Integer originalPostId, Long reportCount, boolean isHidden) {
        this.commentId = commentId;
        // 내용이 길 경우 일부만 잘라서 preview로 제공 (예시)
        this.commentContentPreview = (commentContent != null && commentContent.length() > 50) ? commentContent.substring(0, 50) + "..." : commentContent;
        this.authorUserId = authorUserId;
        this.authorNickname = authorNickname;
        this.commentCreatedAt = commentCreatedAt;
        this.originalPostId = originalPostId;
        this.reportCount = reportCount;
        this.isHidden = isHidden;
    }
}