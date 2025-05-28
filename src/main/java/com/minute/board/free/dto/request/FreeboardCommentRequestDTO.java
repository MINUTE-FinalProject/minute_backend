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

    // 인증 기능 연동 전까지 임시로 사용할 작성자 ID
    @Schema(description = "작성자 User ID (인증 연동 전 임시)", example = "wansu00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String userId; // 실제 User 엔티티의 ID 타입과 일치

    @NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
    @Schema(description = "댓글 내용", example = "정말 유용한 정보 감사합니다!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commentContent;

    // 생성자 (필요에 따라)
    public FreeboardCommentRequestDTO(String userId, String commentContent) {
        this.userId = userId;
        this.commentContent = commentContent;
    }
}