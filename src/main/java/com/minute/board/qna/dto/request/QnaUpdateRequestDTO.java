package com.minute.board.qna.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "QnA 수정 요청 DTO")
public class QnaUpdateRequestDTO {

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다.")
    @Schema(description = "수정할 문의 제목", requiredMode = Schema.RequiredMode.REQUIRED, example = "수정된 문의 제목입니다.")
    private String inquiryTitle;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    @Schema(description = "수정할 문의 내용", requiredMode = Schema.RequiredMode.REQUIRED, example = "수정된 문의 내용입니다...")
    private String inquiryContent;

    @Schema(description = "삭제할 기존 첨부파일 ID 목록 (없으면 빈 리스트 또는 null)", example = "[1, 3]")
    private List<Integer> attachmentIdsToDelete;

    // 새로운 첨부파일은 컨트롤러에서 @RequestPart List<MultipartFile> newFiles 로 받습니다.
}