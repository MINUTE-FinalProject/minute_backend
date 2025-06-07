package com.minute.mypage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 마이페이지 통계 정보 응답 DTO")
public class AdminStatsResponseDTO {

    @Schema(description = "전체 문의(QnA) 수", example = "19")
    private final long qnaCount;

    @Schema(description = "전체 공지사항 수", example = "3")
    private final long noticeCount;

    // 필요하다면 나중에 다른 통계 정보도 여기에 추가할 수 있습니다.
    // 예:
    // @Schema(description = "전체 회원 수", example = "150")
    // private final long totalUsersCount;
    //
    // @Schema(description = "신고된 회원 수", example = "5")
    // private final long reportedUsersCount;
}
