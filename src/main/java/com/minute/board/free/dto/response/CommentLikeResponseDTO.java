package com.minute.board.free.dto.response; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "댓글 좋아요 처리 응답 DTO")
public class CommentLikeResponseDTO {

    @Schema(description = "댓글 ID", example = "1")
    private Integer commentId;

    @Schema(description = "현재 댓글의 총 좋아요 수", example = "5")
    private int currentLikeCount;

    @Schema(description = "현재 요청한 사용자가 이 댓글을 좋아요 했는지 여부", example = "true")
    private boolean likedByCurrentUser;
}