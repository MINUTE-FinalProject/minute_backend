package com.minute.video.controller;

import com.minute.video.Entity.Category;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.repository.CategoryRepository;
import com.minute.video.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Tag(name = "Video", description = "영상 관련 API")
public class VideoController {

    private final VideoService videoService;

    @Operation(summary = "영상 목록 조회", description =
            "1) `category`가 있으면 카테고리별, 2) `tag`가 있으면 태그별, 3) `userId`만 있으면 추천, "
                    + "4) 아무것도 없으면 전체 최신순 영상을 반환합니다.")
    @GetMapping
    public List<VideoResponseDTO> getVideos(
            @Parameter(description="카테고리 필터 (optional)") @RequestParam(required = false) String category,
            @Parameter(description="태그 필터 (optional)")        @RequestParam(required = false) String tag,
            @Parameter(description="로그인된 사용자 ID (optional)")   @RequestParam(required = false) String userId
    ) {
        if (category != null && !category.isBlank()) {
            return videoService.getVideoByCategory(category);
        }
        if (tag != null && !tag.isBlank()) {
            return videoService.getVideosByTag(tag);
        }
        return videoService.getVideos(userId);
    }

    @Operation(summary = "영상 상세 조회", description = "영상 ID에 해당하는 상세 정보를 반환합니다.")
    @GetMapping("/{videoId}")
    public VideoResponseDTO getVideoDetail(
            @Parameter(description = "영상 고유 ID",example = "asdf1234")
            @PathVariable String videoId
    ){
        return videoService.getVideoDetail(videoId);
    }
}
