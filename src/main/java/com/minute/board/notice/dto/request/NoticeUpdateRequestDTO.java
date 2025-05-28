package com.minute.board.notice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size; // 선택적 필드에도 길이 제한은 유효할 수 있음
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "공지사항 수정 요청 DTO. 모든 필드는 선택 사항이며, 제공된 필드만 업데이트됩니다.")
public class NoticeUpdateRequestDTO {

    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다.")
    @Schema(description = "수정할 공지사항 제목 (선택 사항)", example = "서버 점검 시간 변경 안내")
    private String noticeTitle;

    @Schema(description = "수정할 공지사항 내용 (선택 사항)", example = "서버 점검 시간이 1시간 앞당겨졌습니다.")
    private String noticeContent;

    @Schema(description = "수정할 중요 공지 여부 (선택 사항)", example = "true")
    private Boolean noticeIsImportant; // boolean 대신 Boolean을 사용하여 null 값 허용 (수정 안 함을 의미)
}