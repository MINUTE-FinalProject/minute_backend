package com.minute.board.free.dto.response; // 패키지 경로는 실제 프로젝트에 맞게 조정해주세요.

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder // 빌더 패턴 사용 (Service에서 DTO 변환 시 유용)
@Schema(description = "자유게시판 댓글 조회 응답 DTO")
public class FreeboardCommentResponseDTO {

    @Schema(description = "댓글 ID", example = "1")
    private Integer commentId;

    @Schema(description = "댓글 내용", example = "정말 유용한 정보 감사합니다!")
    private String commentContent;

    @Schema(description = "댓글 좋아요 수", example = "5")
    private int commentLikeCount;

    @Schema(description = "댓글 숨김 여부", example = "false")
    private boolean commentIsHidden;

    @Schema(description = "댓글 작성일시", example = "2025-05-25T14:30:00")
    private LocalDateTime commentCreatedAt;

    @Schema(description = "댓글 수정일시", example = "2025-05-25T14:35:00")
    private LocalDateTime commentUpdatedAt;

    @Schema(description = "작성자 User ID", example = "wansu00")
    private String userId;

    @Schema(description = "작성자 닉네임", example = "완수최고")
    private String userNickName;

    @Schema(description = "댓글이 달린 게시글 ID", example = "101")
    private Integer postId;

    // <<< 추가된 필드 >>>
    @Schema(description = "현재 로그인한 사용자의 해당 댓글 좋아요 여부", example = "true")
    private boolean isLikedByCurrentUser;
}