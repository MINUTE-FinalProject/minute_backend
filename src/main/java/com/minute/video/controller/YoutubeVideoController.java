package com.minute.video.controller;

import com.minute.video.service.YoutubeApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/youtube")
@RequiredArgsConstructor
public class YoutubeVideoController {

    private final YoutubeApiService youtubeApiService;

    // 1. 상단 슬라이더 (지역별)
    @GetMapping("/slider")
    public List<Map<String, Object>> getSliderVideos(@RequestParam String region) {
        // ex) "부산 여행지"로 검색
        String keyword = region + " 여행지";
        return youtubeApiService.searchVideosByKeyword(keyword, 10);
    }

    // 2. 지역별 여행 영상 카드
    @GetMapping("/region")
    public List<Map<String, Object>> getRegionVideos(@RequestParam String region) {
        // ex) "해운대 여행" 등
        String keyword = region + " 여행";
        return youtubeApiService.searchVideosByKeyword(keyword, 5);
    }

    // 3. 쇼츠만
    @GetMapping("/shorts")
    public List<Map<String, Object>> getShortsByRegion(
            @RequestParam String region,
            @RequestParam(defaultValue = "15") int maxResults
    ) {
        return youtubeApiService.searchShortsByRegion(region, maxResults);
    }
}
