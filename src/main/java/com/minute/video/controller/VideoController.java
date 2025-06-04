package com.minute.video.controller;

import com.minute.video.dto.CategoryDTO;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.service.CategoryService;
import com.minute.video.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Video", description = "영상 관련 API")
public class VideoController {

    private final VideoService videoService;
    private final CategoryService categoryService;

    @Operation(
            summary = "영상 목록 조회",
            description =
                    "영상 목록을 조회합니다.\n\n" +
                            "1) `keyword` 파라미터가 있으면 제목 검색 결과를 반환합니다.\n" +
                            "2) `category` 파라미터가 있으면 해당 카테고리별 영상 목록을 반환합니다.\n" +
                            "3) `tag` 파라미터가 있으면 해당 태그별 영상 목록을 반환합니다.\n" +
                            "4) `userId` 파라미터만 있으면 로그인된 사용자의 맞춤 추천 영상을 반환합니다.\n" +
                            "5) 아무 파라미터도 없으면 좋아요 수 기준 인기 영상을 반환합니다.\n" +
                            "   (만약 좋아요 데이터가 없으면 조회수 기준, 조회수 데이터도 없으면 최신순 영상 목록을 반환)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상적으로 영상 목록을 반환합니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터가 전달되었습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @GetMapping
    public List<VideoResponseDTO> getVideos(
            @Parameter(description = "제목 검색 키워드")   @RequestParam(required = false) String keyword,
            @Parameter(description = "카테고리 필터")     @RequestParam(required = false) String category,
            @Parameter(description = "태그 필터")         @RequestParam(required = false) String tag,
            @Parameter(description = "로그인된 사용자 ID") @RequestParam(required = false) String userId
    ) {
        // 검색어가 있으면 “DB + API” 통합 검색으로 바꿔 줍니다.
        if (keyword != null && !keyword.isBlank()) {
            // 예시: API에서 최대 50개 까지 가져온다고 가정
            return videoService.searchMixedVideos(keyword, 50);
        }

        // 2) category 필터가 있으면 그 카테고리 영상만 반환
        if (category != null && !category.isBlank()) {
            return videoService.getVideoByCategory(category);
        }

        // 3) tag 필터가 있으면 그 태그 영상만 반환
        if (tag != null && !tag.isBlank()) {
            return videoService.getVideosByTag(tag);
        }

        // 4) userId만 있으면 추천 영상 목록을 반환
        if (userId != null && !userId.isBlank()) {
            return videoService.getRecommendedVideos(userId);
        }

        // 5) 아무 파라미터도 없으면 좋아요 수 기준 인기 영상 반환
        return videoService.getPopularByLikeCount();
    }

    @Operation(
            summary = "영상 상세 조회",
            description = "영상 ID에 해당하는 상세 정보를 조회하고, 조회수를 1 증가시키며, 로그인된 사용자라면 시청 기록도 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "영상 상세정보를 정상적으로 조회하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 영상 ID입니다. 다시 확인해 주세요."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
    })
    @GetMapping("/{videoId}")
    public VideoResponseDTO getVideoDetail(
            @Parameter(description = "영상 고유 ID", example = "asdf1234")
            @PathVariable String videoId,
            @Parameter(description = "로그인된 사용자 ID (없으면 null로 넘겨주세요)")
            @RequestParam(required = false) String userId
    ) {
        return videoService.getVideoDetailAndIncrement(videoId, userId);
    }

    @Operation(summary = "최신 영상 50개 조회", description = "최신순으로 정렬된 상위 50개 영상을 반환합니다.")
    @GetMapping("/latest")
    public List<VideoResponseDTO> getLatestVideos() {
        return videoService.getAllVideos();  // 최신순 50개 반환
    }

    @Operation(summary = "카테고리 목록 조회", description = "전체 카테고리 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상적으로 카테고리 목록을 반환합니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @GetMapping("/categories")
    public List<CategoryDTO> getAllCategories() {
        return categoryService.getCategoryList();
    }

    @GetMapping("/mixed")
    public List<VideoResponseDTO> getMixedVideos(
            @RequestParam String keyword,
        @RequestParam(defaultValue = "10") int apiCount
    ) {
            return videoService.searchMixedVideos(keyword, apiCount);
        }
}
