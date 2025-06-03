package com.minute.board.qna.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "사용자용 QnA 목록 조회 아이템 응답 DTO")
public class QnaSummaryResponseDTO {

    @Schema(description = "문의 ID", example = "101")
    private Integer inquiryId;

    @Schema(description = "문의 제목", example = "결제 관련 문의입니다.")
    private String inquiryTitle;

    @Schema(description = "작성자 닉네임", example = "행복한쿼카")
    private String authorNickname;

    @Schema(description = "문의 상태 (PENDING, ANSWERED)", example = "ANSWERED")
    private String inquiryStatus; // QnaStatus Enum 값을 문자열로

    @Schema(description = "문의 작성 시각")
    private LocalDateTime inquiryCreatedAt;

    @Schema(description = "첨부파일 존재 여부", example = "true")
    private boolean hasAttachments;

    // Qna 엔티티로부터 DTO를 생성하는 정적 메서드 (예시)
    // public static QnaSummaryResponseDTO fromEntity(Qna qna) {
    //     return QnaSummaryResponseDTO.builder()
    //             .inquiryId(qna.getInquiryId())
    //             .inquiryTitle(qna.getInquiryTitle())
    //             .authorNickname(qna.getUser() != null ? qna.getUser().getUserNickName() : "알 수 없음")
    //             .inquiryStatus(qna.getInquiryStatus().name())
    //             .inquiryCreatedAt(qna.getInquiryCreatedAt())
    //             .hasAttachments(qna.getAttachments() != null && !qna.getAttachments().isEmpty())
    //             .build();
    // }
}