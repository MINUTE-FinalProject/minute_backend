package com.minute.board.free.dto.response; // 또는 com.minute.board.admin.dto 등

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "관리자용 신고된 활동 항목 (게시글 신고 또는 댓글 신고) 응답 DTO")
public class AdminReportedActivityItemDTO {

    @Schema(description = "활동 유형 (POST_REPORT: 게시글 신고, COMMENT_REPORT: 댓글 신고)", example = "POST_REPORT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String itemType;

    @Schema(description = "신고 ID (게시글 신고 ID 또는 댓글 신고 ID)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reportId;

    @Schema(description = "신고된 원본 항목 ID (게시글 ID 또는 댓글 ID)", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reportedItemId;

    @Schema(description = "신고된 항목 제목(게시글) 또는 내용 미리보기(댓글)", example = "부적절한 제목입니다.")
    private String itemTitleOrContentPreview;

    @Schema(description = "신고된 항목 작성자 닉네임", example = "문제유저")
    private String reportedItemAuthorNickname;

    @Schema(description = "신고된 항목 작성자 User ID", example = "problemUser123")
    private String reportedItemAuthorUserId;

    @Schema(description = "신고자 User ID", example = "reporterUser")
    private String reporterUserId;

    @Schema(description = "신고자 닉네임", example = "정의로운시민")
    private String reporterNickname;

    @Schema(description = "신고 일시 (이 필드를 기준으로 정렬 가능)", example = "2025-05-27T10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime reportCreatedAt; // FreeboardPostReport의 postReportDate 또는 FreeboardCommentReport의 commentReportDate

    @Schema(description = "신고된 원본 항목 생성일시", example = "2025-05-26T15:00:00")
    private LocalDateTime originalItemCreatedAt;

    @Schema(description = "신고된 원본 항목 숨김 처리 여부", example = "false")
    private boolean isItemHidden;

    // 댓글 신고의 경우, 해당 댓글이 달린 원본 게시글 ID (선택적)
    @Schema(description = "댓글 신고인 경우, 원본 게시글 ID", example = "205")
    private Integer originalPostIdForComment;
}