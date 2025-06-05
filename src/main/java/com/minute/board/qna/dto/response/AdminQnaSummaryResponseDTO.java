package com.minute.board.qna.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "관리자용 QnA 목록 조회 아이템 응답 DTO")
public class AdminQnaSummaryResponseDTO {

    @Schema(description = "문의 ID", example = "101")
    private Integer inquiryId;

    @Schema(description = "문의 제목", example = "서비스 이용 중 궁금한 점")
    private String inquiryTitle;

    @Schema(description = "작성자 User ID", example = "user123")
    private String authorUserId;

    @Schema(description = "작성자 닉네임", example = "궁금해요")
    private String authorNickname;

    @Schema(description = "문의 상태 (PENDING, ANSWERED)", example = "PENDING")
    private String inquiryStatus;

    @Schema(description = "문의 작성 시각")
    private LocalDateTime inquiryCreatedAt;

    @Schema(description = "신고 건수 (해당 문의에 대한)", example = "3")
    private long reportCount; // QnaReport 개수

    @Schema(description = "첨부파일 존재 여부", example = "true")
    private boolean hasAttachments;


    // Qna 엔티티와 신고 건수로부터 DTO를 생성하는 정적 메서드 (예시)
    // public static AdminQnaSummaryResponseDTO fromEntity(Qna qna, long reportCount) {
    //     return AdminQnaSummaryResponseDTO.builder()
    //             .inquiryId(qna.getInquiryId())
    //             .inquiryTitle(qna.getInquiryTitle())
    //             .authorUserId(qna.getUser() != null ? qna.getUser().getUserId() : "알 수 없음")
    //             .authorNickname(qna.getUser() != null ? qna.getUser().getUserNickName() : "알 수 없음")
    //             .inquiryStatus(qna.getInquiryStatus().name())
    //             .inquiryCreatedAt(qna.getInquiryCreatedAt())
    //             .reportCount(reportCount) // 서비스 계층에서 계산된 값 전달
    //             .hasAttachments(qna.getAttachments() != null && !qna.getAttachments().isEmpty())
    //             .build();
    // }
}