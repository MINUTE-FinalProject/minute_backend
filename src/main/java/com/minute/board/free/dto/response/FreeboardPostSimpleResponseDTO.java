package com.minute.board.free.dto.response; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "자유게시판 게시글 목록 아이템 응답 DTO")
public class FreeboardPostSimpleResponseDTO {

    @Schema(description = "게시글 ID", example = "1")
    private Integer postId;

    @Schema(description = "게시글 제목", example = "오늘 날씨가 좋네요!")
    private String postTitle;

    @Schema(description = "게시글 조회수", example = "105")
    private int postViewCount;

    @Schema(description = "게시글 좋아요수", example = "15")
    private int postLikeCount;

    @Schema(description = "게시글 작성일시", example = "2025-05-24T10:30:00")
    private LocalDateTime postCreatedAt;

    @Schema(description = "작성자 User ID", example = "testUser123")
    private String userId;

    @Schema(description = "작성자 닉네임", example = "날씨요정")
    private String userNickName;

    // 목록에서는 댓글 수를 함께 보여주는 것도 좋음 (필요 시 추가)
    // @Schema(description = "댓글 수", example = "5")
    // private int commentCount;
}