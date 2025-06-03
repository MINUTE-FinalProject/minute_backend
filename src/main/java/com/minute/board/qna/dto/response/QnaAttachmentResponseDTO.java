package com.minute.board.qna.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "QnA 첨부파일 응답 DTO")
public class QnaAttachmentResponseDTO {

    @Schema(description = "첨부파일 ID (DB상 ID)", example = "1")
    private Integer imgId;

    @Schema(description = "파일 S3 URL", example = "https://minuteproject.s3.ap-northeast-2.amazonaws.com/qna/uuid_filename.jpg")
    private String fileUrl; // 필드명을 S3 URL임을 명확히 하기 위해 변경 (기존: imgFilePath)

    @Schema(description = "원본 파일명", example = "증빙자료.jpg")
    private String originalFilename; // 필드명 변경 (기존: imgOriginalFilename)

    @Schema(description = "업로드된 시각")
    private LocalDateTime createdAt; // 필드명 변경 (기존: imgCreatedAt)

    // QnaAttachment 엔티티와 S3 파일 URL로부터 DTO를 생성하는 정적 메서드 (예시)
    // public static QnaAttachmentResponseDTO fromEntity(QnaAttachment attachment, String s3FileUrl) {
    //     return QnaAttachmentResponseDTO.builder()
    //             .imgId(attachment.getImgId())
    //             .fileUrl(s3FileUrl) // 서비스 계층에서 S3 URL을 생성하여 전달
    //             .originalFilename(attachment.getImgOriginalFilename())
    //             .createdAt(attachment.getImgCreatedAt())
    //             .build();
    // }
}