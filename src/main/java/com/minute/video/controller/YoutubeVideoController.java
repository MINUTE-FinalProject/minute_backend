package com.minute.video.controller;

import com.minute.video.Entity.YoutubeVideo;
import com.minute.video.service.YoutubeApiService;
import com.minute.video.service.YoutubeVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/youtube")
@RequiredArgsConstructor
public class YoutubeVideoController {

    private final YoutubeApiService youtubeApiService;
    private final YoutubeVideoService youtubeVideoService;

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
            @RequestParam(defaultValue = "KR") String region,
            @RequestParam(defaultValue = "50") int maxResults
    ) {
        return youtubeApiService.searchShortsByRegion(region, maxResults);
    }

    @GetMapping("/db/shorts")
    public List<YoutubeVideo> getDbShorts(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "15") int maxResults
    ) {
        if (region != null && city != null) {
            return youtubeVideoService.getVideosByRegionAndCity(region, city, maxResults);
        } else if (region != null) {
            return youtubeVideoService.getVideosByRegion(region, maxResults);
        } else {
            return youtubeVideoService.getAllVideos(maxResults);
        }
    }

    @PostMapping("/shorts/save")
    public String saveShortsToDb(@RequestParam String region, @RequestParam(defaultValue="15") int maxResults) {
        List<Map<String, Object>> list = youtubeApiService.searchShortsByRegion(region, maxResults);
        youtubeVideoService.saveYoutubeVideos(list, region);
        return "ok";
    }
}
