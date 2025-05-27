package com.minute.board.free.dto.response; // DTO의 실제 패키지 경로

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor; // 추가
import lombok.Builder;      // <<< 추가
import lombok.Getter;
import lombok.NoArgsConstructor; // 추가

import java.time.LocalDateTime;

@Getter
@Builder      // <<< @Builder 어노테이션 추가
@NoArgsConstructor // 추가 (빌더와 함께 사용 시, 또는 다른 프레임워크 호환성)
@AllArgsConstructor // 추가 (빌더 내부 로직 및 JPA DTO 프로젝션 시 필요할 수 있음)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "신고된 댓글 항목 응답 DTO") // AdminReportedActivityItemDTO 와 필드를 맞추거나, 별도 DTO로 관리
public class ReportedCommentEntryDTO {

    @Schema(description = "활동 유형 (COMMENT_REPORT 고정)", example = "COMMENT_REPORT")
    private String itemType; // AdminReportedActivityItemDTO 와 통일성을 위해 추가 고려

    @Schema(description = "신고 ID (댓글 신고 ID)", example = "1")
    private Integer reportId;

    @Schema(description = "신고된 원본 댓글 ID", example = "7")
    private Integer reportedItemId; // commentId

    @Schema(description = "신고된 댓글 내용 미리보기", example = "이 댓글은 문제가 있어 보입니다...")
    private String itemTitleOrContentPreview; // commentContentPreview

    @Schema(description = "신고된 댓글 작성자 User ID", example = "commenter01")
    private String reportedItemAuthorUserId;

    @Schema(description = "신고된 댓글 작성자 닉네임", example = "댓글러")
    private String reportedItemAuthorNickname;

    @Schema(description = "신고자 User ID", example = "reporterUser456")
    private String reporterUserId;

    @Schema(description = "신고자 닉네임", example = "정의로운신고맨")
    private String reporterNickname;

    @Schema(description = "신고 일시", example = "2025-05-28T10:00:00")
    private LocalDateTime reportCreatedAt;

    @Schema(description = "신고된 원본 댓글 생성일시", example = "2025-05-27T15:00:00")
    private LocalDateTime originalItemCreatedAt;

    @Schema(description = "신고된 원본 댓글 숨김 처리 여부", example = "false")
    private boolean isItemHidden;

    @Schema(description = "댓글 신고인 경우, 원본 게시글 ID", example = "2")
    private Integer originalPostIdForComment;

    // 빌더를 사용하므로, 모든 필드를 받는 생성자는 Lombok의 @AllArgsConstructor가 자동으로 만들어줍니다.
    // 만약 @Builder.Default를 사용하는 필드가 있다면 @AllArgsConstructor만으로는 부족하고,
    // 수동으로 생성자를 만들거나 @Builder와 함께 @NoArgsConstructor, @AllArgsConstructor를 모두 사용하는 것이 일반적입니다.
}