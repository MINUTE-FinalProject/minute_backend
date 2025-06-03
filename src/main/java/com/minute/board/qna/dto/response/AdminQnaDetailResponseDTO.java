package com.minute.board.qna.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "관리자용 QnA 상세 조회 응답 DTO")
public class AdminQnaDetailResponseDTO {

    @Schema(description = "문의 ID", example = "101")
    private Integer inquiryId;

    @Schema(description = "문의 제목", example = "서비스 이용 중 궁금한 점")
    private String inquiryTitle;

    @Schema(description = "문의 내용", example = "이용 중 특정 기능이...")
    private String inquiryContent;

    @Schema(description = "작성자 User ID", example = "user123")
    private String authorUserId;

    @Schema(description = "작성자 닉네임", example = "궁금해요")
    private String authorNickname;

    @Schema(description = "문의 상태 (PENDING, ANSWERED)", example = "PENDING")
    private String inquiryStatus;

    @Schema(description = "문의 작성 시각")
    private LocalDateTime inquiryCreatedAt;

    @Schema(description = "문의 수정 시각")
    private LocalDateTime inquiryUpdatedAt;

    @Schema(description = "첨부파일 목록")
    private List<QnaAttachmentResponseDTO> attachments; // 내부적으로 S3 URL 포함

    @Schema(description = "답변 정보 (없을 경우 null)")
    private QnaReplyResponseDTO reply;

    @Schema(description = "해당 문의에 대한 신고 건수", example = "0")
    private long reportCount; // QnaReport 개수

    // Qna 엔티티 등에서 이 DTO를 생성하는 정적 메서드 (예시)
    // public static AdminQnaDetailResponseDTO fromEntity(Qna qna, List<QnaAttachmentResponseDTO> attachmentDTOs, QnaReplyResponseDTO replyDTO, long reportCount) {
    //     return AdminQnaDetailResponseDTO.builder()
    //             .inquiryId(qna.getInquiryId())
    //             .inquiryTitle(qna.getInquiryTitle())
    //             .inquiryContent(qna.getInquiryContent())
    //             .authorUserId(qna.getUser() != null ? qna.getUser().getUserId() : "알 수 없음")
    //             .authorNickname(qna.getUser() != null ? qna.getUser().getUserNickName() : "알 수 없음")
    //             .inquiryStatus(qna.getInquiryStatus().name())
    //             .inquiryCreatedAt(qna.getInquiryCreatedAt())
    //             .inquiryUpdatedAt(qna.getInquiryUpdatedAt())
    //             .attachments(attachmentDTOs)
    //             .reply(replyDTO)
    //             .reportCount(reportCount)
    //             .build();
    // }
}