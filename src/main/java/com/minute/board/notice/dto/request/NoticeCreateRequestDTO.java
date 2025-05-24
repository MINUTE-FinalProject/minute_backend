package com.minute.board.notice.dto.request; // 실제 패키지 경로에 맞게 수정해주세요.

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank; // 유효성 검사를 위해 추가
import jakarta.validation.constraints.Size;    // 유효성 검사를 위해 추가
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter // 컨트롤러에서 @RequestBody로 받으려면 Setter 또는 모든 필드를 받는 생성자가 필요할 수 있습니다.
@NoArgsConstructor // 기본 생성자
@Schema(description = "공지사항 생성 요청 DTO")
public class NoticeCreateRequestDTO {

    @NotBlank(message = "제목은 필수 입력 항목입니다.") // 제목은 비어있을 수 없음
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다.") // 제목 길이 제한
    @Schema(description = "공지사항 제목", example = "새로운 이벤트 안내", requiredMode = Schema.RequiredMode.REQUIRED)
    private String noticeTitle;

    @NotBlank(message = "내용은 필수 입력 항목입니다.") // 내용도 비어있을 수 없음
    @Schema(description = "공지사항 내용", example = "푸짐한 경품이 가득한 이벤트를 지금 바로 만나보세요!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String noticeContent;

    @Schema(description = "중요 공지 여부 (기본값: false)", example = "false", defaultValue = "false")
    private boolean noticeIsImportant = false; // 기본값을 false로 설정

    // 작성자 ID는 이 DTO로 받지 않습니다.
    // 대신, API를 호출하는 인증된 사용자(관리자)의 정보를 Spring Security를 통해 가져와서 사용할 것입니다.
}