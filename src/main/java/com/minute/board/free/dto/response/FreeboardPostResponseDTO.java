package com.minute.board.free.dto.response; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder // 빌더 패턴 사용 (Service에서 DTO 변환 시 유용)
@Schema(description = "자유게시판 게시글 상세 조회 응답 DTO")
public class FreeboardPostResponseDTO {

    @Schema(description = "게시글 ID", example = "1")
    private Integer postId;

    @Schema(description = "게시글 제목", example = "오늘 날씨가 좋네요!")
    private String postTitle;

    @Schema(description = "게시글 내용", example = "산책하기 좋은 날씨입니다. 다들 뭐하시나요?")
    private String postContent;

    @Schema(description = "게시글 조회수", example = "105")
    private int postViewCount;

    @Schema(description = "게시글 좋아요수", example = "15")
    private int postLikeCount;

    @Schema(description = "게시글 숨김 여부 (true: 숨김, false: 공개)", example = "false")
    private boolean postIsHidden; // DB는 TINYINT(1) 이지만 Java에서는 boolean

    @Schema(description = "게시글 작성일시", example = "2025-05-24T10:30:00")
    private LocalDateTime postCreatedAt;

    @Schema(description = "게시글 수정일시", example = "2025-05-24T11:00:00")
    private LocalDateTime postUpdatedAt;

    @Schema(description = "작성자 User ID", example = "testUser123")
    private String userId; // User 엔티티의 ID

    @Schema(description = "작성자 닉네임", example = "날씨요정")
    private String userNickName; // User 엔티티에서 가져올 닉네임

    // <<< 추가된 필드 >>>
    @Schema(description = "현재 로그인한 사용자의 해당 게시글 좋아요 여부", example = "true")
    private boolean isLikedByCurrentUser;

    @Schema(description = "현재 로그인한 사용자의 해당 게시글 신고 여부", example = "false")
    private boolean isReportedByCurrentUser;
}