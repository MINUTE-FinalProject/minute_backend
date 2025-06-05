package com.minute.video.controller;

import com.minute.video.Entity.Video;
import com.minute.video.service.VideoService;
import com.minute.video.service.YoutubeApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
//    @GetMapping("/region")
//    public List<Map<String, Object>> getRegionVideos(@RequestParam String region) {
//        // ex) "해운대 여행" 등
//        String keyword = region + " 여행";
//        return youtubeApiService.searchVideosByKeyword(keyword, 5);
//    }

    // 2. 지역별 여행 영상 카드 → 예외 시 빈 리스트 리턴하도록 수정
    @GetMapping("/region")
    public List<Map<String, Object>> getRegionVideos(@RequestParam String region) {
        String keyword = region + " 여행";
        try {
            return youtubeApiService.searchVideosByKeyword(keyword, 5);
        } catch (Exception e) {
            // (1) 로그 출력
            System.err.println("▶ getRegionVideos 예외 발생: region=" + region + ", message=" + e.getMessage());
            e.printStackTrace();
            // (2) 프론트가 빈 배열로 받아서 “영상이 없습니다” UI를 보여줄 수 있도록 빈 리스트 반환
            return Collections.emptyList();
        }
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
        try {
            if (region != null && city != null) {
                return videoService.getVideosByRegionAndCity(region, city, maxResults);
            } else if (region != null) {
                return videoService.getVideosByRegion(region, maxResults);
            } else {
                return videoService.getAllVideos(maxResults);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
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