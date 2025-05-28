package com.minute.board.free.dto.request; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import io.swagger.v3.oas.annotations.media.Schema;
// import jakarta.validation.constraints.NotBlank; // 더 이상 필요하지 않음
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "댓글 신고 요청 DTO (인증 연동 후에는 내용이 없을 수 있습니다)")
public class CommentReportRequestDTO {

    // userId 필드가 제거되어 내용이 없는 DTO가 되었습니다.
    // 댓글 신고 기능도 신고하는 사용자(인증 정보)와 대상 댓글(경로 변수)만으로 처리 가능합니다.
    // 만약 신고 사유 등의 추가 정보가 필요하다면 여기에 필드를 추가할 수 있습니다.

}