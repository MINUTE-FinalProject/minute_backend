package com.minute.board.free.dto.request; // 패키지 경로는 실제 프로젝트에 맞게 조정해주세요.

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "자유게시판 댓글 생성 및 수정 요청 DTO")
public class FreeboardCommentRequestDTO {

    // userId 필드가 제거되었습니다.

    @NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
    @Schema(description = "댓글 내용", example = "정말 유용한 정보 감사합니다!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commentContent;

    // userId가 제거된 생성자 (필요에 따라 유지 또는 다른 형태로 수정)
    public FreeboardCommentRequestDTO(String commentContent) {
        this.commentContent = commentContent;
    }
}