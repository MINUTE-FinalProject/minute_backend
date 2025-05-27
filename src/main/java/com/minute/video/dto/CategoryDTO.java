package com.minute.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "CategoryDTO", description = "카테고리 목록 조회 DTO")
public class CategoryDTO {

    @Schema(description = "카테고리 고유 ID", example = "1")
    private int categoryId;
    @Schema(description = "카테고리 이름", example = "여행")
    private String categoryName;
    @Schema(description = "유튜브 검색 키워드", example = "제주 여행")
    private String youtubeKeyword;
}
