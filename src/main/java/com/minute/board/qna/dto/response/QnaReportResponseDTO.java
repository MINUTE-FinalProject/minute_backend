package com.minute.board.qna.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "QnA 신고 처리 결과 응답 DTO")
public class QnaReportResponseDTO {

    @Schema(description = "생성된 (또는 기존) 신고 ID", example = "50", nullable = true)
    private Integer reportId;

    @Schema(description = "신고된 문의 ID", example = "101")
    private Integer qnaId;

    @Schema(description = "신고한 관리자 User ID", example = "admin01")
    private String reporterUserId;

    @Schema(description = "신고된 시각 (이미 신고된 경우 기존 신고 시각)", example = "2025-06-04T14:30:00")
    private LocalDateTime reportedAt;

    @Schema(description = "처리 결과 메시지", example = "문의가 성공적으로 신고되었습니다.")
    private String message;
}