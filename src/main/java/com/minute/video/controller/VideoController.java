package com.minute.video.controller;

import com.minute.video.Entity.Category;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.repository.CategoryRepository;
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

    @Operation(summary = "영상 목록 조회", description =
            "영상 목록을 조회합니다.\n\n" +
                    "1) `category` 파라미터가 있으면 해당 카테고리별 영상 목록을 반환합니다.\n" +
                    "2) `tag` 파라미터가 있으면 해당 태그별 영상 목록을 반환합니다.\n" +
                    "3) `userId`가 있으면 로그인된 사용자의 맞춤 추천 영상을 반환합니다.\n" +
                    "4) 아무 파라미터가 없으면 비로그인 사용자를 대상으로 " +
                    "좋아요 수 기준 인기 영상을 보여주며, 좋아요 데이터가 없으면 조회수 기준, 조회수 데이터도 없으면 최신순 영상 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상적으로 영상 목록을 반환합니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터가 전달되었습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @GetMapping
    public List<VideoResponseDTO> getVideos(
            @Parameter(description = "영상 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description="카테고리 필터") @RequestParam(required = false) String category,
            @Parameter(description="태그 필터")        @RequestParam(required = false) String tag,
            @Parameter(description="로그인된 사용자 ID ")   @RequestParam(required = false) String userId
    ) {
        if(keyword != null && !keyword.isBlank()){
            return videoService.searchByKeyword(keyword);
        }
        if (category != null && !category.isBlank()) {
            // category 파라미터가 있으면 해당 카테고리 영상 목록 조회
            return videoService.getVideoByCategory(category);
        }
        if (tag != null && !tag.isBlank()) {
            // tag 파라미터가 있으면 해당 태그 영상 목록 조회
            return videoService.getVideosByTag(tag);
        }
        if (userId != null && !userId.isBlank()) {
            // 로그인된 사용자: 맞춤 추천 영상 조회
            return videoService.getRecommendedVideos(userId);
        }
        // 아무 필터도 없으면 비로그인 사용자 대상으로 좋아요 수 기준 인기 영상 조회
        return videoService.getPopularByLikeCount();
    }

    @Operation(summary = "영상 상세 조회", description = "영상 ID에 해당하는 상세 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "영상 상세정보를 정상적으로 조회하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 영상 ID입니다. 다시 확인해 주세요."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
    })
    @GetMapping("/{videoId}")
    public VideoResponseDTO getVideoDetail(
            @Parameter(description = "영상 고유 ID",example = "asdf1234")
            @PathVariable String videoId
    ){
        return videoService.getVideoDetail(videoId);
    }

    @GetMapping("/searchAll")
    public List<VideoResponseDTO> searchAnywhere(@RequestParam String keyword) {
        return videoService.searchByTitleOrRegionOrCity(keyword);
    }

    @GetMapping("/search/mixed")
    public List<VideoResponseDTO> searchMixed(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int apiCount) {
        return videoService.searchMixedVideos(keyword, apiCount);
    }



}
