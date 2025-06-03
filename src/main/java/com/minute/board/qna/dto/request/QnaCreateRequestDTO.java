package com.minute.board.qna.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "QnA 생성 요청 DTO")
public class QnaCreateRequestDTO {

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다.")
    @Schema(description = "문의 제목", requiredMode = Schema.RequiredMode.REQUIRED, example = "새로운 문의 제목입니다.")
    private String inquiryTitle;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    @Schema(description = "문의 내용", requiredMode = Schema.RequiredMode.REQUIRED, example = "문의 내용입니다...")
    private String inquiryContent;

    // 첨부파일은 컨트롤러에서 @RequestPart List<MultipartFile> files 로 받습니다.
}