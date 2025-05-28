package com.minute.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "TagResponseDTO", description = "태그 목록 조회 응답 DTO")
public class TagResponseDTO {

    @Schema(description = "태그 고유 ID", example = "123")
    private int tagId;
    @Schema(description = "태그 이름", example = "제주도")
    private String tagName;
}
