package com.minute.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SearchSuggestionDTO", description = "검색창 포커스 시 반환할 최근/인기 검색어")
public class SearchSuggestionsDTO {
    // 검색창 포커스 시 최근검색 + 인기검색 응답

    @Schema(description = "사용자의 최근 검색어 목록")
    private List<String> recentKeywords;

    @Schema(description = "전체 인기 검색어 목록")
    private List<String> popularKeywords;
}
