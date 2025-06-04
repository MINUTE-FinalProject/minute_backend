package com.minute.board.qna.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "관리자용 신고된 QnA 목록 아이템 응답 DTO")
public class ReportedQnaItemResponseDTO {

    @Schema(description = "신고 대상 QnA ID (inquiryId)", example = "101")
    private Integer id; // 프론트엔드의 item.id 와 맞추기 위함

    @Schema(description = "항목 타입 (항상 'QNA' 또는 'INQUIRY')", example = "QNA")
    private String itemType; // 프론트엔드에서 게시글/댓글/문의 구분용

    @Schema(description = "QnA 작성자 User ID", example = "user123")
    private String authorId;

    @Schema(description = "QnA 작성자 닉네임", example = "궁금해요")
    private String authorNickname;

    @Schema(description = "QnA 제목", example = "이용 중 특정 기능 문의")
    private String titleOrContentSnippet; // 프론트엔드의 titleOrContentSnippet 필드명 유지 (QnA 제목을 담음)

    @Schema(description = "QnA 작성일")
    private LocalDateTime originalPostDate; // 프론트엔드의 originalPostDate 필드명 유지 (inquiryCreatedAt)

    @Schema(description = "해당 QnA에 대한 (관리자) 누적 신고 건수", example = "1")
    private long reportCount;

    // QnA는 별도의 숨김/공개 처리가 없으므로 hiddenStatus, isItemHiddenBoolean 필드는 제외합니다.
}