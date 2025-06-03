package com.minute.video.controller;

import com.minute.video.Entity.Video;
import com.minute.video.service.VideoService;
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
    private final VideoService videoService;

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
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "15") int maxResults
    ) {
        return youtubeApiService.searchShortsByRegion(region, maxResults);
    }

    @GetMapping("/db/shorts")
    public List<Video> getDbShorts(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "15") int maxResults
    ) {
        if (region != null && city != null) {
            // VideoService에서 region, city로 검색하는 메소드 만들어둬야 함
            return videoService.getVideosByRegionAndCity(region, city, maxResults);
        } else if (region != null) {
            return videoService.getVideosByRegion(region, maxResults);
        } else {
            return videoService.getAllVideos(maxResults);
        }
    }

    // ▶▶ 유튜브 API에서 받아온 쇼츠 DB 저장
    @PostMapping("/shorts/save")
    public String saveShortsToDb(@RequestParam String region, @RequestParam(defaultValue="15") int maxResults) {
        List<Map<String, Object>> list = youtubeApiService.searchShortsByRegion(region, maxResults);
        videoService.saveVideosFromApi(list, region);
        return "ok";
    }
}