package com.minute.board.free.dto.request; // 패키지 경로는 실제 프로젝트에 맞게 조정해주세요.

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull; // boolean 타입에는 @NotNull 사용
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "게시글 공개/숨김 상태 변경 요청 DTO")
public class PostVisibilityRequestDTO {

    @NotNull(message = "숨김 여부(isHidden) 값은 필수입니다.")
    @Schema(description = "게시글 숨김 처리 여부 (true: 숨김, false: 공개)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isHidden; // Wrapper 클래스 Boolean 사용 (null 허용 여부에 따라)

    public PostVisibilityRequestDTO(Boolean isHidden) {
        this.isHidden = isHidden;
    }
}