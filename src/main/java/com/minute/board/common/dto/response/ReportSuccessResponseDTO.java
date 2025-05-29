package com.minute.board.common.dto.response; // 공통 응답 DTO 패키지로 이동 고려

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "신고 처리 성공 응답 DTO")
public class ReportSuccessResponseDTO {

    @Schema(description = "처리 결과 메시지", example = "게시글이 성공적으로 신고되었습니다.")
    private String message;

    @Schema(description = "신고된 대상의 ID (게시글 ID 또는 댓글 ID)", example = "101")
    private Integer targetId;
}