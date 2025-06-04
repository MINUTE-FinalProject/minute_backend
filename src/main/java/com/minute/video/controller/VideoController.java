package com.minute.video.controller;

import com.minute.video.dto.VideoResponseDTO;
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

    @GetMapping
    public List<VideoResponseDTO> getVideos(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String userId
    ) {
        if(keyword != null && !keyword.isBlank()){
            return videoService.searchByKeyword(keyword);
        }
        if (category != null && !category.isBlank()) {
            return videoService.getVideoByCategory(category);
        }
        if (tag != null && !tag.isBlank()) {
            return videoService.getVideosByTag(tag);
        }
        if (userId != null && !userId.isBlank()) {
            return videoService.getRecommendedVideos(userId);
        }
        return videoService.getPopularByLikeCount();
    }

    @GetMapping("/{videoId}")
    public VideoResponseDTO getVideoDetail(@PathVariable String videoId) {
        return videoService.getVideoDetail(videoId);
    }

    @GetMapping("/latest")
    public List<VideoResponseDTO> getLatestVideos() {
        return videoService.getAllVideos();
    }

    @GetMapping("/categories")
    public List<?> getAllCategories() {
        // 카테고리 목록 반환
        // 서비스 구현에 따라 CategoryDTO 등 반환
        return null;
    }

    @GetMapping("/mixed")
    public List<VideoResponseDTO> getMixedVideos(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int apiCount
    ) {
        return videoService.searchMixedVideos(keyword, apiCount);
    }
}
