package com.minute.board.free.dto.request; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank; // Jakarta Bean Validation 사용
import jakarta.validation.constraints.Size;   // Jakarta Bean Validation 사용
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // Jackson 등에서 JSON 변환 시 기본 생성자가 필요할 수 있습니다.
@Schema(description = "자유게시판 게시글 생성 및 수정 요청 DTO")
public class FreeboardPostRequestDTO {

    // 인증 기능 연동 전까지 임시로 사용할 작성자 ID
    // 인증 연동 후에는 @AuthenticationPrincipal 등을 통해 서버에서 직접 가져오므로,
    // 이 필드는 제거되거나 다른 방식으로 처리될 수 있습니다.
    @Schema(description = "작성자 User ID (인증 연동 전 임시)", example = "testUser123", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String userId; // 실제 User 엔티티의 ID 타입과 일치시켜야 합니다. (현재 String으로 가정)

    @NotBlank(message = "제목은 필수 입력 항목입니다.") // 유효성 검사: 비어있거나 공백만 있을 수 없음
    @Size(min = 1, max = 255, message = "제목은 1자 이상 255자 이하로 입력해주세요.") // 유효성 검사: 길이 제한
    @Schema(description = "게시글 제목", example = "오늘 날씨가 좋네요!", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
    private String postTitle;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    @Schema(description = "게시글 내용", example = "산책하기 좋은 날씨입니다. 다들 뭐하시나요?", requiredMode = Schema.RequiredMode.REQUIRED)
    private String postContent;

    // 생성자 (필요에 따라 추가)
    public FreeboardPostRequestDTO(String userId, String postTitle, String postContent) {
        this.userId = userId;
        this.postTitle = postTitle;
        this.postContent = postContent;
    }
}