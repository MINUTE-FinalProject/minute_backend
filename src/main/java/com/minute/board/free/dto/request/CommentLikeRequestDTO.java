package com.minute.board.free.dto.request; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "댓글 좋아요 요청 DTO")
public class CommentLikeRequestDTO {

    @NotBlank(message = "사용자 ID는 필수입니다.")
    @Schema(description = "좋아요를 누르는 사용자 User ID (인증 연동 전 임시)", example = "wansu00", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    public CommentLikeRequestDTO(String userId) {
        this.userId = userId;
    }
}