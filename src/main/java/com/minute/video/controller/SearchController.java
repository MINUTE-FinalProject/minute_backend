package com.minute.video.controller;

import com.minute.video.dto.SearchHistoryRequestDTO;
import com.minute.video.dto.SearchHistoryResponseDTO;
import com.minute.video.dto.SearchSuggestionsDTO;
import com.minute.video.service.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "검색 관련 API")
@RequiredArgsConstructor
public class SearchController {

    private final SearchHistoryService searchHistoryService;

    @Operation(summary = "검색 실행 및 기록 저장", description ="사용자가 입력한 키워드로 영상을 검색하고, 검색 히스토리에 해당 키워드를 저장합니다.")
    @PostMapping
    public ResponseEntity<Void> saveSearch(@RequestBody SearchHistoryRequestDTO dto){
        searchHistoryService.saveSearchHistory(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "최근 검색어 조회", description ="해당 사용자의 최근 검색어 목록을 최신순으로 반환합니다.")
    @GetMapping("/history")
    public List<SearchHistoryResponseDTO> getHistory(@RequestParam String userId){
        return searchHistoryService.getUserSearchHistory(userId);
    }

    @Operation(summary = "최근 검색어 삭제", description = "사용자의 특정 검색 기록을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "검색 기록이 정상적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다. userId 또는 검색어를 확인해 주세요."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
    })
    @DeleteMapping("/history")
    public ResponseEntity<Void> deleteSearchHistory(
            @RequestParam String userId,
            @RequestParam String keyword) {
        searchHistoryService.deleteSearchHistory(userId, keyword);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "인기 검색어 조회", description ="전체 사용자 기준 상위 5개 인기 검색어를 반환합니다.")
    @GetMapping("/popular")
    public List<String> popularKeywords(){
        return searchHistoryService.getPopularKeywords();
    }

    @Operation(summary = "검색어 제안", description ="검색창 포커스 시 사용자의 최근 검색어와 인기 검색어를 함께 제공합니다.")
    @GetMapping("/suggestions")
    public SearchSuggestionsDTO getSuggestions(@RequestParam String userId){
        return searchHistoryService.getSearchSuggestions(userId);
    }

}
