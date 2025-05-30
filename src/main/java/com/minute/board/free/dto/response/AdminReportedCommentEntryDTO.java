package com.minute.board.free.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder // Builder는 있어도 되고 없어도 되지만, 생성자는 필수
@NoArgsConstructor
@AllArgsConstructor // 이 어노테이션이 JPQL SELECT NEW 순서와 맞는 생성자를 만들어줍니다.
@Schema(description = "관리자용 신고된 댓글 요약 정보 DTO")
public class AdminReportedCommentEntryDTO {

    // JPQL SELECT 순서: c.commentId, c.commentContent, u.userId, u.userNickName, c.commentCreatedAt, p.postId, COUNT(r.commentReportId), c.commentIsHidden

    @Schema(description = "댓글 ID", example = "7")
    private Integer commentId;

    @Schema(description = "댓글 내용 (전체 또는 미리보기 - JPQL에서 c.commentContent를 가져오므로 전체 내용)")
    private String commentContent; // JPQL에서 c.commentContent를 가져오므로 contentPreview 대신 사용

    @Schema(description = "댓글 작성자 User ID", example = "commenter01")
    private String authorUserId;

    @Schema(description = "댓글 작성자 닉네임", example = "댓글러")
    private String authorNickname;

    @Schema(description = "댓글 작성일시", example = "2025-05-25T14:30:00")
    private LocalDateTime commentCreatedAt;

    @Schema(description = "댓글이 달린 원본 게시글 ID", example = "2")
    private Integer originalPostId; // p.postId에 해당

    @Schema(description = "해당 댓글의 총 신고 횟수", example = "3")
    private Long reportCount; // COUNT(r.commentReportId)에 해당

    @Schema(description = "댓글 숨김 처리 여부", example = "false")
    private boolean isHidden; // c.commentIsHidden에 해당
}