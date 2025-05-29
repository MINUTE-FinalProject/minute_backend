package com.minute.board.free.dto.request; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "댓글 공개/숨김 상태 변경 요청 DTO")
public class CommentVisibilityRequestDTO {

    @NotNull(message = "숨김 여부(isHidden) 값은 필수입니다.")
    @Schema(description = "댓글 숨김 처리 여부 (true: 숨김, false: 공개)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isHidden;

    public CommentVisibilityRequestDTO(Boolean isHidden) {
        this.isHidden = isHidden;
    }
}