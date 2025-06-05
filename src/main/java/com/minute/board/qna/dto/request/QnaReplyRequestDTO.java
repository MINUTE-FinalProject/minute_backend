package com.minute.board.qna.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "QnA 답변 작성/수정 요청 DTO")
public class QnaReplyRequestDTO {

    @NotBlank(message = "답변 내용은 필수 입력 항목입니다.")
    @Schema(description = "답변 내용", requiredMode = Schema.RequiredMode.REQUIRED, example = "답변드립니다...")
    private String replyContent;
}