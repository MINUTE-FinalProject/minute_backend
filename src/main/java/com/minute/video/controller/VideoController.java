package com.minute.video.controller;

import com.minute.video.Entity.Category;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.repository.CategoryRepository;
import com.minute.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping
    public List<VideoResponseDTO> getVideos(@RequestParam(required=false) String userId) {
        if (userId == null) {
            return videoService.getAllVideos(); // 비로그인 상태 (영상 전체 조회)
        }else{
            return videoService.getRecommendedVideos(userId);
        }

    }
}
