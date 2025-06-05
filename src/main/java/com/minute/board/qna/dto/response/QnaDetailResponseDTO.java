package com.minute.board.qna.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "사용자용 QnA 상세 조회 응답 DTO")
public class QnaDetailResponseDTO {

    @Schema(description = "문의 ID", example = "101")
    private Integer inquiryId;

    @Schema(description = "문의 제목", example = "결제 관련 문의입니다.")
    private String inquiryTitle;

    @Schema(description = "문의 내용", example = "안녕하세요, 결제 시 오류가 발생하여 문의드립니다...")
    private String inquiryContent;

    @Schema(description = "작성자 닉네임", example = "행복한쿼카")
    private String authorNickname;

    @Schema(description = "문의 상태 (PENDING, ANSWERED)", example = "ANSWERED")
    private String inquiryStatus;

    @Schema(description = "문의 작성 시각")
    private LocalDateTime inquiryCreatedAt;

    @Schema(description = "문의 수정 시각")
    private LocalDateTime inquiryUpdatedAt;

    @Schema(description = "첨부파일 목록")
    private List<QnaAttachmentResponseDTO> attachments; // 내부적으로 S3 URL 포함

    @Schema(description = "답변 정보 (없을 경우 null)")
    private QnaReplyResponseDTO reply;

    // Qna 엔티티, 첨부파일 DTO 목록, 답변 DTO로부터 이 DTO를 생성하는 정적 메서드 (예시)
    // public static QnaDetailResponseDTO fromEntity(Qna qna, List<QnaAttachmentResponseDTO> attachmentDTOs, QnaReplyResponseDTO replyDTO) {
    //     return QnaDetailResponseDTO.builder()
    //             .inquiryId(qna.getInquiryId())
    //             .inquiryTitle(qna.getInquiryTitle())
    //             .inquiryContent(qna.getInquiryContent())
    //             .authorNickname(qna.getUser() != null ? qna.getUser().getUserNickName() : "알 수 없음")
    //             .inquiryStatus(qna.getInquiryStatus().name())
    //             .inquiryCreatedAt(qna.getInquiryCreatedAt())
    //             .inquiryUpdatedAt(qna.getInquiryUpdatedAt())
    //             .attachments(attachmentDTOs)
    //             .reply(replyDTO)
    //             .build();
    // }
}